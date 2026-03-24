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
@DisplayName("Password Reset Code Service Tests")
class PasswordResetCodeServiceTest {

    @Mock
    private PasswordResetCodeRepository passwordResetCodeRepository;
    @Mock
    private PasswordEncoderProxy passwordEncoder;
    @Mock
    private CodeGenerator codeGenerator;
    @Mock
    private IUserNotificationPublisher userNotificationPublisher;
    @Mock
    private AccountsConfigProperties accountsConfigProps;

    private PasswordResetCodeService passwordResetCodeService;
    private PhoneNumber testUserId;
    private String testGeneratedCode;
    private String testCodeHash;

    @BeforeEach
    public void setUp() {
        passwordResetCodeService = new PasswordResetCodeService(
                passwordResetCodeRepository,
                passwordEncoder,
                codeGenerator,
                userNotificationPublisher,
                accountsConfigProps
        );

        testUserId = new PhoneNumber("+12345678901");
        testGeneratedCode = "123456";
        testCodeHash = "hashedCode123456";
    }

    @Nested
    @DisplayName("Generate Code Tests")
    class GenerateCodeTests {

        @Test
        @DisplayName("Should successfully generate and save password reset code")
        void generateCodeSuccess() {
            // given
            when(codeGenerator.generateCode()).thenReturn(testGeneratedCode);
            when(passwordEncoder.encode(testGeneratedCode)).thenReturn(testCodeHash);
            when(accountsConfigProps.passwordResetDelayHours()).thenReturn(1);

            ArgumentCaptor<PasswordResetCode> captor = ArgumentCaptor.forClass(PasswordResetCode.class);
            PasswordResetCode savedCode = PasswordResetCode.builder()
                    .id(UUID.randomUUID())
                    .userId(testUserId)
                    .codeHash(testCodeHash)
                    .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                    .createdAt(Instant.now())
                    .build();
            when(passwordResetCodeRepository.save(any(PasswordResetCode.class))).thenReturn(savedCode);

            // when
            PasswordResetCode result = passwordResetCodeService.generateCode(testUserId);

            // then
            assertNotNull(result);
            assertEquals(testUserId, result.getUserId());
            assertEquals(testCodeHash, result.getCodeHash());
            assertNotNull(result.getExpiresAt());
            assertFalse(result.isExpired());

            // verify mocks
            verify(passwordResetCodeRepository, times(1)).clearCodesForUser(testUserId.number());
            verify(codeGenerator, times(1)).generateCode();
            verify(passwordEncoder, times(1)).encode(testGeneratedCode);
            verify(passwordResetCodeRepository, times(1)).save(argThat(code ->
                    code.getUserId().equals(testUserId) && code.getCodeHash().equals(testCodeHash)
            ));
            verify(userNotificationPublisher, times(1)).publishUserNotificationEvent(
                    testUserId.number(),
                    "Your password reset code is: " + testGeneratedCode
            );
        }

        @Test
        @DisplayName("Should clear existing codes before generating new one")
        void generateCodeClearsExistingCodes() {
            // given
            when(codeGenerator.generateCode()).thenReturn(testGeneratedCode);
            when(passwordEncoder.encode(testGeneratedCode)).thenReturn(testCodeHash);
            when(accountsConfigProps.passwordResetDelayHours()).thenReturn(2);
            when(passwordResetCodeRepository.save(any())).thenReturn(
                    PasswordResetCode.builder()
                            .id(UUID.randomUUID())
                            .userId(testUserId)
                            .codeHash(testCodeHash)
                            .expiresAt(Instant.now().plus(2, ChronoUnit.HOURS))
                            .createdAt(Instant.now())
                            .build()
            );

            // when
            passwordResetCodeService.generateCode(testUserId);

            // then
            verify(passwordResetCodeRepository, times(1)).clearCodesForUser(testUserId.number());
        }

        @Test
        @DisplayName("Should respect configured expiration hours")
        void generateCodeRespectConfiguredExpiration() {
            // given
            int configuredHours = 3;
            when(codeGenerator.generateCode()).thenReturn(testGeneratedCode);
            when(passwordEncoder.encode(testGeneratedCode)).thenReturn(testCodeHash);
            when(accountsConfigProps.passwordResetDelayHours()).thenReturn(configuredHours);

            ArgumentCaptor<PasswordResetCode> captor = ArgumentCaptor.forClass(PasswordResetCode.class);
            when(passwordResetCodeRepository.save(captor.capture())).thenReturn(
                PasswordResetCode.builder()
                    .id(UUID.randomUUID())
                    .userId(testUserId)
                    .codeHash(testCodeHash)
                    .expiresAt(Instant.now().plus(configuredHours, ChronoUnit.HOURS))
                    .createdAt(Instant.now())
                    .build()
            );

            // when
            Instant beforeGeneration = Instant.now();
            passwordResetCodeService.generateCode(testUserId);
            Instant afterGeneration = Instant.now();

            // then
            PasswordResetCode savedCode = captor.getValue();
            long secondsDifference = ChronoUnit.SECONDS.between(beforeGeneration, savedCode.getExpiresAt());
            long expectedSeconds = configuredHours * 3600L;
            assertTrue(secondsDifference >= expectedSeconds - 5 && secondsDifference <= expectedSeconds + 5);
        }
    }

    @Nested
    @DisplayName("Verify Code Tests")
    class VerifyCodeTests {

