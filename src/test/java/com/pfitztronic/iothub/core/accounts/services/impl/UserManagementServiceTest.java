package com.pfitztronic.iothub.core.accounts.services.impl;

import com.pfitztronic.iothub.core.accounts.exceptions.InvalidPasswordFormatException;
import com.pfitztronic.iothub.core.accounts.exceptions.InvalidUserIdentityException;
import com.pfitztronic.iothub.core.accounts.exceptions.UserAlreadyExistsException;
import com.pfitztronic.iothub.core.accounts.models.PhoneNumber;
import com.pfitztronic.iothub.core.accounts.models.User;
import com.pfitztronic.iothub.core.accounts.models.UserStatus;
import com.pfitztronic.iothub.core.accounts.models.VerificationCode;
import com.pfitztronic.iothub.core.accounts.publishers.interfaces.IAuditedEventPublisher;
import com.pfitztronic.iothub.core.accounts.publishers.interfaces.IUserCreatedEventPublisher;
import com.pfitztronic.iothub.core.accounts.publishers.interfaces.IUserNotificationPublisher;
import com.pfitztronic.iothub.core.accounts.repositories.impl.UserRepository;
import com.pfitztronic.iothub.core.accounts.repositories.impl.VerificationCodeRepository;
import com.pfitztronic.iothub.core.accounts.util.CodeGenerator;
import com.pfitztronic.iothub.core.accounts.util.PasswordEncoderProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Management Service Tests")
class UserManagementServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private VerificationCodeRepository verificationCodeRepository;
    @Mock
    private PasswordEncoderProxy passwordEncoder;
    @Mock
    private CodeGenerator codeGenerator;
    @Mock
    private IUserCreatedEventPublisher userCreatedEventPublisher;
    @Mock
    private IUserNotificationPublisher userNotificationPublisher;
    @Mock
    private IAuditedEventPublisher auditedEventPublisher;

    private UserManagementService userManagementService;


    @Nested
    @DisplayName("Create New User Tests")
    class CreateNewUserTests {

        private String userId;
        private String name;
        private String password;
        private String passwordHash;
        private String generatedCode;
        private String generatedCodeHash;

        @BeforeEach
        public void setUp() {
            userManagementService = new UserManagementService(
                    userRepository,
                    verificationCodeRepository,
                    codeGenerator,
                    passwordEncoder,
                    auditedEventPublisher,
                    userNotificationPublisher,
                    userCreatedEventPublisher
            );
        }

        @Test
        void createNewUserSuccess() {
            // given
            userId = "+12345678901";
            name = "Test User";
            password = "Secure@Password123";
            passwordHash = "694d939ae6e91fd93e43eb276b0fc3f77bc85454ad74cd46663b53d058064858";

            generatedCode = "943112";
            generatedCodeHash = "66855c596d4492ebaba6e691b6a6d3899a31a45da0788b75e443bfb317824c84";

            // mocks
            when(passwordEncoder.encode(password)).thenReturn(passwordHash);
            when(passwordEncoder.encode(generatedCode)).thenReturn(generatedCodeHash);
            when(codeGenerator.generateCode()).thenReturn(generatedCode);

            when(userRepository.save(any(User.class))).thenReturn(
                    User.builder()
                            .userId(new PhoneNumber(userId))
                            .name(name)
                            .passwordHash(passwordHash)
                            .createdAt(Instant.now())
                            .build()
            );
            when(verificationCodeRepository.save(any(VerificationCode.class))).thenReturn(
                    VerificationCode.builder()
                            .id(UUID.randomUUID())
                            .userId(new PhoneNumber(userId))
                            .codeHash(generatedCodeHash)
                            .createdAt(Instant.now())
                            .build()
            );

            // when
            User newUser = userManagementService.createNewUser(userId, name, password);

            // then
            assertNotNull(newUser);
            assertEquals(name, newUser.getName());
            assertEquals(userId, newUser.getUserId().number());
            assertFalse(newUser.isVerified());
            assertEquals(UserStatus.ACTIVE, newUser.getStatus());
            // capture call arguments
            ArgumentCaptor<User> unsavedUserCaptor = ArgumentCaptor.forClass(User.class);
            ArgumentCaptor<VerificationCode> unsavedVerificationCodeCaptor = ArgumentCaptor.forClass(VerificationCode.class);
            verify(userRepository, times(1)).save(unsavedUserCaptor.capture());
            verify(verificationCodeRepository, times(1)).save(unsavedVerificationCodeCaptor.capture());
            // verify captor arguments
            // check unsavedUser
            assertEquals(userId, unsavedUserCaptor.getValue().getUserId().number());
            assertEquals(name, unsavedUserCaptor.getValue().getName());
            assertEquals(passwordHash, unsavedUserCaptor.getValue().getPasswordHash());
            // check unsavedVerificationCode
            assertEquals(userId, unsavedVerificationCodeCaptor.getValue().getUserId().number());
            assertEquals(generatedCodeHash, unsavedVerificationCodeCaptor.getValue().getCodeHash());
            verify(passwordEncoder, times(1)).encode(password);
            verify(userCreatedEventPublisher, times(1)).publishUserCreatedEvent(newUser.getUserId().number());
            verify(userNotificationPublisher, times(1)).publishUserNotificationEvent(
                    newUser.getUserId().number(),
                    "Your account verification code is: " + generatedCode
            );
        }

        @Test
        public void createNewUserFailAlreadyExists() {
            // given
            userId = "+12345678901";
            name = "Test User";
            password = "Secure@Password123";
            passwordHash = "694d939ae6e91fd93e43eb276b0fc3f77bc85454ad74cd46663b53d058064858";

            generatedCode = "943112";
            generatedCodeHash = "66855c596d4492ebaba6e691b6a6d3899a31a45da0788b75e443bfb317824c84";

            // mocks
            when(userRepository.findUserById(userId)).thenReturn(
                    User.builder()
                            .userId(new PhoneNumber(userId))
                            .name(name)
                            .passwordHash(passwordHash)
                            .createdAt(Instant.now())
                            .build()
            );

            // when
            Exception exception = assertThrows(UserAlreadyExistsException.class, () -> {
                userManagementService.createNewUser(userId, name, password);
            });

            // then
            assertEquals("User with this phone number already exists.", exception.getMessage());
        }

        @Test
        public void createNewUserFailInvalidPhoneNumber() {
            // given
            userId = "12345678901";
            name = "Test User";
            password = "Secure@Password123";

            // when
            Exception exception = assertThrows(InvalidUserIdentityException.class, () -> {
                userManagementService.createNewUser(userId, name, password);
            });

            // then
            assertEquals("Phone should start with + and be between 11 and 14 digits long.", exception.getMessage());
        }

        @Test
        public void createNewUserFailInvalidPassword() {
            // given
            userId = "+12345678901";
            name = "Test User";
            password = "SecurePassword123";

            // when
            Exception exception = assertThrows(InvalidPasswordFormatException.class, () -> {
                userManagementService.createNewUser(userId, name, password);
            });

        }
    }

}