package com.pfitztronic.iothub.core.authentication.services.impl;

import com.pfitztronic.iothub.core.TestFixtures;
import com.pfitztronic.iothub.core.authentication.dto.LoginInputData;
import com.pfitztronic.iothub.core.authentication.dto.LoginResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Authentication Service Tests")
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtAuthenticationSessionService jwtAuthenticationSessionService;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        try {
            authenticationService = new AuthenticationService(
                    authenticationManager,
                    jwtAuthenticationSessionService
            );
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should successfully login with valid credentials")
        void shouldSuccessfullyLoginWithValidCredentials() {
            // given
            LoginInputData credentials = new LoginInputData(TestFixtures.TEST_PHONE_NUMBER, TestFixtures.TEST_PASSWORD);
            Authentication mockAuthentication = createMockAuthentication(TestFixtures.TEST_USER_ID, true);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(mockAuthentication);
            when(jwtAuthenticationSessionService.generateToken(TestFixtures.TEST_USER_ID, TestFixtures.TEST_USER_AGENT))
                    .thenReturn(TestFixtures.GENERATED_JWT_TOKEN);

            // when
            LoginResponse response = authenticationService.login(TestFixtures.TEST_USER_AGENT, credentials);

            // then
            assertNotNull(response);
            assertEquals(TestFixtures.GENERATED_JWT_TOKEN, response.token());
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(jwtAuthenticationSessionService).generateToken(TestFixtures.TEST_USER_ID, TestFixtures.TEST_USER_AGENT);
        }

        @Test
        @DisplayName("Should create correct authentication token with phone number and password")
        void shouldCreateCorrectAuthenticationToken() {
            // given
            LoginInputData credentials = new LoginInputData(TestFixtures.TEST_PHONE_NUMBER, TestFixtures.TEST_PASSWORD);
            Authentication mockAuthentication = createMockAuthentication(TestFixtures.TEST_USER_ID, true);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(mockAuthentication);
            when(jwtAuthenticationSessionService.generateToken(anyString(), anyString()))
                    .thenReturn(TestFixtures.GENERATED_JWT_TOKEN);

            // when
            authenticationService.login(TestFixtures.TEST_USER_AGENT, credentials);

            // then
            verify(authenticationManager).authenticate(argThat(token ->
                    token.getName().equals(TestFixtures.TEST_PHONE_NUMBER) &&
                    token.getCredentials().equals(TestFixtures.TEST_PASSWORD)
            ));
        }

        @Test
        @DisplayName("Should return LoginResponse with generated token")
        void shouldReturnLoginResponseWithToken() {
            // given
            LoginInputData credentials = new LoginInputData(TestFixtures.TEST_PHONE_NUMBER, TestFixtures.TEST_PASSWORD);
            Authentication mockAuthentication = createMockAuthentication(TestFixtures.TEST_USER_ID, true);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(mockAuthentication);
            when(jwtAuthenticationSessionService.generateToken(TestFixtures.TEST_USER_ID, TestFixtures.TEST_USER_AGENT))
                    .thenReturn(TestFixtures.GENERATED_JWT_TOKEN);

            // when
            LoginResponse response = authenticationService.login(TestFixtures.TEST_USER_AGENT, credentials);

            // then
            assertNotNull(response);
            assertNotNull(response.token());
            assertEquals(TestFixtures.GENERATED_JWT_TOKEN, response.token());
        }

        @Test
        @DisplayName("Should throw AuthorizationDeniedException when authentication fails")
        void shouldThrowAuthorizationDeniedExceptionWhenAuthenticationFails() {
            // given
            LoginInputData credentials = new LoginInputData(TestFixtures.TEST_PHONE_NUMBER, TestFixtures.TEST_PASSWORD);
            Authentication failedAuthentication = createMockAuthentication(TestFixtures.TEST_USER_ID, false);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(failedAuthentication);

            // when & then
            AuthorizationDeniedException exception = assertThrows(
                    AuthorizationDeniedException.class,
                    () -> authenticationService.login(TestFixtures.TEST_USER_AGENT, credentials)
            );
            assertEquals("Authentication failed", exception.getMessage());
            verify(jwtAuthenticationSessionService, never()).generateToken(anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw exception when authentication manager throws BadCredentialsException")
        void shouldThrowExceptionOnBadCredentials() {
            // given
            LoginInputData credentials = new LoginInputData(TestFixtures.TEST_PHONE_NUMBER, TestFixtures.TEST_PASSWORD);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // when & then
            assertThrows(BadCredentialsException.class,
                    () -> authenticationService.login(TestFixtures.TEST_USER_AGENT, credentials)
            );
        }

        @Test
        @DisplayName("Should generate token with user ID from authentication")
        void shouldGenerateTokenWithUserIdFromAuthentication() {
            // given
            LoginInputData credentials = new LoginInputData(TestFixtures.TEST_PHONE_NUMBER, TestFixtures.TEST_PASSWORD);
            String expectedUserId = "user-123-xyz";
            Authentication mockAuthentication = createMockAuthentication(expectedUserId, true);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(mockAuthentication);
            when(jwtAuthenticationSessionService.generateToken(expectedUserId, TestFixtures.TEST_USER_AGENT))
                    .thenReturn(TestFixtures.GENERATED_JWT_TOKEN);

            // when
            authenticationService.login(TestFixtures.TEST_USER_AGENT, credentials);

            // then
            verify(jwtAuthenticationSessionService).generateToken(expectedUserId, TestFixtures.TEST_USER_AGENT);
        }

        @Test
        @DisplayName("Should pass user agent to token generation")
        void shouldPassUserAgentToTokenGeneration() {
            // given
            LoginInputData credentials = new LoginInputData(TestFixtures.TEST_PHONE_NUMBER, TestFixtures.TEST_PASSWORD);
            String customUserAgent = "Custom User Agent";
            Authentication mockAuthentication = createMockAuthentication(TestFixtures.TEST_USER_ID, true);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(mockAuthentication);
            when(jwtAuthenticationSessionService.generateToken(TestFixtures.TEST_USER_ID, customUserAgent))
                    .thenReturn(TestFixtures.GENERATED_JWT_TOKEN);

            // when
            authenticationService.login(customUserAgent, credentials);

            // then
            verify(jwtAuthenticationSessionService).generateToken(TestFixtures.TEST_USER_ID, customUserAgent);
        }
    }

    @Nested
    @DisplayName("Logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("Should successfully logout authenticated user")
        void shouldSuccessfullyLogoutAuthenticatedUser() {
            // given
            Authentication mockAuthentication = createMockAuthentication(TestFixtures.TEST_USER_ID, true);
            setSecurityContext(mockAuthentication);

            // when
            String result = authenticationService.logout(TestFixtures.TEST_USER_AGENT);

            // then
            assertEquals("Logged out successfully", result);
            verify(jwtAuthenticationSessionService).revokeCurrentSession(TestFixtures.TEST_USER_ID, TestFixtures.TEST_USER_AGENT);
        }

        @Test
        @DisplayName("Should revoke current session with correct user ID and user agent")
        void shouldRevokeCurrentSessionWithCorrectParameters() {
            // given
            String userId = "user-456-xyz";
            String userAgent = "Custom Browser";
            Authentication mockAuthentication = createMockAuthentication(userId, true);
            setSecurityContext(mockAuthentication);

            // when
            authenticationService.logout(userAgent);

            // then
            verify(jwtAuthenticationSessionService).revokeCurrentSession(userId, userAgent);
        }

        @Test
        @DisplayName("Should throw AuthorizationDeniedException when no authenticated user")
        void shouldThrowExceptionWhenNoAuthenticatedUser() {
            // given - no security context set
            SecurityContextHolder.clearContext();

            // when & then
            AuthorizationDeniedException exception = assertThrows(
                    AuthorizationDeniedException.class,
                    () -> authenticationService.logout(TestFixtures.TEST_USER_AGENT)
            );
            assertEquals("No authenticated user found", exception.getMessage());
            verify(jwtAuthenticationSessionService, never()).revokeCurrentSession(anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw exception when authentication is null")
        void shouldThrowExceptionWhenAuthenticationIsNull() {
            // given
            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(null);

            // when & then
            AuthorizationDeniedException exception = assertThrows(
                    AuthorizationDeniedException.class,
                    () -> authenticationService.logout(TestFixtures.TEST_USER_AGENT)
            );
            assertEquals("No authenticated user found", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when authentication is not authenticated")
        void shouldThrowExceptionWhenAuthenticationNotAuthenticated() {
            // given
            Authentication mockAuthentication = createMockAuthentication(TestFixtures.TEST_USER_ID, false);
            setSecurityContext(mockAuthentication);

            // when & then
            AuthorizationDeniedException exception = assertThrows(
                    AuthorizationDeniedException.class,
                    () -> authenticationService.logout(TestFixtures.TEST_USER_AGENT)
            );
            assertEquals("No authenticated user found", exception.getMessage());
            verify(jwtAuthenticationSessionService, never()).revokeCurrentSession(anyString(), anyString());
        }

        @Test
        @DisplayName("Should pass correct user agent to revoke session")
        void shouldPassCorrectUserAgentToRevokeSession() {
            // given
            String specificUserAgent = "Specific User Agent String";
            Authentication mockAuthentication = createMockAuthentication(TestFixtures.TEST_USER_ID, true);
            setSecurityContext(mockAuthentication);

            // when
            authenticationService.logout(specificUserAgent);

            // then
            verify(jwtAuthenticationSessionService).revokeCurrentSession(TestFixtures.TEST_USER_ID, specificUserAgent);
        }
    }

    @Nested
    @DisplayName("Logout All Sessions Tests")
    class LogoutAllSessionsTests {

        @Test
        @DisplayName("Should successfully logout from all sessions")
        void shouldSuccessfullyLogoutFromAllSessions() {
            // given
            Authentication mockAuthentication = createMockAuthentication(TestFixtures.TEST_USER_ID, true);
            setSecurityContext(mockAuthentication);

            // when
            String result = authenticationService.logoutAllSessions();

            // then
            assertEquals("Logged out from all sessions successfully", result);
            verify(jwtAuthenticationSessionService).revokeAllSessions(TestFixtures.TEST_USER_ID);
        }

        @Test
        @DisplayName("Should revoke all sessions for authenticated user")
        void shouldRevokeAllSessionsForAuthenticatedUser() {
            // given
            String userId = "user-789-xyz";
            Authentication mockAuthentication = createMockAuthentication(userId, true);
            setSecurityContext(mockAuthentication);

            // when
            authenticationService.logoutAllSessions();

            // then
            verify(jwtAuthenticationSessionService).revokeAllSessions(userId);
        }

        @Test
        @DisplayName("Should throw AuthorizationDeniedException when no authenticated user")
        void shouldThrowExceptionWhenNoAuthenticatedUser() {
            // given - no security context set
            SecurityContextHolder.clearContext();

            // when & then
            AuthorizationDeniedException exception = assertThrows(
                    AuthorizationDeniedException.class,
                    () -> authenticationService.logoutAllSessions()
            );
            assertEquals("No authenticated user found", exception.getMessage());
            verify(jwtAuthenticationSessionService, never()).revokeAllSessions(anyString());
        }

        @Test
        @DisplayName("Should throw exception when authentication is null")
        void shouldThrowExceptionWhenAuthenticationIsNull() {
            // given
            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(null);

            // when & then
            AuthorizationDeniedException exception = assertThrows(
                    AuthorizationDeniedException.class,
                    () -> authenticationService.logoutAllSessions()
            );
            assertEquals("No authenticated user found", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when authentication is not authenticated")
        void shouldThrowExceptionWhenAuthenticationNotAuthenticated() {
            // given
            Authentication mockAuthentication = createMockAuthentication(TestFixtures.TEST_USER_ID, false);
            setSecurityContext(mockAuthentication);

            // when & then
            AuthorizationDeniedException exception = assertThrows(
                    AuthorizationDeniedException.class,
                    () -> authenticationService.logoutAllSessions()
            );
            assertEquals("No authenticated user found", exception.getMessage());
            verify(jwtAuthenticationSessionService, never()).revokeAllSessions(anyString());
        }
    }

    @Nested
    @DisplayName("Security Context Integration Tests")
    class SecurityContextIntegrationTests {

        @Test
        @DisplayName("Should retrieve user from security context during logout")
        void shouldRetrieveUserFromSecurityContextDuringLogout() {
            // given
            String contextUsername = "context-user-123";
            Authentication mockAuthentication = createMockAuthentication(contextUsername, true);
            setSecurityContext(mockAuthentication);

            // when
            authenticationService.logout(TestFixtures.TEST_USER_AGENT);

            // then
            verify(jwtAuthenticationSessionService).revokeCurrentSession(contextUsername, TestFixtures.TEST_USER_AGENT);
        }

        @Test
        @DisplayName("Should retrieve user from security context during logout all")
        void shouldRetrieveUserFromSecurityContextDuringLogoutAll() {
            // given
            String contextUsername = "context-user-456";
            Authentication mockAuthentication = createMockAuthentication(contextUsername, true);
            setSecurityContext(mockAuthentication);

            // when
            authenticationService.logoutAllSessions();

            // then
            verify(jwtAuthenticationSessionService).revokeAllSessions(contextUsername);
        }
    }

    @Nested
    @DisplayName("Exception Message Tests")
    class ExceptionMessageTests {

        @Test
        @DisplayName("Should have correct message for failed authentication")
        void shouldHaveCorrectMessageForFailedAuthentication() {
            // given
            LoginInputData credentials = new LoginInputData(TestFixtures.TEST_PHONE_NUMBER, TestFixtures.TEST_PASSWORD);
            Authentication failedAuth = createMockAuthentication(TestFixtures.TEST_USER_ID, false);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(failedAuth);

            // when
            AuthorizationDeniedException exception = assertThrows(
                    AuthorizationDeniedException.class,
                    () -> authenticationService.login(TestFixtures.TEST_USER_AGENT, credentials)
            );

            // then
            assertEquals("Authentication failed", exception.getMessage());
        }

        @Test
        @DisplayName("Should have correct message for logout without authentication")
        void shouldHaveCorrectMessageForLogoutWithoutAuthentication() {
            // given
            SecurityContextHolder.clearContext();

            // when
            AuthorizationDeniedException exception = assertThrows(
                    AuthorizationDeniedException.class,
                    () -> authenticationService.logout(TestFixtures.TEST_USER_AGENT)
            );

            // then
            assertEquals("No authenticated user found", exception.getMessage());
        }

        @Test
        @DisplayName("Should have correct message for logout all without authentication")
        void shouldHaveCorrectMessageForLogoutAllWithoutAuthentication() {
            // given
            SecurityContextHolder.clearContext();

            // when
            AuthorizationDeniedException exception = assertThrows(
                    AuthorizationDeniedException.class,
                    () -> authenticationService.logoutAllSessions()
            );

            // then
            assertEquals("No authenticated user found", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Concurrent Login Tests")
    class ConcurrentLoginTests {

        @Test
        @DisplayName("Should handle concurrent login attempts")
        void concurrentLoginAttempts() throws InterruptedException {
            // given
            int numberOfThreads = 3;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(numberOfThreads);
            AtomicInteger successCount = new AtomicInteger(0);
            ConcurrentLinkedQueue<Throwable> unexpectedFailures = new ConcurrentLinkedQueue<>();

            // Setup mock before concurrent test
            Authentication mockAuth = TestFixtures.createMockAuthentication(TestFixtures.TEST_USER_ID, true);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(mockAuth);
            when(jwtAuthenticationSessionService.generateToken(anyString(), anyString()))
                    .thenReturn(TestFixtures.GENERATED_JWT_TOKEN);

            // when
            ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

            for (int i = 0; i < numberOfThreads; i++) {
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        
                        LoginResponse response = authenticationService.login(TestFixtures.TEST_USER_AGENT,
                                new LoginInputData(TestFixtures.TEST_PHONE_NUMBER, TestFixtures.TEST_PASSWORD));
                        
                        if (response != null && response.token() != null && !response.token().isEmpty()) {
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
            assertEquals(numberOfThreads, successCount.get(), "All logins should succeed");
        }
    }

    // Helper methods
    private Authentication createMockAuthentication(String username, boolean authenticated) {
        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER")
        );
        return new UsernamePasswordAuthenticationToken(
                username,
                null,
                authenticated ? authorities : Collections.emptyList()
        ) {
            @Override
            public boolean isAuthenticated() {
                return authenticated;
            }
        };
    }

    private void setSecurityContext(Authentication authentication) {
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
    }
}