        @Test
        @DisplayName("Should successfully verify valid code")
        void verifyCodeSuccess() {
            // given
            String providedCode = "123456";
            UUID codeId = UUID.randomUUID();
            PasswordResetCode storedCode = PasswordResetCode.builder()
                    .id(codeId)
                    .userId(testUserId)
                    .codeHash(testCodeHash)
                    .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                    .createdAt(Instant.now())
                    .build();

            when(passwordResetCodeRepository.findLatestByUserId(testUserId.number()))
                    .thenReturn(Optional.of(storedCode));
            when(passwordEncoder.matches(providedCode, testCodeHash)).thenReturn(true);

            // when
            assertDoesNotThrow(() -> passwordResetCodeService.verifyCode(testUserId, providedCode));

            // then
            verify(passwordResetCodeRepository, times(1)).findLatestByUserId(testUserId.number());
            verify(passwordEncoder, times(1)).matches(providedCode, testCodeHash);
            verify(passwordResetCodeRepository, times(1)).deleteById(codeId);
        }

        @Test
        @DisplayName("Should throw when no code found for user")
        void verifyCodeNoCodeFound() {
            // given
            when(passwordResetCodeRepository.findLatestByUserId(testUserId.number()))
                    .thenReturn(Optional.empty());

            // when
            Exception exception = assertThrows(PasswordResetCodeNotFoundException.class, () ->
                    passwordResetCodeService.verifyCode(testUserId, testGeneratedCode)
            );

            // then
            assertEquals("No password reset code found for user", exception.getMessage());
            verify(passwordResetCodeRepository, times(1)).findLatestByUserId(testUserId.number());
            verify(passwordEncoder, never()).matches(any(), any());
            verify(passwordResetCodeRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Should throw when code has expired")
        void verifyCodeExpired() {
            // given
            UUID codeId = UUID.randomUUID();
            PasswordResetCode expiredCode = PasswordResetCode.builder()
                    .id(codeId)
                    .userId(testUserId)
                    .codeHash(testCodeHash)
                    .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                    .createdAt(Instant.now().minus(2, ChronoUnit.HOURS))
                    .build();

            when(passwordResetCodeRepository.findLatestByUserId(testUserId.number()))
                    .thenReturn(Optional.of(expiredCode));

            // when
            Exception exception = assertThrows(PasswordResetCodeExpiredException.class, () ->
                    passwordResetCodeService.verifyCode(testUserId, testGeneratedCode)
            );

            // then
            assertEquals("Password reset code has expired", exception.getMessage());
            verify(passwordResetCodeRepository, times(1)).findLatestByUserId(testUserId.number());
            verify(passwordEncoder, never()).matches(any(), any());
            verify(passwordResetCodeRepository, times(1)).deleteById(codeId);
        }

        @Test
        @DisplayName("Should throw when code hash doesn't match")
        void verifyCodeHashMismatch() {
            // given
            String providedCode = "654321";
            UUID codeId = UUID.randomUUID();
            PasswordResetCode storedCode = PasswordResetCode.builder()
                    .id(codeId)
                    .userId(testUserId)
                    .codeHash(testCodeHash)
                    .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                    .createdAt(Instant.now())
                    .build();

            when(passwordResetCodeRepository.findLatestByUserId(testUserId.number()))
                    .thenReturn(Optional.of(storedCode));
            when(passwordEncoder.matches(providedCode, testCodeHash)).thenReturn(false);

            // when
            Exception exception = assertThrows(InvalidPasswordResetCodeException.class, () ->
                    passwordResetCodeService.verifyCode(testUserId, providedCode)
            );

            // then
            assertEquals("Invalid password reset code", exception.getMessage());
            verify(passwordResetCodeRepository, times(1)).findLatestByUserId(testUserId.number());
            verify(passwordEncoder, times(1)).matches(providedCode, testCodeHash);
            verify(passwordResetCodeRepository, never()).deleteById(codeId);
        }

        @Test
        @DisplayName("Should delete code after successful verification")
        void verifyCodeDeletesAfterSuccess() {
            // given
            UUID codeId = UUID.randomUUID();
            PasswordResetCode storedCode = PasswordResetCode.builder()
                    .id(codeId)
                    .userId(testUserId)
                    .codeHash(testCodeHash)
                    .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                    .createdAt(Instant.now())
                    .build();

            when(passwordResetCodeRepository.findLatestByUserId(testUserId.number()))
                    .thenReturn(Optional.of(storedCode));
            when(passwordEncoder.matches(testGeneratedCode, testCodeHash)).thenReturn(true);

            // when
            passwordResetCodeService.verifyCode(testUserId, testGeneratedCode);

            // then
            verify(passwordResetCodeRepository, times(1)).deleteById(codeId);
        }

        @Test
        @DisplayName("Should not match against empty code")
        void verifyCodeEmptyCodeProvided() {
            // given
            UUID codeId = UUID.randomUUID();
            PasswordResetCode storedCode = PasswordResetCode.builder()
                    .id(codeId)
                    .userId(testUserId)
                    .codeHash(testCodeHash)
                    .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                    .createdAt(Instant.now())
                    .build();

            when(passwordResetCodeRepository.findLatestByUserId(testUserId.number()))
                    .thenReturn(Optional.of(storedCode));
            when(passwordEncoder.matches("", testCodeHash)).thenReturn(false);

            // when
            Exception exception = assertThrows(InvalidPasswordResetCodeException.class, () ->
                    passwordResetCodeService.verifyCode(testUserId, "")
            );

            // then
            assertEquals("Invalid password reset code", exception.getMessage());
        }
    }
}

