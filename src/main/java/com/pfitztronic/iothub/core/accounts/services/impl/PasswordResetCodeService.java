package com.pfitztronic.iothub.core.accounts.services.impl;

import com.pfitztronic.iothub.core.accounts.exceptions.InvalidPasswordResetCodeException;
import com.pfitztronic.iothub.core.accounts.exceptions.PasswordResetCodeExpiredException;
import com.pfitztronic.iothub.core.accounts.exceptions.PasswordResetCodeNotFoundException;
import com.pfitztronic.iothub.core.accounts.models.PasswordResetCode;
import com.pfitztronic.iothub.core.accounts.models.PhoneNumber;
import com.pfitztronic.iothub.core.accounts.publishers.interfaces.IUserNotificationPublisher;
import com.pfitztronic.iothub.core.accounts.repositories.impl.PasswordResetCodeRepository;
import com.pfitztronic.iothub.core.accounts.services.AccountsConfigProperties;
import com.pfitztronic.iothub.core.accounts.util.CodeGenerator;
import com.pfitztronic.iothub.core.accounts.util.PasswordEncoderProxy;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Service class for managing password reset code operations.
 * Handles the generation, verification, and lifecycle of password reset codes.
 * This service ensures that password reset codes are securely hashed, properly expired, and validated before use.
 */
@Slf4j
public class PasswordResetCodeService {
    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final CodeGenerator codeGenerator;
    private final PasswordEncoderProxy passwordEncoder;
    private final IUserNotificationPublisher userNotificationPublisher;
    private final AccountsConfigProperties accountsConfigProps;

    /**
     * Constructs a PasswordResetCodeService with required dependencies.
     *
     * @param passwordResetCodeRepository the repository for persisting password reset codes
     * @param passwordEncoder the encoder for hashing password reset codes
     * @param codeGenerator the generator for creating random password reset codes
     * @param userNotificationPublisher the publisher for sending code notifications to users
     * @param accountsConfigProps the configuration properties for code expiration settings
     */
    public PasswordResetCodeService(
            PasswordResetCodeRepository passwordResetCodeRepository,
            PasswordEncoderProxy passwordEncoder,
            CodeGenerator codeGenerator,
            IUserNotificationPublisher userNotificationPublisher,
            AccountsConfigProperties accountsConfigProps
    ) {
        this.passwordResetCodeRepository = passwordResetCodeRepository;
        this.codeGenerator = codeGenerator;
        this.passwordEncoder = passwordEncoder;
        this.userNotificationPublisher = userNotificationPublisher;
        this.accountsConfigProps = accountsConfigProps;
    }

    /**
     * Generates a password reset code for the given user and sends it via notification.
     *
     * @param userId the phone number of the user
     * @return the generated password reset code
     */
    public PasswordResetCode generateCode(PhoneNumber userId) {
        // Remove any existing codes for the user
        passwordResetCodeRepository.clearCodesForUser(userId.number());

        // Generate a new password reset code
        var passwordResetCodeLiteral = codeGenerator.generateCode();

        var passwordResetCode = PasswordResetCode.builder()
                .userId(userId)
                .codeHash(passwordEncoder.encode(passwordResetCodeLiteral))
                .expiresAt(Instant.now().plus(
                        accountsConfigProps.passwordResetDelayHours(), ChronoUnit.HOURS)
                )
                .build();

        passwordResetCode = passwordResetCodeRepository.save(passwordResetCode);

        // Publish notification event
        log.info("Publishing password reset code creation event for user: {}", userId.number());
        userNotificationPublisher.publishUserNotificationEvent(
                userId.number(),
                "Your password reset code is: " + passwordResetCodeLiteral
        );

        return passwordResetCode;
    }

    /**
     * Verifies the password reset code for the given user.
     *
     * @param userId the phone number of the user
     * @param code the plaintext password reset code
     * @throws PasswordResetCodeNotFoundException if no code exists
     * @throws PasswordResetCodeExpiredException if the code has expired
     * @throws InvalidPasswordResetCodeException if the code doesn't match
     */
    public void verifyCode(PhoneNumber userId, String code) {
        var optionalStoredCode = passwordResetCodeRepository.findLatestByUserId(userId.number());

        if (optionalStoredCode.isEmpty()) {
            throw new PasswordResetCodeNotFoundException("No password reset code found for user");
        }

        var storedCode = optionalStoredCode.get();

        // Check if the code has expired
        if (storedCode.isExpired()) {
            passwordResetCodeRepository.deleteById(storedCode.getId());
            throw new PasswordResetCodeExpiredException("Password reset code has expired");
        }

        // Verify the code hash using matches
        if (!passwordEncoder.matches(code, storedCode.getCodeHash())) {
            throw new InvalidPasswordResetCodeException("Invalid password reset code");
        }

        // Delete the code after successful verification
        log.info("Password reset code verified successfully for user: {}", userId.number());
        passwordResetCodeRepository.deleteById(storedCode.getId());
    }
}


