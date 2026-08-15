package com.pfitztronic.iothub.core.authentication.services.impl;

import com.pfitztronic.iothub.core.TestFixtures;
import com.pfitztronic.iothub.core.authentication.JwtProperties;
import com.pfitztronic.iothub.core.authentication.exceptions.UserAgentCredentialsInvalidException;
import com.pfitztronic.iothub.core.authentication.exceptions.UserAgentSessionExpiredException;
import com.pfitztronic.iothub.core.authentication.exceptions.UserAgentSessionRevokedException;
import com.pfitztronic.iothub.core.authentication.models.Session;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT Authentication Session Service Tests")
class JwtAuthenticationSessionServiceTest {

    @Mock
    private SessionManagementService sessionManagementService;

    private JwtAuthenticationSessionService jwtAuthenticationSessionService;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties(TestFixtures.TEST_SECRET, TestFixtures.JWT_EXPIRATION_SECONDS);
        jwtAuthenticationSessionService = new JwtAuthenticationSessionService(
                jwtProperties,
                sessionManagementService
        );
    }

    private SecretKey getTestSigningKey() {
        return Keys.hmacShaKeyFor(TestFixtures.TEST_SECRET.getBytes());
    }

    private String generateValidToken(UUID sessionId, String userId, String userAgent, Instant expiresAt) {
        return Jwts.builder()
                .subject(userId)
                .claim("sessionId", sessionId.toString())
                .claim("userAgent", userAgent)
                .issuedAt(Date.from(TestFixtures.FIXED_INSTANT))
                .expiration(Date.from(expiresAt))
                .signWith(getTestSigningKey())
                .compact();
    }

    @Nested
    @DisplayName("Generate Token Tests")
    class GenerateTokenTests {

        @Test
        @DisplayName("Should generate a valid JWT token for a user")
        void generateTokenSuccess() {
            // given
            UUID sessionId = UUID.randomUUID();
            Instant createdAt = TestFixtures.FIXED_INSTANT;
            Instant expiresAt = createdAt.plusSeconds(TestFixtures.JWT_EXPIRATION_SECONDS);

            Session session = new Session(
                    sessionId,
                    TestFixtures.TEST_USER_ID,
                    TestFixtures.TEST_USER_AGENT,
                    createdAt,
                    expiresAt,
                    null
            );

            when(sessionManagementService.createUserAgentSession(TestFixtures.TEST_USER_ID, TestFixtures.TEST_USER_AGENT, TestFixtures.JWT_EXPIRATION_SECONDS))
                    .thenReturn(session);

            // when
            String token = jwtAuthenticationSessionService.generateToken(TestFixtures.TEST_USER_ID, TestFixtures.TEST_USER_AGENT);

            // then - verify token structure (JWT format: header.payload.signature)
            assertNotNull(token);
            assertFalse(token.isBlank(), "Token should not be blank");
            String[] parts = token.split("\\.");
            assertEquals(3, parts.length, "JWT token should have 3 parts (header.payload.signature)");
            assertTrue(parts[0].length() > 0, "Token header should not be empty");
            assertTrue(parts[1].length() > 0, "Token payload should not be empty");
            assertTrue(parts[2].length() > 0, "Token signature should not be empty");
            verify(sessionManagementService, times(1))
                    .createUserAgentSession(TestFixtures.TEST_USER_ID, TestFixtures.TEST_USER_AGENT, TestFixtures.JWT_EXPIRATION_SECONDS);
        }

        @Test
        @DisplayName("Should create session with correct duration")
        void generateTokenCreatesSessionWithCorrectDuration() {
            // given
            UUID sessionId = UUID.randomUUID();
            Instant createdAt = TestFixtures.FIXED_INSTANT;
            Instant expiresAt = createdAt.plusSeconds(TestFixtures.JWT_EXPIRATION_SECONDS);

            Session session = new Session(
                    sessionId,
                    TestFixtures.TEST_USER_ID,
                    TestFixtures.TEST_USER_AGENT,
                    createdAt,
                    expiresAt,
                    null
            );

            when(sessionManagementService.createUserAgentSession(TestFixtures.TEST_USER_ID, TestFixtures.TEST_USER_AGENT, TestFixtures.JWT_EXPIRATION_SECONDS))
                    .thenReturn(session);

            // when
            jwtAuthenticationSessionService.generateToken(TestFixtures.TEST_USER_ID, TestFixtures.TEST_USER_AGENT);

            // then
            verify(sessionManagementService).createUserAgentSession(
                    eq(TestFixtures.TEST_USER_ID),
                    eq(TestFixtures.TEST_USER_AGENT),
                    eq(TestFixtures.JWT_EXPIRATION_SECONDS)
            );
        }
    }

    @Nested
    @DisplayName("Authenticate Token Tests")
    class AuthenticateTokenTests {

        @Test
        @DisplayName("Should authenticate a valid token successfully")
        void authenticateValidTokenSuccess() {
            // given
            UUID sessionId = UUID.randomUUID();
            Instant createdAt = TestFixtures.FIXED_INSTANT;
            Instant expiresAt = createdAt.plusSeconds(TestFixtures.JWT_EXPIRATION_SECONDS);

            Session session = new Session(
                    sessionId,
                    TestFixtures.TEST_USER_ID,
                    TestFixtures.TEST_USER_AGENT,
                    createdAt,
                    expiresAt,
                    null
            );

            String token = generateValidToken(sessionId, TestFixtures.TEST_USER_ID, TestFixtures.TEST_USER_AGENT, expiresAt);

            when(sessionManagementService.findCurrentUserAgentSession(sessionId, TestFixtures.TEST_USER_ID, TestFixtures.TEST_USER_AGENT))
                    .thenReturn(session);

            // when
            String authenticatedUserId = jwtAuthenticationSessionService.authenticate(token, TestFixtures.TEST_USER_AGENT);

            // then
            assertEquals(TestFixtures.TEST_USER_ID, authenticatedUserId);
            verify(sessionManagementService, times(1))
                    .findCurrentUserAgentSession(sessionId, TestFixtures.TEST_USER_ID, TestFixtures.TEST_USER_AGENT);
        }

        @Test
        @DisplayName("Should throw exception for null token")
        void authenticateNullTokenThrowsException() {
            // when & then
            RuntimeException exception = assertThrows(UserAgentCredentialsInvalidException.class,
                    () -> jwtAuthenticationSessionService.authenticate(null, TestFixtures.TEST_USER_AGENT));

            assertEquals("Invalid token", exception.getMessage());
            verifyNoInteractions(sessionManagementService);
        }

        @Test
        @DisplayName("Should throw exception for empty token")
        void authenticateEmptyTokenThrowsException() {
            // when & then
            RuntimeException exception = assertThrows(UserAgentCredentialsInvalidException.class,
                    () -> jwtAuthenticationSessionService.authenticate("", TestFixtures.TEST_USER_AGENT));

            assertEquals("Invalid token", exception.getMessage());
            verifyNoInteractions(sessionManagementService);
        }

        @Test
        @DisplayName("Should throw UserAgentCredentialsInvalidException for malformed token")
        void authenticateMalformedTokenThrowsException() {
            // given
            String malformedToken = "not.a.valid.jwt.token";

            // when & then
            assertThrows(UserAgentCredentialsInvalidException.class,
                    () -> jwtAuthenticationSessionService.authenticate(malformedToken, TestFixtures.TEST_USER_AGENT));

            verifyNoInteractions(sessionManagementService);
        }

        @Test
        @DisplayName("Should throw exception for token with invalid signature")
        void authenticateTokenWithInvalidSignatureThrowsException() {
            // given
            String differentSecret = "differentSecretKeyForTestingPurposesThatIsLongEnough!";
            SecretKey differentKey = Keys.hmacShaKeyFor(differentSecret.getBytes());

            String tokenWithInvalidSignature = Jwts.builder()
                    .subject(TestFixtures.TEST_USER_ID)
                    .claim("sessionId", UUID.randomUUID().toString())
                    .claim("userAgent", TestFixtures.TEST_USER_AGENT)
                    .issuedAt(Date.from(TestFixtures.FIXED_INSTANT))
                    .expiration(Date.from(TestFixtures.FIXED_INSTANT.plusSeconds(TestFixtures.JWT_EXPIRATION_SECONDS)))
                    .signWith(differentKey)
                    .compact();

            // when & then
            assertThrows(Exception.class,
                    () -> jwtAuthenticationSessionService.authenticate(tokenWithInvalidSignature, TestFixtures.TEST_USER_AGENT));

            verifyNoInteractions(sessionManagementService);
        }
    }

    @Nested
    @DisplayName("Session Validation Tests")
    class SessionValidationTests {

        @Test
        @DisplayName("Should reject a valid token replayed with a different user agent")
        void authenticateTokenWithDifferentUserAgentThrowsException() {
            UUID sessionId = UUID.randomUUID();
            Instant expiresAt = TestFixtures.FIXED_INSTANT.plusSeconds(TestFixtures.JWT_EXPIRATION_SECONDS);
            String token = generateValidToken(
                    sessionId,
                    TestFixtures.TEST_USER_ID,
                    TestFixtures.TEST_USER_AGENT,
                    expiresAt
            );

            UserAgentCredentialsInvalidException exception = assertThrows(
                    UserAgentCredentialsInvalidException.class,
                    () -> jwtAuthenticationSessionService.authenticate(token, TestFixtures.TEST_USER_AGENT_2)
            );

            assertEquals("User agent does not match token", exception.getMessage());
            verifyNoInteractions(sessionManagementService);
        }

        @Test
        @DisplayName("Should throw UserAgentSessionExpiredException for expired session")
        void authenticateExpiredSessionThrowsException() {
            // given
            UUID sessionId = UUID.randomUUID();
            // Use Instant.now() for expiration since the implementation checks against current time
            Instant expiresAt = Instant.now().minusSeconds(3600); // Expired 1 hour ago
            Instant createdAt = expiresAt.minusSeconds(7200); // Created 2 hours before expiry

            Session expiredSession = new Session(
                    sessionId,
                    TestFixtures.TEST_USER_ID,
                    TestFixtures.TEST_USER_AGENT,
                    createdAt,
                    expiresAt,
                    null
            );

            // Generate token with future expiration (JWT level) but session is expired
            String token = generateValidToken(sessionId, TestFixtures.TEST_USER_ID, TestFixtures.TEST_USER_AGENT,
                    Instant.now().plusSeconds(TestFixtures.JWT_EXPIRATION_SECONDS));

            when(sessionManagementService.findCurrentUserAgentSession(sessionId, TestFixtures.TEST_USER_ID, TestFixtures.TEST_USER_AGENT))
                    .thenReturn(expiredSession);

            // when & then
            UserAgentSessionExpiredException exception = assertThrows(UserAgentSessionExpiredException.class,
                    () -> jwtAuthenticationSessionService.authenticate(token, TestFixtures.TEST_USER_AGENT));

            assertEquals("Token has expired", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw UserAgentSessionRevokedException for revoked session")
        void authenticateRevokedSessionThrowsException() {
            // given
            UUID sessionId = UUID.randomUUID();
            Instant createdAt = TestFixtures.FIXED_INSTANT;
            Instant expiresAt = createdAt.plusSeconds(TestFixtures.JWT_EXPIRATION_SECONDS);
            Instant revokedAt = TestFixtures.FIXED_INSTANT.minusSeconds(60); // Revoked 1 minute ago

            Session revokedSession = new Session(
                    sessionId,
                    TestFixtures.TEST_USER_ID,
                    TestFixtures.TEST_USER_AGENT,
                    createdAt,
                    expiresAt,
                    revokedAt
            );

            String token = generateValidToken(sessionId, TestFixtures.TEST_USER_ID, TestFixtures.TEST_USER_AGENT, expiresAt);

            when(sessionManagementService.findCurrentUserAgentSession(sessionId, TestFixtures.TEST_USER_ID, TestFixtures.TEST_USER_AGENT))
                    .thenReturn(revokedSession);

            // when & then
            UserAgentSessionRevokedException exception = assertThrows(UserAgentSessionRevokedException.class,
                    () -> jwtAuthenticationSessionService.authenticate(token, TestFixtures.TEST_USER_AGENT));

            assertEquals("Token has been revoked", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for invalid session ID format in token")
        void authenticateInvalidSessionIdFormatThrowsException() {
            // given
            String tokenWithInvalidSessionId = Jwts.builder()
                    .subject(TestFixtures.TEST_USER_ID)
                    .claim("sessionId", "not-a-valid-uuid")
                    .claim("userAgent", TestFixtures.TEST_USER_AGENT)
                    .issuedAt(Date.from(TestFixtures.FIXED_INSTANT))
                    .expiration(Date.from(TestFixtures.FIXED_INSTANT.plusSeconds(TestFixtures.JWT_EXPIRATION_SECONDS)))
                    .signWith(getTestSigningKey())
                    .compact();

            // when & then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> jwtAuthenticationSessionService.authenticate(tokenWithInvalidSessionId, TestFixtures.TEST_USER_AGENT));

            assertEquals("Invalid session ID format", exception.getMessage());
            verifyNoInteractions(sessionManagementService);
        }
    }

    @Nested
    @DisplayName("Token Round Trip Tests")
    class TokenRoundTripTests {

        @Test
        @DisplayName("Should successfully authenticate a token that was just generated")
        void generateAndAuthenticateTokenSuccessfully() {
            // given
            UUID sessionId = UUID.randomUUID();
            Instant createdAt = TestFixtures.FIXED_INSTANT;
            Instant expiresAt = createdAt.plusSeconds(TestFixtures.JWT_EXPIRATION_SECONDS);

            Session session = new Session(
                    sessionId,
                    TestFixtures.TEST_USER_ID,
                    TestFixtures.TEST_USER_AGENT,
                    createdAt,
                    expiresAt,
                    null
            );

            when(sessionManagementService.createUserAgentSession(TestFixtures.TEST_USER_ID, TestFixtures.TEST_USER_AGENT, TestFixtures.JWT_EXPIRATION_SECONDS))
                    .thenReturn(session);
            when(sessionManagementService.findCurrentUserAgentSession(sessionId, TestFixtures.TEST_USER_ID, TestFixtures.TEST_USER_AGENT))
                    .thenReturn(session);

            // when
            String token = jwtAuthenticationSessionService.generateToken(TestFixtures.TEST_USER_ID, TestFixtures.TEST_USER_AGENT);
            String authenticatedUserId = jwtAuthenticationSessionService.authenticate(token, TestFixtures.TEST_USER_AGENT);

            // then
            assertEquals(TestFixtures.TEST_USER_ID, authenticatedUserId);
            verify(sessionManagementService, times(1))
                    .createUserAgentSession(TestFixtures.TEST_USER_ID, TestFixtures.TEST_USER_AGENT, TestFixtures.JWT_EXPIRATION_SECONDS);
            verify(sessionManagementService, times(1))
                    .findCurrentUserAgentSession(sessionId, TestFixtures.TEST_USER_ID, TestFixtures.TEST_USER_AGENT);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle session with exact current time as expiry")
        void authenticateSessionExpiringNow() {
            // given
            UUID sessionId = UUID.randomUUID();
            // Use Instant.now() for expiration since the implementation checks against current time
            Instant expiresAt = Instant.now().minusNanos(1); // Just expired
            Instant createdAt = expiresAt.minusSeconds(TestFixtures.JWT_EXPIRATION_SECONDS);

            Session justExpiredSession = new Session(
                    sessionId,
                    TestFixtures.TEST_USER_ID,
                    TestFixtures.TEST_USER_AGENT,
                    createdAt,
                    expiresAt,
                    null
            );

            String token = generateValidToken(sessionId, TestFixtures.TEST_USER_ID, TestFixtures.TEST_USER_AGENT,
                    Instant.now().plusSeconds(TestFixtures.JWT_EXPIRATION_SECONDS));

            when(sessionManagementService.findCurrentUserAgentSession(sessionId, TestFixtures.TEST_USER_ID, TestFixtures.TEST_USER_AGENT))
                    .thenReturn(justExpiredSession);

            // when & then
            assertThrows(UserAgentSessionExpiredException.class,
                    () -> jwtAuthenticationSessionService.authenticate(token, TestFixtures.TEST_USER_AGENT));
        }

        @Test
        @DisplayName("Should authenticate different users with different tokens")
        void authenticateDifferentUsersWithDifferentTokens() {
            // given
            String userId1 = "+12345678901";
            String userId2 = "+10987654321";
            String userAgent1 = "Agent1";
            String userAgent2 = "Agent2";

            UUID sessionId1 = UUID.randomUUID();
            UUID sessionId2 = UUID.randomUUID();
            Instant createdAt = TestFixtures.FIXED_INSTANT;
            Instant expiresAt = createdAt.plusSeconds(TestFixtures.JWT_EXPIRATION_SECONDS);

            Session session1 = new Session(sessionId1, userId1, userAgent1, createdAt, expiresAt, null);
            Session session2 = new Session(sessionId2, userId2, userAgent2, createdAt, expiresAt, null);

            String token1 = generateValidToken(sessionId1, userId1, userAgent1, expiresAt);
            String token2 = generateValidToken(sessionId2, userId2, userAgent2, expiresAt);

            when(sessionManagementService.findCurrentUserAgentSession(sessionId1, userId1, userAgent1))
                    .thenReturn(session1);
            when(sessionManagementService.findCurrentUserAgentSession(sessionId2, userId2, userAgent2))
                    .thenReturn(session2);

            // when
            String authenticatedUser1 = jwtAuthenticationSessionService.authenticate(token1, userAgent1);
            String authenticatedUser2 = jwtAuthenticationSessionService.authenticate(token2, userAgent2);

            // then
            assertEquals(userId1, authenticatedUser1);
            assertEquals(userId2, authenticatedUser2);
        }
    }
}
