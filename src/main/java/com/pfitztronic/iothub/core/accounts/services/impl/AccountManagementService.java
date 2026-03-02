package com.pfitztronic.iothub.core.accounts.services.impl;
import com.pfitztronic.iothub.core.accounts.dto.CreateNewAccountData;
import com.pfitztronic.iothub.core.accounts.dto.NewAccountResponse;
import com.pfitztronic.iothub.core.accounts.exceptions.AccountCreationLimitExceededException;
import com.pfitztronic.iothub.core.accounts.exceptions.AccountDoesNotExistException;
import com.pfitztronic.iothub.core.accounts.exceptions.AccountNameAlreadyExistsException;
import com.pfitztronic.iothub.core.accounts.exceptions.InvalidAccountIdentifierException;
import com.pfitztronic.iothub.core.accounts.models.Account;
import com.pfitztronic.iothub.core.accounts.models.AccountName;
import com.pfitztronic.iothub.core.accounts.models.AccountStatus;
import com.pfitztronic.iothub.core.accounts.models.User;
import com.pfitztronic.iothub.core.accounts.publishers.interfaces.*;
import com.pfitztronic.iothub.core.accounts.repositories.impl.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
public class AccountManagementService {
    private static final int DELETE_GRACE_PERIOD_DAYS = 30;
    private final AccountRepository accountRepository;
    private final UserManagementService userManagementService;
    private final IUserNotificationPublisher userNotificationPublisher;
    private final IAccountCreatedEventPublisher accountCreatedEventPublisher;
    private final IAuditedEventPublisher auditedEventPublisher;
    private final IAccountStatusChangedEventPublisher accountStatusChangedEventPublisher;
    private final IAccountDeletedEventPublisher accountDeletedEventPublisher;

    public AccountManagementService(
            AccountRepository accountRepository,
            UserManagementService userManagementService,
            IUserNotificationPublisher userNotificationPublisher,
            IAccountCreatedEventPublisher accountCreatedEventPublisher,
            IAccountStatusChangedEventPublisher accountStatusChangedEventPublisher,
            IAuditedEventPublisher auditedEventPublisher,
            IAccountDeletedEventPublisher accountDeletedEventPublisher
    ) {
        this.accountRepository = accountRepository;
        this.userManagementService = userManagementService;
        this.userNotificationPublisher = userNotificationPublisher;
        this.accountCreatedEventPublisher = accountCreatedEventPublisher;
        this.accountStatusChangedEventPublisher = accountStatusChangedEventPublisher;
        this.auditedEventPublisher = auditedEventPublisher;
        this.accountDeletedEventPublisher = accountDeletedEventPublisher;
    }

    public NewAccountResponse createNewAccount(CreateNewAccountData data) {
        User adminUser;

        // check if user already exists is handled in UserManagementService
        adminUser = this.userManagementService.createNewUser(
                data.phoneNumber().trim(),
                data.userName().trim(),
                data.password().trim()
        );

        return createAccount(adminUser, data.accountName().trim());
    }

    public NewAccountResponse createAccountForUser(String userId, String accountName) {
        User user = this.userManagementService.getUserById(userId);
        return createAccount(user, accountName);
    }

    private NewAccountResponse createAccount(User user, String accountName) {
        log.info("Creating new account with name: {}", accountName);
        // Check name availability
        checkAccountNameAvailability(accountName);

        // check if user has more accounts
        checkIfUserCanCreateAccount(user.getUserId().number(), 1);

        // Create and save account
        var newAccount = Account.builder()
                .accountName(new AccountName(accountName))
                .adminId(user.getUserId())
                .build();

        try {
            Account savedAccount = this.accountRepository.save(newAccount);
            log.info("New account created.");

            // Publish events
            log.info("Publishing account created events.");
            this.accountCreatedEventPublisher.publishAccountCreatedEvent(savedAccount.getAccountId());
            this.userNotificationPublisher.publishUserNotificationEvent(
                    user.getUserId().number(),
                    "Your account '" + savedAccount.getAccountName().value() + "' has been created successfully."
            );
            this.auditedEventPublisher.publishAuditedEvent(
                    new AuditedEvent(
                            savedAccount.getAccountId(),
                            user.getUserId().number(),
                            Instant.now(),
                            "Account",
                            savedAccount.getAccountId().toString(),
                            "Account created with name: " + savedAccount.getAccountName().value(),
                            "AccountManagementService"
                    )
            );

            return new NewAccountResponse(
                    savedAccount.getAccountId().toString(),
                    savedAccount.getAccountName().value(),
                    user.getUserId().number()
            );
        } catch (DataIntegrityViolationException ex) {
            throw new AccountNameAlreadyExistsException("Account with this name already exists.");
        }
    }

    private void checkAccountNameAvailability(String accountName) {
        var existingAccount = this.accountRepository.findOneByName(accountName);
        if (existingAccount != null) {
            log.debug("Account with name {} already exists.", accountName);
            throw new AccountNameAlreadyExistsException("Account name already in use.");
        }
    }

    private void checkIfUserCanCreateAccount(String userId, long limit) {
        var numberOfAccountsWhereUserIsAdmin = accountRepository.findCountByAdminId(userId);
        if (numberOfAccountsWhereUserIsAdmin >= limit) {
            log.debug("User {} has reached the account creation limit. Account creation aborted.", userId);
            throw new AccountCreationLimitExceededException("User has reached the limit of accounts they can create.");
        }
    }

