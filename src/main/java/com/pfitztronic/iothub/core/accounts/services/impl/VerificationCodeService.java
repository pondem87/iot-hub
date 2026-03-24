package com.pfitztronic.iothub.core.accounts.services.impl;

import com.pfitztronic.iothub.core.accounts.models.PhoneNumber;
import com.pfitztronic.iothub.core.accounts.models.VerificationCode;
import com.pfitztronic.iothub.core.accounts.publishers.interfaces.IUserNotificationPublisher;
import com.pfitztronic.iothub.core.accounts.repositories.impl.VerificationCodeRepository;
import com.pfitztronic.iothub.core.accounts.services.AccountsConfigProperties;
import com.pfitztronic.iothub.core.accounts.util.CodeGenerator;
import com.pfitztronic.iothub.core.accounts.util.PasswordEncoderProxy;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
public class VerificationCodeService {
    private final VerificationCodeRepository verificationCodeRepository;
    private final CodeGenerator codeGenerator;
    private final PasswordEncoderProxy passwordEncoder;
    private final IUserNotificationPublisher userNotificationPublisher;
    private final AccountsConfigProperties accountsConfigProps;

    /**
     * Constructs a VerificationCodeService with required dependencies.
     *
     * @param verificationCodeRepository the repository for persisting verification codes
     * @param passwordEncoder the encoder for hashing verification codes
     * @param codeGenerator the generator for creating random verification codes
     * @param userNotificationPublisher the publisher for sending code notifications to users
     * @param accountsConfigProps the configuration properties for code expiration settings
     */
    public VerificationCodeService(
            VerificationCodeRepository verificationCodeRepository,
            PasswordEncoderProxy passwordEncoder,
            CodeGenerator codeGenerator,
            IUserNotificationPublisher userNotificationPublisher,
            AccountsConfigProperties accountsConfigProps
    ) {
        this.verificationCodeRepository = verificationCodeRepository;
        this.codeGenerator = codeGenerator;
        this.passwordEncoder = passwordEncoder;
        this.userNotificationPublisher = userNotificationPublisher;
        this.accountsConfigProps = accountsConfigProps;
    }

    /**
     * Generates a new verification code for the given user and sends it via notification.
     * Any existing verification codes for the user will be invalidated first.
     *
     * @param userId the phone number of the user requesting verification
     * @return the generated VerificationCode object with hash and expiration details
     */
    public VerificationCode generateVerificationCode(PhoneNumber userId) {

        // remove any existing codes for the user
        verificationCodeRepository.clearCodesForUser(userId.number());

        // Generate a new verification code
        var verificationCodeLiteral = codeGenerator.generateCode();

        var verificationCode = VerificationCode.builder()
                .userId(userId)
                .codeHash(passwordEncoder.encode(verificationCodeLiteral))
                .expiresAt(Instant.now().plus(
                        accountsConfigProps.verifyAccountDelayHours(), ChronoUnit.HOURS)
                )
                .build();

        verificationCode = verificationCodeRepository.save(verificationCode);

        // Publish events
        log.info("Publishing verification code creation events.");
        userNotificationPublisher.publishUserNotificationEvent(
                userId.number(),
                "Your account verification code is: " + verificationCodeLiteral
        );

        return verificationCode;
    }

    /**
     * Verifies a verification code for the given user.
     * The code must be valid and not expired. Upon successful verification, the code is deleted.
     *
     * @param userId the phone number of the user
     * @param code the plaintext verification code to verify
     * @throws IllegalArgumentException if no code found, code has expired, or code doesn't match
     */
    public void verifyCode(PhoneNumber userId, String code) {
        var optionalStoredCode = verificationCodeRepository.findLatestByUserId(userId.number());

        if (optionalStoredCode.isEmpty()) {
            throw new IllegalArgumentException("No verification code found for user");
        }

        var storedCode = optionalStoredCode.get();

        if (storedCode.isExpired()) {
            verificationCodeRepository.deleteById(storedCode.getId());
            throw new IllegalArgumentException("Verification code has expired");
        }

        if (!passwordEncoder.matches(code, storedCode.getCodeHash())) {
            throw new IllegalArgumentException("Invalid verification code");
        }

        // Delete the code after successful verification
        log.info("Verification code verified successfully for user: {}", userId.number());
        verificationCodeRepository.deleteById(storedCode.getId());
    }
}
