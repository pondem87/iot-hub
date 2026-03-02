package com.pfitztronic.iothub.core.authentication.services.impl;

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

    private static final String TEST_SECRET = "mySecretKeyForTestingPurposesThatIsLongEnough256Bits!";
    private static final long TEST_EXPIRATION = 3600L; // 1 hour in seconds
    private static final String TEST_USER_ID = "+12345678901";
    private static final String TEST_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";

    @Mock
    private SessionManagementService sessionManagementService;

    private JwtAuthenticationSessionService jwtAuthenticationSessionService;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties(TEST_SECRET, TEST_EXPIRATION);
        jwtAuthenticationSessionService = new JwtAuthenticationSessionService(
                jwtProperties,
                sessionManagementService
        );
    }

    private SecretKey getTestSigningKey() {
        return Keys.hmacShaKeyFor(TEST_SECRET.getBytes());
    }

    private String generateValidToken(UUID sessionId, String userId, String userAgent, Instant expiresAt) {
        return Jwts.builder()
                .subject(userId)
                .claim("sessionId", sessionId.toString())
                .claim("userAgent", userAgent)
                .issuedAt(Date.from(Instant.now()))
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
            Instant createdAt = Instant.now();
            Instant expiresAt = createdAt.plusSeconds(TEST_EXPIRATION);

            Session session = new Session(
                    sessionId,
                    TEST_USER_ID,
                    TEST_USER_AGENT,
                    createdAt,
                    expiresAt,
                    null
            );

            when(sessionManagementService.createUserAgentSession(TEST_USER_ID, TEST_USER_AGENT, TEST_EXPIRATION))
                    .thenReturn(session);

            // when
            String token = jwtAuthenticationSessionService.generateToken(TEST_USER_ID, TEST_USER_AGENT);

            // then
            assertNotNull(token);
            assertFalse(token.isEmpty());
            verify(sessionManagementService, times(1))
                    .createUserAgentSession(TEST_USER_ID, TEST_USER_AGENT, TEST_EXPIRATION);
        }

        @Test
        @DisplayName("Should create session with correct duration")
        void generateTokenCreatesSessionWithCorrectDuration() {
            // given
            UUID sessionId = UUID.randomUUID();
            Instant createdAt = Instant.now();
            Instant expiresAt = createdAt.plusSeconds(TEST_EXPIRATION);

            Session session = new Session(
                    sessionId,
                    TEST_USER_ID,
                    TEST_USER_AGENT,
                    createdAt,
                    expiresAt,
                    null
            );

            when(sessionManagementService.createUserAgentSession(TEST_USER_ID, TEST_USER_AGENT, TEST_EXPIRATION))
                    .thenReturn(session);

            // when
            jwtAuthenticationSessionService.generateToken(TEST_USER_ID, TEST_USER_AGENT);

            // then
            verify(sessionManagementService).createUserAgentSession(
                    eq(TEST_USER_ID),
                    eq(TEST_USER_AGENT),
                    eq(TEST_EXPIRATION)
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
            Instant createdAt = Instant.now();
            Instant expiresAt = createdAt.plusSeconds(TEST_EXPIRATION);

            Session session = new Session(
                    sessionId,
                    TEST_USER_ID,
                    TEST_USER_AGENT,
                    createdAt,
                    expiresAt,
                    null
            );

            String token = generateValidToken(sessionId, TEST_USER_ID, TEST_USER_AGENT, expiresAt);

            when(sessionManagementService.findCurrentUserAgentSession(sessionId, TEST_USER_ID, TEST_USER_AGENT))
                    .thenReturn(session);

            // when
            String authenticatedUserId = jwtAuthenticationSessionService.authenticate(token);

            // then
            assertEquals(TEST_USER_ID, authenticatedUserId);
            verify(sessionManagementService, times(1))
                    .findCurrentUserAgentSession(sessionId, TEST_USER_ID, TEST_USER_AGENT);
        }

        @Test
        @DisplayName("Should throw exception for null token")
        void authenticateNullTokenThrowsException() {
            // when & then
            RuntimeException exception = assertThrows(UserAgentCredentialsInvalidException.class,
                    () -> jwtAuthenticationSessionService.authenticate(null));

            assertEquals("Invalid token", exception.getMessage());
            verifyNoInteractions(sessionManagementService);
        }

        @Test
        @DisplayName("Should throw exception for empty token")
        void authenticateEmptyTokenThrowsException() {
            // when & then
            RuntimeException exception = assertThrows(UserAgentCredentialsInvalidException.class,
                    () -> jwtAuthenticationSessionService.authenticate(""));

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
                    () -> jwtAuthenticationSessionService.authenticate(malformedToken));

            verifyNoInteractions(sessionManagementService);
        }

        @Test
        @DisplayName("Should throw exception for token with invalid signature")
        void authenticateTokenWithInvalidSignatureThrowsException() {
            // given
            String differentSecret = "differentSecretKeyForTestingPurposesThatIsLongEnough!";
            SecretKey differentKey = Keys.hmacShaKeyFor(differentSecret.getBytes());

            String tokenWithInvalidSignature = Jwts.builder()
                    .subject(TEST_USER_ID)
                    .claim("sessionId", UUID.randomUUID().toString())
                    .claim("userAgent", TEST_USER_AGENT)
                    .issuedAt(Date.from(Instant.now()))
                    .expiration(Date.from(Instant.now().plusSeconds(TEST_EXPIRATION)))
                    .signWith(differentKey)
                    .compact();

            // when & then
            assertThrows(Exception.class,
                    () -> jwtAuthenticationSessionService.authenticate(tokenWithInvalidSignature));

            verifyNoInteractions(sessionManagementService);
        }
    }

    @Nested
    @DisplayName("Session Validation Tests")
    class SessionValidationTests {

        @Test
        @DisplayName("Should throw UserAgentSessionExpiredException for expired session")
        void authenticateExpiredSessionThrowsException() {
            // given
            UUID sessionId = UUID.randomUUID();
            Instant createdAt = Instant.now().minusSeconds(7200); // 2 hours ago
            Instant expiresAt = Instant.now().minusSeconds(3600); // 1 hour ago (expired)

            Session expiredSession = new Session(
                    sessionId,
                    TEST_USER_ID,
                    TEST_USER_AGENT,
                    createdAt,
                    expiresAt,
                    null
            );

            // Generate token with future expiration (JWT level) but session is expired
            String token = generateValidToken(sessionId, TEST_USER_ID, TEST_USER_AGENT,
                    Instant.now().plusSeconds(TEST_EXPIRATION));

            when(sessionManagementService.findCurrentUserAgentSession(sessionId, TEST_USER_ID, TEST_USER_AGENT))
                    .thenReturn(expiredSession);

            // when & then
            UserAgentSessionExpiredException exception = assertThrows(UserAgentSessionExpiredException.class,
                    () -> jwtAuthenticationSessionService.authenticate(token));

            assertEquals("Token has expired", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw UserAgentSessionRevokedException for revoked session")
        void authenticateRevokedSessionThrowsException() {
            // given
            UUID sessionId = UUID.randomUUID();
            Instant createdAt = Instant.now();
            Instant expiresAt = createdAt.plusSeconds(TEST_EXPIRATION);
            Instant revokedAt = Instant.now().minusSeconds(60); // Revoked 1 minute ago

            Session revokedSession = new Session(
                    sessionId,
                    TEST_USER_ID,
                    TEST_USER_AGENT,
                    createdAt,
                    expiresAt,
                    revokedAt
            );

            String token = generateValidToken(sessionId, TEST_USER_ID, TEST_USER_AGENT, expiresAt);

            when(sessionManagementService.findCurrentUserAgentSession(sessionId, TEST_USER_ID, TEST_USER_AGENT))
                    .thenReturn(revokedSession);

            // when & then
            UserAgentSessionRevokedException exception = assertThrows(UserAgentSessionRevokedException.class,
                    () -> jwtAuthenticationSessionService.authenticate(token));

            assertEquals("Token has been revoked", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for invalid session ID format in token")
        void authenticateInvalidSessionIdFormatThrowsException() {
            // given
            String tokenWithInvalidSessionId = Jwts.builder()
                    .subject(TEST_USER_ID)
                    .claim("sessionId", "not-a-valid-uuid")
                    .claim("userAgent", TEST_USER_AGENT)
                    .issuedAt(Date.from(Instant.now()))
                    .expiration(Date.from(Instant.now().plusSeconds(TEST_EXPIRATION)))
                    .signWith(getTestSigningKey())
                    .compact();

            // when & then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> jwtAuthenticationSessionService.authenticate(tokenWithInvalidSessionId));

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
            Instant createdAt = Instant.now();
            Instant expiresAt = createdAt.plusSeconds(TEST_EXPIRATION);

            Session session = new Session(
                    sessionId,
                    TEST_USER_ID,
                    TEST_USER_AGENT,
                    createdAt,
                    expiresAt,
                    null
            );

            when(sessionManagementService.createUserAgentSession(TEST_USER_ID, TEST_USER_AGENT, TEST_EXPIRATION))
                    .thenReturn(session);
            when(sessionManagementService.findCurrentUserAgentSession(sessionId, TEST_USER_ID, TEST_USER_AGENT))
                    .thenReturn(session);

            // when
            String token = jwtAuthenticationSessionService.generateToken(TEST_USER_ID, TEST_USER_AGENT);
            String authenticatedUserId = jwtAuthenticationSessionService.authenticate(token);

            // then
            assertEquals(TEST_USER_ID, authenticatedUserId);
            verify(sessionManagementService, times(1))
                    .createUserAgentSession(TEST_USER_ID, TEST_USER_AGENT, TEST_EXPIRATION);
            verify(sessionManagementService, times(1))
                    .findCurrentUserAgentSession(sessionId, TEST_USER_ID, TEST_USER_AGENT);
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
            Instant createdAt = Instant.now().minusSeconds(TEST_EXPIRATION);
            Instant expiresAt = Instant.now().minusNanos(1); // Just expired

            Session justExpiredSession = new Session(
                    sessionId,
                    TEST_USER_ID,
                    TEST_USER_AGENT,
                    createdAt,
                    expiresAt,
                    null
            );

            String token = generateValidToken(sessionId, TEST_USER_ID, TEST_USER_AGENT,
                    Instant.now().plusSeconds(TEST_EXPIRATION));

            when(sessionManagementService.findCurrentUserAgentSession(sessionId, TEST_USER_ID, TEST_USER_AGENT))
                    .thenReturn(justExpiredSession);

            // when & then
            assertThrows(UserAgentSessionExpiredException.class,
                    () -> jwtAuthenticationSessionService.authenticate(token));
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
            Instant createdAt = Instant.now();
            Instant expiresAt = createdAt.plusSeconds(TEST_EXPIRATION);

            Session session1 = new Session(sessionId1, userId1, userAgent1, createdAt, expiresAt, null);
            Session session2 = new Session(sessionId2, userId2, userAgent2, createdAt, expiresAt, null);

            String token1 = generateValidToken(sessionId1, userId1, userAgent1, expiresAt);
            String token2 = generateValidToken(sessionId2, userId2, userAgent2, expiresAt);

            when(sessionManagementService.findCurrentUserAgentSession(sessionId1, userId1, userAgent1))
                    .thenReturn(session1);
            when(sessionManagementService.findCurrentUserAgentSession(sessionId2, userId2, userAgent2))
                    .thenReturn(session2);

            // when
            String authenticatedUser1 = jwtAuthenticationSessionService.authenticate(token1);
            String authenticatedUser2 = jwtAuthenticationSessionService.authenticate(token2);

            // then
            assertEquals(userId1, authenticatedUser1);
            assertEquals(userId2, authenticatedUser2);
        }
    }
}
