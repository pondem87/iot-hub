package com.pfitztronic.iothub.core.authentication.services.impl;

import com.pfitztronic.iothub.core.authentication.exceptions.UserAgentSessionAuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT Authentication Filter Tests")
class JwtAuthenticationFilterTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String VALID_JWT_TOKEN = "valid.jwt.token";
    private static final String INVALID_JWT_TOKEN = "invalid.jwt.token";
    private static final String TEST_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";
    private static final String TEST_USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager);
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("Filter Chain Processing Tests")
    class FilterChainProcessingTests {

        @Test
        @DisplayName("Should process request without authorization header")
        void processRequestWithoutAuthorizationHeader() throws ServletException, IOException {
            // given
            when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
            when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn(TEST_USER_AGENT);

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(request, response);
            verify(authenticationManager, never()).authenticate(any());
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }

        @Test
        @DisplayName("Should process request with non-Bearer authorization header")
        void processRequestWithNonBearerAuthorizationHeader() throws ServletException, IOException {
            // given
            when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic dXNlcjpwYXNz");
            when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn(TEST_USER_AGENT);

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(request, response);
            verify(authenticationManager, never()).authenticate(any());
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }

        @Test
        @DisplayName("Should extract Bearer token and process valid JWT")
        void extractBearerTokenAndProcessValidJwt() throws ServletException, IOException {
            // given
            String authHeader = "Bearer " + VALID_JWT_TOKEN;
            when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
            when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn(TEST_USER_AGENT);

            // Create a mock authenticated authentication object
            Authentication mockAuthentication = createMockAuthentication(true);
            when(authenticationManager.authenticate(any(JwtAuthentication.class)))
                    .thenReturn(mockAuthentication);

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            verify(authenticationManager).authenticate(any(JwtAuthentication.class));
            verify(filterChain).doFilter(request, response);
            assertNotNull(SecurityContextHolder.getContext().getAuthentication());
            assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
        }
    }

    @Nested
    @DisplayName("JWT Authentication Tests")
    class JwtAuthenticationTests {

        @Test
        @DisplayName("Should authenticate valid JWT token successfully")
        void authenticateValidJwtTokenSuccessfully() throws ServletException, IOException {
            // given
            String authHeader = "Bearer " + VALID_JWT_TOKEN;
            when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
            when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn(TEST_USER_AGENT);

            Authentication mockAuthentication = createMockAuthentication(true);
            when(authenticationManager.authenticate(any(JwtAuthentication.class)))
                    .thenReturn(mockAuthentication);

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            verify(authenticationManager).authenticate(any(JwtAuthentication.class));
            verify(filterChain).doFilter(request, response);
            SecurityContext context = SecurityContextHolder.getContext();
            assertNotNull(context.getAuthentication());
            assertTrue(context.getAuthentication().isAuthenticated());
            assertEquals(TEST_USERNAME, context.getAuthentication().getName());
        }

        @Test
        @DisplayName("Should set security context with authenticated JWT")
        void setSecurityContextWithAuthenticatedJwt() throws ServletException, IOException {
            // given
            String authHeader = "Bearer " + VALID_JWT_TOKEN;
            when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
            when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn(TEST_USER_AGENT);

            Authentication mockAuthentication = createMockAuthentication(true);
            when(authenticationManager.authenticate(any(JwtAuthentication.class)))
                    .thenReturn(mockAuthentication);

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            SecurityContext context = SecurityContextHolder.getContext();
            assertNotNull(context);
            assertNotNull(context.getAuthentication());
        }

        @Test
        @DisplayName("Should not set security context if authentication fails")
        void notSetSecurityContextIfAuthenticationFails() throws ServletException, IOException {
            // given
            String authHeader = "Bearer " + INVALID_JWT_TOKEN;
            when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
            when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn(TEST_USER_AGENT);

            Authentication mockAuthentication = createMockAuthentication(false);
            when(authenticationManager.authenticate(any(JwtAuthentication.class)))
                    .thenReturn(mockAuthentication);

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(request, response);
            SecurityContext context = SecurityContextHolder.getContext();
            assertNull(context.getAuthentication());
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Should handle UserAgentSessionAuthenticationException with 401 response")
        void handleUserAgentSessionAuthenticationException() throws ServletException, IOException {
            // given
            String authHeader = "Bearer " + INVALID_JWT_TOKEN;
            when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
            when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn(TEST_USER_AGENT);

            when(authenticationManager.authenticate(any(JwtAuthentication.class)))
                    .thenThrow(new UserAgentSessionAuthenticationException("Token validation failed"));

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(filterChain, never()).doFilter(request, response);
        }

        @Test
        @DisplayName("Should handle expired token exception")
        void handleExpiredTokenException() throws ServletException, IOException {
            // given
            String authHeader = "Bearer " + INVALID_JWT_TOKEN;
            when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
            when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn(TEST_USER_AGENT);

            when(authenticationManager.authenticate(any(JwtAuthentication.class)))
                    .thenThrow(new UserAgentSessionAuthenticationException("Token has expired"));

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should handle revoked token exception")
        void handleRevokedTokenException() throws ServletException, IOException {
            // given
            String authHeader = "Bearer " + INVALID_JWT_TOKEN;
            when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
            when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn(TEST_USER_AGENT);

            when(authenticationManager.authenticate(any(JwtAuthentication.class)))
                    .thenThrow(new UserAgentSessionAuthenticationException("Token has been revoked"));

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should handle invalid credentials exception")
        void handleInvalidCredentialsException() throws ServletException, IOException {
            // given
            String authHeader = "Bearer " + INVALID_JWT_TOKEN;
            when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
            when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn(TEST_USER_AGENT);

            when(authenticationManager.authenticate(any(JwtAuthentication.class)))
                    .thenThrow(new UserAgentSessionAuthenticationException("User agent credentials are invalid"));

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("User Agent Header Tests")
    class UserAgentHeaderTests {

        @Test
        @DisplayName("Should extract user agent from request header")
        void extractUserAgentFromRequestHeader() throws ServletException, IOException {
            // given
            String customUserAgent = "Custom User Agent";
            String authHeader = "Bearer " + VALID_JWT_TOKEN;
            when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
            when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn(customUserAgent);

            Authentication mockAuthentication = createMockAuthentication(true);
            when(authenticationManager.authenticate(any(JwtAuthentication.class)))
                    .thenReturn(mockAuthentication);

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            verify(authenticationManager).authenticate(any(JwtAuthentication.class));
        }

        @Test
        @DisplayName("Should process request with null user agent")
        void processRequestWithNullUserAgent() throws ServletException, IOException {
            // given
            String authHeader = "Bearer " + VALID_JWT_TOKEN;
            when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
            when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn(null);

            Authentication mockAuthentication = createMockAuthentication(true);
            when(authenticationManager.authenticate(any(JwtAuthentication.class)))
                    .thenReturn(mockAuthentication);

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            verify(authenticationManager).authenticate(any(JwtAuthentication.class));
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("Authorization Header Format Tests")
    class AuthorizationHeaderFormatTests {

        @Test
        @DisplayName("Should handle Bearer token with extra spaces")
        void handleBearerTokenWithExtraSpaces() throws ServletException, IOException {
            // given
            String authHeader = "Bearer  " + VALID_JWT_TOKEN;
            when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
            when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn(TEST_USER_AGENT);

            Authentication mockAuthentication = createMockAuthentication(true);
            when(authenticationManager.authenticate(any(JwtAuthentication.class)))
                    .thenReturn(mockAuthentication);

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then - should still process since it checks if startsWith("Bearer ")
            verify(authenticationManager).authenticate(any(JwtAuthentication.class));
        }

        @Test
        @DisplayName("Should handle empty Bearer token")
        void handleEmptyBearerToken() throws ServletException, IOException {
            // given
            String authHeader = "Bearer ";
            when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
            when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn(TEST_USER_AGENT);

            when(authenticationManager.authenticate(any(JwtAuthentication.class)))
                    .thenThrow(new UserAgentSessionAuthenticationException("Token is empty"));

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should not process request with case-sensitive Bearer prefix")
        void notProcessRequestWithCaseSensitiveBearerPrefix() throws ServletException, IOException {
            // given - using lowercase 'bearer' instead of 'Bearer'
            String authHeader = "bearer " + VALID_JWT_TOKEN;
            when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
            when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn(TEST_USER_AGENT);

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then - should skip authentication since startsWith check is case-sensitive
            verify(authenticationManager, never()).authenticate(any());
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("Filter Chain Continuation Tests")
    class FilterChainContinuationTests {

        @Test
        @DisplayName("Should continue filter chain after successful authentication")
        void continueFilterChainAfterSuccessfulAuthentication() throws ServletException, IOException {
            // given
            String authHeader = "Bearer " + VALID_JWT_TOKEN;
            when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
            when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn(TEST_USER_AGENT);

            Authentication mockAuthentication = createMockAuthentication(true);
            when(authenticationManager.authenticate(any(JwtAuthentication.class)))
                    .thenReturn(mockAuthentication);

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should continue filter chain after authentication failure without exception")
        void continueFilterChainAfterAuthenticationFailure() throws ServletException, IOException {
            // given
            String authHeader = "Bearer " + INVALID_JWT_TOKEN;
            when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
            when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn(TEST_USER_AGENT);

            Authentication mockAuthentication = createMockAuthentication(false);
            when(authenticationManager.authenticate(any(JwtAuthentication.class)))
                    .thenReturn(mockAuthentication);

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should not continue filter chain when exception occurs")
        void notContinueFilterChainWhenExceptionOccurs() throws ServletException, IOException {
            // given
            String authHeader = "Bearer " + INVALID_JWT_TOKEN;
            when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
            when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn(TEST_USER_AGENT);

            when(authenticationManager.authenticate(any(JwtAuthentication.class)))
                    .thenThrow(new UserAgentSessionAuthenticationException("Authentication failed"));

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain, never()).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("Security Context Tests")
    class SecurityContextTests {

        @Test
        @DisplayName("Should clear and recreate security context for authenticated user")
        void clearAndRecreateSecurityContextForAuthenticatedUser() throws ServletException, IOException {
            // given
            String authHeader = "Bearer " + VALID_JWT_TOKEN;
            when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
            when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn(TEST_USER_AGENT);

            Authentication mockAuthentication = createMockAuthentication(true);
            when(authenticationManager.authenticate(any(JwtAuthentication.class)))
                    .thenReturn(mockAuthentication);

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            SecurityContext context = SecurityContextHolder.getContext();
            assertNotNull(context);
            assertEquals(mockAuthentication, context.getAuthentication());
        }

        @Test
        @DisplayName("Should maintain security context across filter chain")
        void maintainSecurityContextAcrossFilterChain() throws ServletException, IOException {
            // given
            String authHeader = "Bearer " + VALID_JWT_TOKEN;
            when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
            when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn(TEST_USER_AGENT);

            Authentication mockAuthentication = createMockAuthentication(true);
            when(authenticationManager.authenticate(any(JwtAuthentication.class)))
                    .thenReturn(mockAuthentication);

            // Capture the security context to verify it's properly set
            doAnswer(invocation -> {
                SecurityContext context = SecurityContextHolder.getContext();
                assertNotNull(context.getAuthentication());
                assertTrue(context.getAuthentication().isAuthenticated());
                return null;
            }).when(filterChain).doFilter(request, response);

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("Multiple Request Processing Tests")
    class MultipleRequestProcessingTests {

        @Test
        @DisplayName("Should handle multiple requests independently")
        void handleMultipleRequestsIndependently() throws ServletException, IOException {
            // First request - with JWT
            String authHeader = "Bearer " + VALID_JWT_TOKEN;
            when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
            when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn(TEST_USER_AGENT);

            Authentication mockAuthentication = createMockAuthentication(true);
            when(authenticationManager.authenticate(any(JwtAuthentication.class)))
                    .thenReturn(mockAuthentication);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Clear context for next request
            SecurityContextHolder.clearContext();

            // Second request - without JWT
            HttpServletRequest request2 = mock(HttpServletRequest.class);
            when(request2.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
            when(request2.getHeader(HttpHeaders.USER_AGENT)).thenReturn(TEST_USER_AGENT);

            jwtAuthenticationFilter.doFilterInternal(request2, response, filterChain);

            // then
            verify(filterChain, times(2)).doFilter(any(), any());
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }
    }

    // Helper method to create mock authentication
    private Authentication createMockAuthentication(boolean authenticated) {
        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER")
        );
        return new JwtAuthentication(
                new User(TEST_USERNAME, "password", authenticated, true, true, true, authorities),
                authenticated
        );
    }
}

