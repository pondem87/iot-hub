package com.pfitztronic.iothub.core.accounts.services.impl;

import com.pfitztronic.iothub.core.TestFixtures;
import com.pfitztronic.iothub.core.accounts.dto.CreateNewAccountData;
import com.pfitztronic.iothub.core.accounts.dto.NewAccountResponse;
import com.pfitztronic.iothub.core.accounts.exceptions.AccountNameAlreadyExistsException;
import com.pfitztronic.iothub.core.accounts.models.Account;
import com.pfitztronic.iothub.core.accounts.models.AccountName;
import com.pfitztronic.iothub.core.accounts.models.PhoneNumber;
import com.pfitztronic.iothub.core.accounts.models.User;
import com.pfitztronic.iothub.core.accounts.publishers.interfaces.*;
import com.pfitztronic.iothub.core.accounts.repositories.impl.AccountRepository;
import com.pfitztronic.iothub.core.accounts.services.AccountsConfigProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

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
    @Mock
    private AccountsConfigProperties accountsConfigProps;

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
                    accountDeletedEventPublisher,
                    accountsConfigProps
            );
        }


        @Test
        public void createNewAccountSuccess() {
            // given
            String name = TestFixtures.TEST_USER_NAME;
            String userId = TestFixtures.TEST_USER_ID;
            String password = TestFixtures.TEST_SECURE_PASSWORD;

            User savedUser = TestFixtures.createTestUser(userId, name, TestFixtures.TEST_PASSWORD_HASH);

            String accountName = TestFixtures.TEST_ACCOUNT_NAME;
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
                                    .createdAt(TestFixtures.FIXED_INSTANT)
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
            String name = TestFixtures.TEST_USER_NAME;
            String userId = TestFixtures.TEST_USER_ID;
            String password = TestFixtures.TEST_SECURE_PASSWORD;
            
            User savedUser = TestFixtures.createTestUser(userId, name, TestFixtures.TEST_PASSWORD_HASH);

            String accountName = TestFixtures.TEST_ACCOUNT_NAME;

            Account existingAccount = Account.builder()
                    .accountId(UUID.randomUUID())
                    .accountName(new AccountName(accountName))
                    .adminId(savedUser.getUserId())
                    .createdAt(TestFixtures.FIXED_INSTANT)
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

        @Test
        @DisplayName("Should propagate notification failure after saving account")
        public void createAccountWithNotificationFailure() {
            // given
            String name = TestFixtures.TEST_USER_NAME_2;
            String userId = TestFixtures.TEST_PHONE_NUMBER_2;
            String password = TestFixtures.TEST_SECURE_PASSWORD;

            User savedUser = TestFixtures.createTestUser(userId, name, TestFixtures.TEST_PASSWORD_HASH);
            String accountName = TestFixtures.TEST_ACCOUNT_NAME_2;
            UUID accountId = UUID.randomUUID();

            when(userManagementService.createNewUser(userId, name, password))
                    .thenReturn(savedUser);

            when(accountRepository.findOneByName(accountName))
                    .thenReturn(null);

            when(accountRepository.save(any(Account.class)))
                    .thenReturn(Account.builder()
                            .accountId(accountId)
                            .accountName(new AccountName(accountName))
                            .adminId(savedUser.getUserId())
                            .createdAt(TestFixtures.FIXED_INSTANT)
                            .build());

            // Notification publisher throws exception
            doThrow(new RuntimeException("Notification service failed"))
                    .when(userNotificationPublisher)
                    .publishUserNotificationEvent(anyString(), anyString());

            // when & then
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> accountManagementService.createNewAccount(
                            new CreateNewAccountData(userId, name, password, accountName)
                    )
            );
            assertEquals("Notification service failed", exception.getMessage());

            verify(accountRepository).save(any(Account.class));
            verify(userNotificationPublisher).publishUserNotificationEvent(anyString(), anyString());
        }

        @Test
        @DisplayName("Should propagate audit event failure after saving account")
        public void createAccountWithAuditEventFailure() {
            // given
            String name = TestFixtures.TEST_USER_NAME;
            String userId = TestFixtures.TEST_USER_ID;
            String password = TestFixtures.TEST_SECURE_PASSWORD;

            User savedUser = TestFixtures.createTestUser(userId, name, TestFixtures.TEST_PASSWORD_HASH);
            String accountName = TestFixtures.TEST_ACCOUNT_NAME;
            UUID accountId = UUID.randomUUID();

            when(userManagementService.createNewUser(userId, name, password))
                    .thenReturn(savedUser);

            when(accountRepository.findOneByName(accountName))
                    .thenReturn(null);

            when(accountRepository.save(any(Account.class)))
                    .thenReturn(Account.builder()
                            .accountId(accountId)
                            .accountName(new AccountName(accountName))
                            .adminId(savedUser.getUserId())
                            .createdAt(TestFixtures.FIXED_INSTANT)
                            .build());

            // Audit event publisher throws exception
            doThrow(new RuntimeException("Audit service failed"))
                    .when(auditedEventPublisher)
                    .publishAuditedEvent(any(AuditedEvent.class));

            // when & then
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> accountManagementService.createNewAccount(
                            new CreateNewAccountData(userId, name, password, accountName)
                    )
            );
            assertEquals("Audit service failed", exception.getMessage());

            verify(accountRepository).save(any(Account.class));
            verify(auditedEventPublisher).publishAuditedEvent(any(AuditedEvent.class));
        }
    }

    @Nested
    @DisplayName("Concurrent Account Creation Tests")
    class ConcurrentAccountCreationTests {

        @BeforeEach
        public void setup() {
            accountManagementService = new AccountManagementService(
                    accountRepository,
                    userManagementService,
                    userNotificationPublisher,
                    accountCreatedEventPublisher,
                    accountStatusChangedEventPublisher,
                    auditedEventPublisher,
                    accountDeletedEventPublisher,
                    accountsConfigProps
            );
        }

        @Test
        @DisplayName("Should handle multiple concurrent account creation attempts")
        public void concurrentAccountCreationHandling() throws InterruptedException {
            // given
            int numberOfThreads = 3;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(numberOfThreads);
            AtomicInteger successCount = new AtomicInteger(0);
            ConcurrentLinkedQueue<Throwable> unexpectedFailures = new ConcurrentLinkedQueue<>();

            String accountName = TestFixtures.TEST_ACCOUNT_NAME;
            User savedUser = TestFixtures.createTestUser();

            // Setup mock to simulate concurrent access - first two succeed, third gets duplicate error
            when(accountRepository.findOneByName(accountName))
                    .thenReturn(null)
                    .thenReturn(null)
                    .thenReturn(Account.builder()
                            .accountId(UUID.randomUUID())
                            .accountName(new AccountName(accountName))
                            .adminId(savedUser.getUserId())
                            .createdAt(TestFixtures.FIXED_INSTANT)
                            .build());

            when(userManagementService.createNewUser(anyString(), anyString(), anyString()))
                    .thenReturn(savedUser);

            when(accountRepository.save(any(Account.class)))
                    .thenReturn(Account.builder()
                            .accountId(UUID.randomUUID())
                            .accountName(new AccountName(accountName))
                            .adminId(savedUser.getUserId())
                            .createdAt(TestFixtures.FIXED_INSTANT)
                            .build());

            // when
            ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

            for (int i = 0; i < numberOfThreads; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        accountManagementService.createNewAccount(
                                new CreateNewAccountData(
                                        TestFixtures.TEST_USER_ID + threadId,
                                        TestFixtures.TEST_USER_NAME + threadId,
                                        TestFixtures.TEST_SECURE_PASSWORD,
                                        accountName
                                )
                        );
                        successCount.incrementAndGet();
                    } catch (AccountNameAlreadyExistsException e) {
                        // Expected for some threads
                    } catch (Exception e) {
                        unexpectedFailures.add(e);
                    } finally {
                        endLatch.countDown();
                    }
                });
            }

            // Release all threads at the same time
            startLatch.countDown();
            endLatch.await();
            executor.shutdown();

            // then
            assertTrue(unexpectedFailures.isEmpty(),
                    () -> "Unexpected concurrent failure: " + unexpectedFailures.peek());
            assertEquals(2, successCount.get(), "Exactly two account creations should succeed");
        }
    }
}