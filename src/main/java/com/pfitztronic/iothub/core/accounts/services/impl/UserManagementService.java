package com.pfitztronic.iothub.core.accounts.services.impl;

import com.pfitztronic.iothub.core.accounts.exceptions.*;
import com.pfitztronic.iothub.core.accounts.models.Password;
import com.pfitztronic.iothub.core.accounts.models.PhoneNumber;
import com.pfitztronic.iothub.core.accounts.models.User;
import com.pfitztronic.iothub.core.accounts.models.UserStatus;
import com.pfitztronic.iothub.core.accounts.publishers.interfaces.IAuditedEventPublisher;
import com.pfitztronic.iothub.core.accounts.publishers.interfaces.IUserCreatedEventPublisher;
import com.pfitztronic.iothub.core.accounts.publishers.interfaces.IUserNotificationPublisher;
import com.pfitztronic.iothub.core.accounts.repositories.impl.UserRepository;
import com.pfitztronic.iothub.core.accounts.util.PasswordEncoderProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

@Slf4j
public class UserManagementService {
    private final UserRepository userRepository;
    private final VerificationCodeService verificationCodeService;
    private final PasswordResetCodeService passwordResetCodeService;
    private final PasswordEncoderProxy passwordEncoder;
    private final IUserNotificationPublisher userNotificationPublisher;
    private final IUserCreatedEventPublisher userCreatedEventPublisher;
    private final IAuditedEventPublisher auditedEventPublisher;

