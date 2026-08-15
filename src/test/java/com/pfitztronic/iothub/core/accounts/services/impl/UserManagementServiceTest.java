package com.pfitztronic.iothub.core.accounts.services.impl;

import com.pfitztronic.iothub.core.TestFixtures;
import com.pfitztronic.iothub.core.accounts.exceptions.*;
import com.pfitztronic.iothub.core.accounts.models.PhoneNumber;
import com.pfitztronic.iothub.core.accounts.models.User;
import com.pfitztronic.iothub.core.accounts.models.UserStatus;
import com.pfitztronic.iothub.core.accounts.models.VerificationCode;
import com.pfitztronic.iothub.core.accounts.publishers.interfaces.IAuditedEventPublisher;
import com.pfitztronic.iothub.core.accounts.publishers.interfaces.IUserCreatedEventPublisher;
import com.pfitztronic.iothub.core.accounts.publishers.interfaces.IUserNotificationPublisher;
import com.pfitztronic.iothub.core.accounts.repositories.impl.UserRepository;
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
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Management Service Tests")
class UserManagementServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private VerificationCodeService verificationCodeService;
    @Mock
    private PasswordResetCodeService passwordResetCodeService;
    @Mock
    private PasswordEncoderProxy passwordEncoder;
    @Mock
    private IUserCreatedEventPublisher userCreatedEventPublisher;
    @Mock
    private IUserNotificationPublisher userNotificationPublisher;
    @Mock
    private IAuditedEventPublisher auditedEventPublisher;

    private UserManagementService userManagementService;

    // Common test data for nested classes
    private PhoneNumber testUserId;
    private User activeVerifiedUser;

    @BeforeEach
    void setUp() {
        userManagementService = new UserManagementService(
                userRepository,
                verificationCodeService,
                passwordResetCodeService,
                passwordEncoder,
                auditedEventPublisher,
                userNotificationPublisher,
                userCreatedEventPublisher
        );
        testUserId = new PhoneNumber("+12345678901");
        activeVerifiedUser = User.builder()
                .userId(testUserId)
                .name("John")
                .passwordHash("hashed")
                .verified(true)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Nested
    @DisplayName("Create New User Tests")
    class CreateNewUserTests {

        private String userId;
        private String name;
        private String password;
        private String passwordHash;
        private String generatedCode;
        private String generatedCodeHash;

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
            when(verificationCodeService.generateVerificationCode(any(PhoneNumber.class))).thenReturn(
                    VerificationCode.builder()
                            .userId(new PhoneNumber(userId))
                            .codeHash(generatedCodeHash)
                            .createdAt(Instant.now())
                            .build()
            );

            when(userRepository.save(any(User.class))).thenReturn(
                    User.builder()
                            .userId(new PhoneNumber(userId))
                            .name(name)
                            .passwordHash(passwordHash)
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
            verify(userRepository, times(1)).save(unsavedUserCaptor.capture());
            // verify captor arguments
            assertEquals(userId, unsavedUserCaptor.getValue().getUserId().number());
            assertEquals(name, unsavedUserCaptor.getValue().getName());
            assertEquals(passwordHash, unsavedUserCaptor.getValue().getPasswordHash());
            verify(passwordEncoder, times(1)).encode(password);
            verify(verificationCodeService, times(1)).generateVerificationCode(new PhoneNumber(userId));
            verify(userCreatedEventPublisher, times(1)).publishUserCreatedEvent(newUser.getUserId().number());
            verify(userNotificationPublisher, times(1)).publishUserNotificationEvent(
                    newUser.getUserId().number(),
                    "Your account has been successfully created."
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
                    Optional.of(User.builder()
                            .userId(new PhoneNumber(userId))
                            .name(name)
                            .passwordHash(passwordHash)
                            .createdAt(Instant.now())
                            .build())
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

    @Nested
    @DisplayName("Change user details")
    class ChangeDetailsTests {
        @Test
        @DisplayName("Should update name when user is active and verified")
        void changeDetailsSuccess() {
            when(userRepository.findUserById(testUserId.number()))
                    .thenReturn(Optional.of(activeVerifiedUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User updated = userManagementService.changeUserDetails(testUserId.number(), "Jane");

            assertEquals("Jane", updated.getName());
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Should fail when user not verified")
        void changeDetailsNotVerified() {
            User notVerified = User.builder()
                    .userId(testUserId)
                    .name("John")
                    .passwordHash("hashed")
                    .verified(false)
                    .status(UserStatus.ACTIVE)
                    .build();

            when(userRepository.findUserById(testUserId.number()))
                    .thenReturn(Optional.of(notVerified));

            assertThrows(UserAccountNotVerifiedException.class,
                    () -> userManagementService.changeUserDetails(testUserId.number(), "Jane"));
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Change password")
    class ChangePasswordTests {
        @Test
        @DisplayName("Should update password when old matches")
        void changePasswordSuccess() {
            when(userRepository.findUserById(testUserId.number()))
                    .thenReturn(Optional.of(activeVerifiedUser));
            when(passwordEncoder.matches("oldPass123!", "hashed")).thenReturn(true);
            when(passwordEncoder.encode("newPass123!")).thenReturn("newHash");

            userManagementService.changePassword(testUserId.number(), "oldPass123!", "newPass123!");

            verify(userRepository, times(1)).save(any(User.class));
            verify(userNotificationPublisher, times(1))
                    .publishUserNotificationEvent(eq(testUserId.number()), anyString());
        }

        @Test
        @DisplayName("Should fail when old password is incorrect")
        void changePasswordInvalidOld() {
            when(userRepository.findUserById(testUserId.number()))
                    .thenReturn(Optional.of(activeVerifiedUser));
            when(passwordEncoder.matches("old", "hashed")).thenReturn(false);

            assertThrows(InvalidOldPasswordException.class,
                    () -> userManagementService.changePassword(testUserId.number(), "old", "newPass123"));
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Password reset code")
    class PasswordResetTests {
        @Test
        @DisplayName("Should request password reset code for active user")
        void requestResetCodeSuccess() {
            when(userRepository.findUserById(testUserId.number()))
                    .thenReturn(Optional.of(activeVerifiedUser));

            userManagementService.requestPasswordResetCode(testUserId.number());

            verify(passwordResetCodeService, times(1))
                    .generateCode(eq(testUserId));
        }

        @Test
        @DisplayName("Should change password using valid reset code")
        void changePasswordWithResetCodeSuccess() {
            when(userRepository.findUserById(testUserId.number()))
                    .thenReturn(Optional.of(activeVerifiedUser));
            when(passwordEncoder.encode("newPass123!")).thenReturn("newHash");

            userManagementService.changePasswordWithResetCode(testUserId.number(), "123456", "newPass123!");

            verify(passwordResetCodeService, times(1))
                    .verifyCode(eq(testUserId), eq("123456"));
            verify(userRepository, times(1)).save(any(User.class));
            verify(userNotificationPublisher, times(1))
                    .publishUserNotificationEvent(eq(testUserId.number()), anyString());
        }
    }

    @Nested
    @DisplayName("Verify user account")
    class VerifyUserTests {
        @Test
        @DisplayName("Should verify user using valid code")
        void verifyUserSuccess() {
            User pending = User.builder()
                    .userId(testUserId)
                    .name("John")
                    .passwordHash("hashed")
                    .verified(false)
                    .status(UserStatus.ACTIVE)
                    .build();

            when(userRepository.findUserById(testUserId.number()))
                    .thenReturn(Optional.of(pending));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            userManagementService.verifyUserAccount(testUserId.number(), "999999");

            verify(verificationCodeService, times(1))
                    .verifyCode(eq(testUserId), eq("999999"));
            verify(userRepository, times(1)).save(any(User.class));
            verify(userNotificationPublisher, times(1))
                    .publishUserNotificationEvent(eq(testUserId.number()), anyString());
        }

        @Test
        @DisplayName("Should fail to resend verification for verified user")
        void resendVerificationAlreadyVerified() {
            when(userRepository.findUserById(testUserId.number()))
                    .thenReturn(Optional.of(activeVerifiedUser));

            assertThrows(IllegalUserAccountStateException.class,
                    () -> userManagementService.resendVerificationCode(testUserId.number()));
            verify(verificationCodeService, never()).generateVerificationCode(any());
        }
    }

    @Nested
    @DisplayName("Concurrent User Creation Tests")
    class ConcurrentUserCreationTests {

        @BeforeEach
        void setUp() {
            userManagementService = new UserManagementService(
                    userRepository,
                    verificationCodeService,
                    passwordResetCodeService,
                    passwordEncoder,
                    auditedEventPublisher,
                    userNotificationPublisher,
                    userCreatedEventPublisher
            );
        }

        @Test
        @DisplayName("Should handle concurrent user creation attempts")
        void concurrentUserCreationHandling() throws InterruptedException {
            // given
            int numberOfThreads = 3;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(numberOfThreads);
            AtomicInteger successCount = new AtomicInteger(0);
            ConcurrentLinkedQueue<Throwable> unexpectedFailures = new ConcurrentLinkedQueue<>();

            // Setup mocks for successful user creation
            when(passwordEncoder.encode(anyString()))
                    .thenReturn("encodedPassword");

            when(userRepository.save(any(User.class)))
                    .thenAnswer(invocation -> {
                        User user = invocation.getArgument(0);
                        return User.builder()
                                .userId(user.getUserId())
                                .name(user.getName())
                                .passwordHash(user.getPasswordHash())
                                .status(UserStatus.ACTIVE)
                                .createdAt(TestFixtures.FIXED_INSTANT)
                                .build();
                    });

            // when
            ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

            for (int i = 0; i < numberOfThreads; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        
                        User createdUser = userManagementService.createNewUser(
                                TestFixtures.TEST_USER_ID + threadId,
                                TestFixtures.TEST_USER_NAME + threadId,
                                TestFixtures.TEST_SECURE_PASSWORD
                        );
                        
                        if (createdUser != null && createdUser.getUserId() != null) {
                            successCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        unexpectedFailures.add(e);
                    } finally {
                        endLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            endLatch.await();
            executor.shutdown();

            // then
            assertTrue(unexpectedFailures.isEmpty(),
                    () -> "Unexpected concurrent failure: " + unexpectedFailures.peek());
            assertEquals(numberOfThreads, successCount.get(), "All user creations should succeed");
        }
    }
}