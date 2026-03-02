package com.pfitztronic.iothub.core.accounts.services.impl;

import com.pfitztronic.iothub.core.accounts.exceptions.UserAlreadyExistsException;
import com.pfitztronic.iothub.core.accounts.models.Password;
import com.pfitztronic.iothub.core.accounts.models.PhoneNumber;
import com.pfitztronic.iothub.core.accounts.models.User;
import com.pfitztronic.iothub.core.accounts.models.VerificationCode;
import com.pfitztronic.iothub.core.accounts.publishers.interfaces.IAuditedEventPublisher;
import com.pfitztronic.iothub.core.accounts.publishers.interfaces.IUserCreatedEventPublisher;
import com.pfitztronic.iothub.core.accounts.publishers.interfaces.IUserNotificationPublisher;
import com.pfitztronic.iothub.core.accounts.repositories.impl.UserRepository;
import com.pfitztronic.iothub.core.accounts.repositories.impl.VerificationCodeRepository;
import com.pfitztronic.iothub.core.accounts.util.CodeGenerator;
import com.pfitztronic.iothub.core.accounts.util.PasswordEncoderProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;


@Slf4j
public class UserManagementService {
    private final UserRepository userRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final PasswordEncoderProxy passwordEncoder;
    private final CodeGenerator codeGenerator;
    private final IUserNotificationPublisher userNotificationPublisher;
    private final IUserCreatedEventPublisher userCreatedEventPublisher;
    private final IAuditedEventPublisher auditedEventPublisher;

    public UserManagementService(
            UserRepository userRepository,
            VerificationCodeRepository verificationCodeRepository,
            CodeGenerator codeGenerator,
            PasswordEncoderProxy passwordEncoder,
            IAuditedEventPublisher auditedEventPublisher,
            IUserNotificationPublisher userNotificationPublisher,
            IUserCreatedEventPublisher userCreatedEventPublisher
    ) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.verificationCodeRepository = verificationCodeRepository;
        this.codeGenerator = codeGenerator;
        this.auditedEventPublisher = auditedEventPublisher;
        this.userNotificationPublisher = userNotificationPublisher;
        this.userCreatedEventPublisher = userCreatedEventPublisher;
    }

    public User createNewUser(String userId, String name, String password) {
        // check if user already exists
        var user = userRepository.findUserById(userId);
        if (user != null) {
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
                    .build();

            log.debug("Generating verification code.");
            var verificationCodeLiteral = codeGenerator.generateCode();

            var verificationCode = VerificationCode.builder()
                    .userId(validUserId)
                    .codeHash(passwordEncoder.encode(verificationCodeLiteral))
                    .build();

            log.debug("Saving new user and code to database.");
            var savedUser = userRepository.save(newUser);
            log.info("New user created.");
            verificationCodeRepository.save(verificationCode);

            // Publish events
            log.info("Publishing user creation events.");
            userNotificationPublisher.publishUserNotificationEvent(
                    savedUser.getUserId().number(),
                    "Your account verification code is: " + verificationCodeLiteral
            );
            userCreatedEventPublisher.publishUserCreatedEvent(savedUser.getUserId().number());

            // return user
            return savedUser;

        } catch (DataIntegrityViolationException ex) {
            // thrown when unique constraint is violated (e.g., userId already exists)
            log.error("Failed to create new user: {}", ex.getMessage());
            throw new UserAlreadyExistsException("User with this phone number already exists.");
        }
    }

    public User getUserById(String userId) {
        return userRepository.findUserById(userId);
    }
}
