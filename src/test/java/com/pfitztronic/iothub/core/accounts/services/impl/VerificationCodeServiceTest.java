package com.pfitztronic.iothub.core.accounts.services.impl;

import com.pfitztronic.iothub.core.accounts.models.PhoneNumber;
import com.pfitztronic.iothub.core.accounts.models.VerificationCode;
import com.pfitztronic.iothub.core.accounts.publishers.interfaces.IUserNotificationPublisher;
import com.pfitztronic.iothub.core.accounts.repositories.impl.VerificationCodeRepository;
import com.pfitztronic.iothub.core.accounts.services.AccountsConfigProperties;
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
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Verification Code Service Tests")
class VerificationCodeServiceTest {

    @Mock
    private VerificationCodeRepository verificationCodeRepository;
    @Mock
    private PasswordEncoderProxy passwordEncoder;
    @Mock
    private CodeGenerator codeGenerator;
    @Mock
    private IUserNotificationPublisher userNotificationPublisher;
    @Mock
    private AccountsConfigProperties accountsConfigProps;

    private VerificationCodeService verificationCodeService;
    private PhoneNumber testUserId;
    private String testGeneratedCode;
    private String testCodeHash;

    @BeforeEach
    void setUp() {
        verificationCodeService = new VerificationCodeService(
                verificationCodeRepository,
                passwordEncoder,
                codeGenerator,
                userNotificationPublisher,
                accountsConfigProps
        );

        testUserId = new PhoneNumber("+12345678901");
        testGeneratedCode = "987654";
        testCodeHash = "hashed987654";
    }

    @Nested
    @DisplayName("Generate Code")
    class GenerateCodeTests {
        @Test
        @DisplayName("Should generate and save verification code and publish notification")
        void generateCodeSuccess() {
            when(codeGenerator.generateCode()).thenReturn(testGeneratedCode);
            when(passwordEncoder.encode(testGeneratedCode)).thenReturn(testCodeHash);
            when(accountsConfigProps.verifyAccountDelayHours()).thenReturn(2);

            VerificationCode saved = VerificationCode.builder()
                    .id(UUID.randomUUID())
                    .userId(testUserId)
                    .codeHash(testCodeHash)
                    .expiresAt(Instant.now().plus(2, ChronoUnit.HOURS))
                    .createdAt(Instant.now())
                    .build();
            when(verificationCodeRepository.save(any(VerificationCode.class))).thenReturn(saved);

            VerificationCode result = verificationCodeService.generateVerificationCode(testUserId);

            assertNotNull(result);
            assertEquals(testUserId, result.getUserId());
            assertEquals(testCodeHash, result.getCodeHash());

            verify(verificationCodeRepository).clearCodesForUser(testUserId.number());
            verify(codeGenerator).generateCode();
            verify(passwordEncoder).encode(testGeneratedCode);
            verify(verificationCodeRepository).save(any(VerificationCode.class));
            verify(userNotificationPublisher).publishUserNotificationEvent(eq(testUserId.number()), contains(testGeneratedCode));
        }

        @Test
        @DisplayName("Should respect expiration from config")
        void respectConfiguredExpiration() {
            int configuredHours = 4;
            when(codeGenerator.generateCode()).thenReturn(testGeneratedCode);
            when(passwordEncoder.encode(testGeneratedCode)).thenReturn(testCodeHash);
            when(accountsConfigProps.verifyAccountDelayHours()).thenReturn(configuredHours);

            ArgumentCaptor<VerificationCode> captor = ArgumentCaptor.forClass(VerificationCode.class);
            when(verificationCodeRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            Instant before = Instant.now();
            verificationCodeService.generateVerificationCode(testUserId);
            Instant after = Instant.now();

            VerificationCode saved = captor.getValue();
            long secs = ChronoUnit.SECONDS.between(before, saved.getExpiresAt());
            long expected = configuredHours * 3600L;
            assertTrue(secs >= expected - 5 && secs <= expected + 5);
        }
    }

    @Nested
    @DisplayName("Verify Code")
    class VerifyCodeTests {
        @Test
        @DisplayName("Should verify valid code and delete it")
        void verifySuccess() {
            UUID id = UUID.randomUUID();
            VerificationCode stored = VerificationCode.builder()
                    .id(id)
                    .userId(testUserId)
                    .codeHash(testCodeHash)
                    .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                    .createdAt(Instant.now())
                    .build();
            when(verificationCodeRepository.findLatestByUserId(testUserId.number()))
                    .thenReturn(Optional.of(stored));
            when(passwordEncoder.matches("999999", testCodeHash)).thenReturn(true);

            assertDoesNotThrow(() -> verificationCodeService.verifyCode(testUserId, "999999"));

            verify(verificationCodeRepository).deleteById(id);
        }

        @Test
        @DisplayName("Should throw when no code found")
        void verifyNoCodeFound() {
            when(verificationCodeRepository.findLatestByUserId(testUserId.number()))
                    .thenReturn(Optional.empty());

            Exception ex = assertThrows(IllegalArgumentException.class,
                    () -> verificationCodeService.verifyCode(testUserId, "111111"));
            assertEquals("No verification code found for user", ex.getMessage());
            verify(verificationCodeRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Should throw when code expired and delete it")
        void verifyExpired() {
            UUID id = UUID.randomUUID();
            VerificationCode expired = VerificationCode.builder()
                    .id(id)
                    .userId(testUserId)
                    .codeHash(testCodeHash)
                    .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                    .createdAt(Instant.now().minus(2, ChronoUnit.HOURS))
                    .build();
            when(verificationCodeRepository.findLatestByUserId(testUserId.number()))
                    .thenReturn(Optional.of(expired));

            Exception ex = assertThrows(IllegalArgumentException.class,
                    () -> verificationCodeService.verifyCode(testUserId, "111111"));
            assertEquals("Verification code has expired", ex.getMessage());
            verify(verificationCodeRepository).deleteById(id);
            verify(passwordEncoder, never()).matches(any(), any());
        }

        @Test
        @DisplayName("Should throw when hash doesn't match")
        void verifyMismatch() {
            UUID id = UUID.randomUUID();
            VerificationCode stored = VerificationCode.builder()
                    .id(id)
                    .userId(testUserId)
                    .codeHash(testCodeHash)
                    .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                    .createdAt(Instant.now())
                    .build();
            when(verificationCodeRepository.findLatestByUserId(testUserId.number()))
                    .thenReturn(Optional.of(stored));
            when(passwordEncoder.matches("111111", testCodeHash)).thenReturn(false);

            Exception ex = assertThrows(IllegalArgumentException.class,
                    () -> verificationCodeService.verifyCode(testUserId, "111111"));
            assertEquals("Invalid verification code", ex.getMessage());
            verify(verificationCodeRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Should not match empty code")
        void verifyEmptyCode() {
            UUID id = UUID.randomUUID();
            VerificationCode stored = VerificationCode.builder()
                    .id(id)
                    .userId(testUserId)
                    .codeHash(testCodeHash)
                    .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                    .createdAt(Instant.now())
                    .build();
            when(verificationCodeRepository.findLatestByUserId(testUserId.number()))
                    .thenReturn(Optional.of(stored));
            when(passwordEncoder.matches("", testCodeHash)).thenReturn(false);

            Exception ex = assertThrows(IllegalArgumentException.class,
                    () -> verificationCodeService.verifyCode(testUserId, ""));
            assertEquals("Invalid verification code", ex.getMessage());
        }
    }
}