    public String disableAccount(String accountId) {
        try {
            var accountUuid = UUID.fromString(accountId);
            return changeAccountStatus(accountUuid, AccountStatus.DISABLED);
        } catch (IllegalArgumentException ex) {
            throw new InvalidAccountIdentifierException("Invalid account identifier provided.");
        }

    }

    public String reactivateAccount(String accountId) {
        try {
            var accountUuid = UUID.fromString(accountId);
            return changeAccountStatus(accountUuid, AccountStatus.ACTIVE);
        } catch (IllegalArgumentException ex) {
            throw new InvalidAccountIdentifierException("Invalid account identifier provided.");
        }
    }

    private String changeAccountStatus(UUID accountId, AccountStatus newStatus) {
        var account = this.accountRepository.findOneById(accountId);
        if (account == null) {
            throw new AccountDoesNotExistException("Account with the provided identifier does not exist.");
        }

        if (AccountStatus.ACTIVE.equals(newStatus)) {
            account.activate();
        } else if (AccountStatus.DISABLED.equals(newStatus)) {
            account.disable();
        } else {
            throw new IllegalArgumentException("Unsupported account status: " + newStatus);
        }

        var savedAccount = this.accountRepository.save(account);

        // Publish events
        log.info("Account with id {} has changed status to {}.", accountId, newStatus);
        this.auditedEventPublisher.publishAuditedEvent(
                new AuditedEvent(
                        savedAccount.getAccountId(),
                        savedAccount.getAdminId().number(),
                        Instant.now(),
                        "Account",
                        savedAccount.getAccountId().toString(),
                        "Account status changed to %s".formatted(savedAccount.getStatus().name()),
                        "AccountManagementService"
                )
        );
        this.userNotificationPublisher.publishUserNotificationEvent(
                savedAccount.getAdminId().number(),
                "Your account '%s' has changed status to '%s'.".formatted(savedAccount.getAccountName(), savedAccount.getStatus().name())
        );
        this.accountStatusChangedEventPublisher.publishAccountStatusChangedEvent(
                new AccountStatusChangedEvent(
                        savedAccount.getAccountId(),
                        savedAccount.getStatus().name()
                )
        );

        return "Account status changed to '%s'".formatted(savedAccount.getStatus().name());
    }

    public String permanentlyDeleteAccount(String accountId) {
        try {
            var accountUuid = UUID.fromString(accountId);
            return disableAndMarkForDelete(accountUuid);
        } catch (IllegalArgumentException e) {
            throw new InvalidAccountIdentifierException("Invalid account identifier provided.");
        }
    }

    private String disableAndMarkForDelete(UUID accountId) {
        var account = accountRepository.findOneById(accountId);
        if (account == null) {
            throw new AccountDoesNotExistException("This account does not exist.");
        }
        account.markForDeletionAt(Instant.now().plus(DELETE_GRACE_PERIOD_DAYS, ChronoUnit.DAYS));
        var savedAccount = accountRepository.save(account);

        // publish events
        this.auditedEventPublisher.publishAuditedEvent(
                new AuditedEvent(
                        savedAccount.getAccountId(),
                        savedAccount.getAdminId().number(),
                        Instant.now(),
                        "Account",
                        savedAccount.getAccountId().toString(),
                        "Account status changed to %s and scheduled for complete delete".formatted(savedAccount.getStatus().name()),
                        "AccountManagementService"
                )
        );
        this.userNotificationPublisher.publishUserNotificationEvent(
                savedAccount.getAdminId().number(),
                "Your account '%s' has changed status to '%s' and all data will be deleted in %d days"
                        .formatted(savedAccount.getAccountName(), savedAccount.getStatus().name(), DELETE_GRACE_PERIOD_DAYS)
        );
        this.accountStatusChangedEventPublisher.publishAccountStatusChangedEvent(
                new AccountStatusChangedEvent(
                        savedAccount.getAccountId(),
                        savedAccount.getStatus().name()
                )
        );

        return "Account status changed to '%s'".formatted(savedAccount.getStatus().name());
    }

    public void deleteAccountsMarkedAndDueForDelete() {
        var accountsToDelete = accountRepository.findAllMarkedForDeletion();
        for (var account : accountsToDelete) {
            var deleted = accountRepository.delete(account);

            if (!deleted) {
                log.warn("Failed to delete account with id {}. It will be retried in the next cleanup cycle.", account.getAccountId());
                continue;
            }

            log.info("Account with id {} has been permanently deleted.", account.getAccountId());

            // publish events
            this.auditedEventPublisher.publishAuditedEvent(
                    new AuditedEvent(
                            account.getAccountId(),
                            account.getAdminId().number(),
                            Instant.now(),
                            "Account",
                            account.getAccountId().toString(),
                            "Account permanently deleted",
                            "AccountManagementService"
                    )
            );
            this.userNotificationPublisher.publishUserNotificationEvent(
                    account.getAdminId().number(),
                    "Your account '%s' has been permanently deleted.".formatted(account.getAccountName())
            );
            this.accountDeletedEventPublisher.publishAccountDeletedEvent(account.getAccountId());
        }
    }
}
