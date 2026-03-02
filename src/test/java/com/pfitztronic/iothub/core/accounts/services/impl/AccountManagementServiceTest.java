package com.pfitztronic.iothub.core.accounts.services.impl;

import com.pfitztronic.iothub.core.accounts.dto.CreateNewAccountData;
import com.pfitztronic.iothub.core.accounts.dto.NewAccountResponse;
import com.pfitztronic.iothub.core.accounts.exceptions.AccountNameAlreadyExistsException;
import com.pfitztronic.iothub.core.accounts.models.Account;
import com.pfitztronic.iothub.core.accounts.models.AccountName;
import com.pfitztronic.iothub.core.accounts.models.PhoneNumber;
import com.pfitztronic.iothub.core.accounts.models.User;
import com.pfitztronic.iothub.core.accounts.publishers.interfaces.*;
import com.pfitztronic.iothub.core.accounts.repositories.impl.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Account Management Service Tests")
class AccountManagementServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private UserManagementService userManagementService;
    @Mock
    private IUserNotificationPublisher userNotificationPublisher;
    @Mock
    private IAccountCreatedEventPublisher accountCreatedEventPublisher;
    @Mock
    private IAccountStatusChangedEventPublisher accountStatusChangedEventPublisher;
    @Mock
    private IAuditedEventPublisher auditedEventPublisher;
    @Mock
    private IAccountDeletedEventPublisher accountDeletedEventPublisher;

    private AccountManagementService accountManagementService;


    @Nested
    @DisplayName("Create New Account Tests")
    class CreateNewAccountTests {

        @BeforeEach
        public void setup() {
            // Any setup before each test can be done here
            accountManagementService = new AccountManagementService(
                    accountRepository,
                    userManagementService,
                    userNotificationPublisher,
                    accountCreatedEventPublisher,
                    accountStatusChangedEventPublisher,
                    auditedEventPublisher,
                    accountDeletedEventPublisher
            );
        }


        @Test
        public void createNewAccountSuccess() {
            // given
            String passwordHash = "694d939ae6e91fd93e43eb276b0fc3f77bc85454ad74cd46663b53d058064858";
            String name = "Test User";
            String userId = "+12345678901";
            String password = "Secure@Password123";

            User savedUser = User.builder()
                    .userId(new PhoneNumber(userId))
                    .name(name)
                    .passwordHash(passwordHash)
                    .createdAt(java.time.Instant.now())
                    .build();

            String accountName = "test account";
            UUID accountId = UUID.randomUUID();

            // mocks
            when(userManagementService.createNewUser(
                    userId,
                    name,
                    password
            )).thenReturn(savedUser);

            when(accountRepository.findOneByName(accountName))
                    .thenReturn(null);

            when(accountRepository.save(any(Account.class)))
                    .thenReturn(
                            Account.builder()
                                    .accountId(accountId)
                                    .accountName(new AccountName(accountName))
                                    .adminId(savedUser.getUserId())
                                    .createdAt(java.time.Instant.now())
                                    .build()
                    );

            // when
            NewAccountResponse response = accountManagementService.createNewAccount(
                    new CreateNewAccountData(
                            userId,
                            name,
                            password,
                            accountName
                    )
            );

            // then
            // assert response
            assertNotNull(response);
            assertEquals(accountName.toLowerCase(), response.accountName());
            assertEquals(savedUser.getUserId().number(), response.adminId());
            assertEquals(accountId.toString(), response.accountId());

            // assert method calls
            ArgumentCaptor<Account>  accountCaptor = ArgumentCaptor.forClass(Account.class);
            verify(accountRepository).save(accountCaptor.capture());
            assertEquals(new AccountName(accountName).value(), accountCaptor.getValue().getAccountName().value());
            assertEquals(userId, accountCaptor.getValue().getAdminId().number());

            // assert events published
            verify(accountCreatedEventPublisher, times(1)).publishAccountCreatedEvent(accountId);
            verify(userNotificationPublisher, times(1)).publishUserNotificationEvent(
                    savedUser.getUserId().number(),
                    "Your account '" + accountName.toLowerCase() + "' has been created successfully."
            );
            verify(auditedEventPublisher, times(1)).publishAuditedEvent(any(AuditedEvent.class));

            // assert audited event content
            ArgumentCaptor<AuditedEvent> auditedEventCaptor = ArgumentCaptor.forClass(AuditedEvent.class);
            verify(auditedEventPublisher).publishAuditedEvent(auditedEventCaptor.capture());
            AuditedEvent capturedEvent = auditedEventCaptor.getValue();
            assertEquals(accountId, capturedEvent.accountId());
            assertEquals(savedUser.getUserId().number(), capturedEvent.userId());
            assertEquals("Account", capturedEvent.objectType());
            assertEquals(accountId.toString(), capturedEvent.objectId());
            assertEquals("Account created with name: " + accountName.toLowerCase(), capturedEvent.description());
            assertEquals("AccountManagementService", capturedEvent.source());
        }

        @Test
        public void createNewAccountFailsWhenAccountNameExists() {
            // given
            String passwordHash = "694d939ae6e91fd93e43eb276b0fc3f77bc85454ad74cd46663b53d058064858";
            String name = "Test User";
            String userId = "+12345678901";
            String password = "Secure@Password123";
            User savedUser = User.builder()
                    .userId(new PhoneNumber(userId))
                    .name(name)
                    .passwordHash(passwordHash)
                    .createdAt(java.time.Instant.now())
                    .build();

            String accountName = "Test Account";

            Account existingAccount = Account.builder()
                    .accountId(UUID.randomUUID())
                    .accountName(new AccountName(accountName))
                    .adminId(savedUser.getUserId())
                    .createdAt(java.time.Instant.now())
                    .build();

            // mocks
            when(userManagementService.createNewUser(
                    userId,
                    name,
                    password
            )).thenReturn(savedUser);

            when(accountRepository.findOneByName(accountName)).thenReturn(existingAccount);

            // when
            Exception exception = assertThrows(
                    AccountNameAlreadyExistsException.class,
                    () -> accountManagementService.createNewAccount(
                            new CreateNewAccountData(
                                    userId,
                                    name,
                                    password,
                                    accountName
                            )
                    )
            );

            // then
            assertEquals("Account name already in use.", exception.getMessage());
            verify(accountRepository, never()).save(any(Account.class));
        }

    }
}