    public UserManagementService(
            UserRepository userRepository,
            VerificationCodeService verificationCodeService,
            PasswordResetCodeService passwordResetCodeService,
            PasswordEncoderProxy passwordEncoder,
            IAuditedEventPublisher auditedEventPublisher,
            IUserNotificationPublisher userNotificationPublisher,
            IUserCreatedEventPublisher userCreatedEventPublisher
    ) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.verificationCodeService = verificationCodeService;
        this.passwordResetCodeService = passwordResetCodeService;
        this.auditedEventPublisher = auditedEventPublisher;
        this.userNotificationPublisher = userNotificationPublisher;
        this.userCreatedEventPublisher = userCreatedEventPublisher;
    }

    /**
     * Creates a new user account with the provided credentials.
     *
     * @param userId the phone number of the new user (must start with + and be 11-14 digits)
     * @param name the full name of the user
     * @param password the password for the user account (must contain uppercase, lowercase, digit, special char)
     * @return the newly created User object with ACTIVE status and unverified state
     * @throws UserAlreadyExistsException if a user with the given phone number already exists
     * @throws InvalidUserIdentityException if the phone number format is invalid
     * @throws InvalidPasswordFormatException if the password format is invalid
     * @throws DataIntegrityViolationException if there's a database constraint violation
     */
    public User createNewUser(String userId, String name, String password) {
        // check if user already exists
        var existing = userRepository.findUserById(userId);
        if (existing.isPresent()) {
            throw new UserAlreadyExistsException("User with this phone number already exists.");
        }

        try {
            log.info("Creating new user.");
            PhoneNumber validUserId = new PhoneNumber(userId);
            Password validPassword = new Password(password);

            var newUser = User.builder()
                    .userId(validUserId)
                    .name(name)
                    .passwordHash(passwordEncoder.encode(validPassword.password()))
                    .verified(false)
                    .status(UserStatus.ACTIVE)
                    .build();

            log.debug("Saving new user and code to database.");
            var savedUser = userRepository.save(newUser);
            log.info("New user created.");

            log.debug("Generating verification code.");
            verificationCodeService.generateVerificationCode(savedUser.getUserId());

            // Publish events
            log.info("Publishing user creation events.");
            userNotificationPublisher.publishUserNotificationEvent(
                    savedUser.getUserId().number(),
                    "Your account has been successfully created."
            );
            userCreatedEventPublisher.publishUserCreatedEvent(savedUser.getUserId().number());

            return savedUser;

        } catch (DataIntegrityViolationException ex) {
            log.error("Failed to create new user: {}", ex.getMessage());
            throw new UserAlreadyExistsException("User with this phone number already exists.");
        }
    }

    /**
     * Retrieves a user by their phone number.
     *
     * @param userId the phone number of the user to retrieve
     * @return an Optional containing the User if found, or empty if not found
     */
    public Optional<User> getUserById(String userId) {
        return userRepository.findUserById(userId);
    }

    /**
     * Changes the user's name details.
     *
     * @param authedUserId the phone number of the authenticated user
     * @param newName the new name for the user
     * @return the updated User object
     * @throws UserAccountNotFoundException if the user account is not found
     * @throws AccountNotActiveException if the user account is not in ACTIVE status
     * @throws UserAccountNotVerifiedException if the user account has not been verified
     */
    public User changeUserDetails(String authedUserId, String newName) {
        var user = userRepository.findUserById(authedUserId)
                .orElseThrow(() -> new UserAccountNotFoundException("User account not found"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotActiveException("User account is not active");
        }
        if (!user.isVerified()) {
            throw new UserAccountNotVerifiedException("User account is not verified");
        }
        user.changeName(newName);
        return userRepository.save(user);
    }

    /**
     * Changes the password of an authenticated user after verifying the old password.
     *
     * @param authedUserId the phone number of the authenticated user
     * @param oldPassword the current password for verification
     * @param newPassword the new password to set (must contain uppercase, lowercase, digit, special char)
     * @throws UserAccountNotFoundException if the user account is not found
     * @throws AccountNotActiveException if the user account is not in ACTIVE status
     * @throws UserAccountNotVerifiedException if the user account has not been verified
     * @throws InvalidOldPasswordException if the provided old password does not match the stored hash
     * @throws InvalidPasswordFormatException if the new password format is invalid
     */
    public void changePassword(String authedUserId, String oldPassword, String newPassword) {
        var user = userRepository.findUserById(authedUserId)
                .orElseThrow(() -> new UserAccountNotFoundException("User account not found"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotActiveException("User account is not active");
        }
        if (!user.isVerified()) {
            throw new UserAccountNotVerifiedException("User account is not verified");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new InvalidOldPasswordException("Old password is incorrect");
        }
        var validNewPassword = new Password(newPassword);
        user.changePassword(passwordEncoder.encode(validNewPassword.password()));
        userRepository.save(user);
        userNotificationPublisher.publishUserNotificationEvent(
                user.getUserId().number(), "Your password has been changed."
        );
    }

    /**
     * Requests a password reset code to be generated and sent to the user via notification.
     *
     * @param userId the phone number of the user requesting the password reset
     * @throws UserAccountNotFoundException if the user account is not found
     * @throws AccountNotActiveException if the user account is not in ACTIVE status
     */
    public void requestPasswordResetCode(String userId) {
        var user = userRepository.findUserById(userId)
                .orElseThrow(() -> new UserAccountNotFoundException("User account not found"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotActiveException("User account is not active");
        }
        passwordResetCodeService.generateCode(user.getUserId());
    }

    /**
     * Changes the user's password using a password reset code.
     * The reset code must be valid and not expired.
     *
     * @param userId the phone number of the user
     * @param resetCode the password reset code sent to the user
     * @param newPassword the new password to set (must contain uppercase, lowercase, digit, special char)
     * @throws UserAccountNotFoundException if the user account is not found
     * @throws AccountNotActiveException if the user account is not in ACTIVE status
     * @throws IllegalArgumentException if the reset code is invalid or expired
     * @throws InvalidPasswordFormatException if the new password format is invalid
     */
    public void changePasswordWithResetCode(String userId, String resetCode, String newPassword) {
        var user = userRepository.findUserById(userId)
                .orElseThrow(() -> new UserAccountNotFoundException("User account not found"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotActiveException("User account is not active");
        }
        // verify reset code first (throws on invalid/expired)
        passwordResetCodeService.verifyCode(user.getUserId(), resetCode);

        var validNewPassword = new Password(newPassword);
        user.changePassword(passwordEncoder.encode(validNewPassword.password()));
        userRepository.save(user);
        userNotificationPublisher.publishUserNotificationEvent(
                user.getUserId().number(), "Your password has been changed."
        );
    }

    /**
     * Verifies a user account using a verification code.
     * The verification code must be valid and not expired.
     *
     * @param authedUserId the phone number of the authenticated user
     * @param verificationCode the verification code sent to the user's phone
     * @throws UserAccountNotFoundException if the user account is not found
     * @throws AccountNotActiveException if the user account is not in ACTIVE status
     * @throws IllegalUserAccountStateException if the user account is already verified
     * @throws IllegalArgumentException if the verification code is invalid or expired
     */
    public void verifyUserAccount(String authedUserId, String verificationCode) {
        var user = userRepository.findUserById(authedUserId)
                .orElseThrow(() -> new UserAccountNotFoundException("User account not found"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotActiveException("User account is not active");
        }
        if (user.isVerified()) {
            throw new IllegalUserAccountStateException("User account already verified");
        }

        // throws on invalid/expired
        verificationCodeService.verifyCode(user.getUserId(), verificationCode);

        user.verify();
        userRepository.save(user);
        userNotificationPublisher.publishUserNotificationEvent(
                user.getUserId().number(), "Your account has been verified."
        );
    }

    /**
     * Resends a verification code to an unverified user account.
     * Any previously generated verification code will be invalidated.
     *
     * @param authedUserId the phone number of the authenticated user
     * @throws UserAccountNotFoundException if the user account is not found
     * @throws AccountNotActiveException if the user account is not in ACTIVE status
     * @throws IllegalUserAccountStateException if the user account is already verified
     */
    public void resendVerificationCode(String authedUserId) {
        var user = userRepository.findUserById(authedUserId)
                .orElseThrow(() -> new UserAccountNotFoundException("User account not found"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotActiveException("User account is not active");
        }
        if (user.isVerified()) {
            throw new IllegalUserAccountStateException("User account already verified");
        }
        verificationCodeService.generateVerificationCode(user.getUserId());
    }
}
