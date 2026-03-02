package com.pfitztronic.iothub.core.authentication.services.impl;

import com.pfitztronic.iothub.core.authentication.exceptions.UserAgentCredentialsInvalidException;
import com.pfitztronic.iothub.core.authentication.exceptions.UserAgentSessionExpiredException;
import com.pfitztronic.iothub.core.authentication.exceptions.UserAgentSessionRevokedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT Authentication Provider Tests")
class JwtAuthenticationProviderTest {

    private static final String TEST_TOKEN = "valid.jwt.token";
    private static final String TEST_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";
    private static final String TEST_USER_ID = "+12345678901";

    @Mock
    private JwtAuthenticationSessionService jwtAuthenticationSessionService;

    @Mock
    private UserDetailsService userDetailsService;

    private JwtAuthenticationProvider jwtAuthenticationProvider;

    @BeforeEach
    void setUp() {
        jwtAuthenticationProvider = new JwtAuthenticationProvider(
                jwtAuthenticationSessionService,
                userDetailsService
        );
    }

    private UserDetails createTestUserDetails(String userId, Collection<? extends GrantedAuthority> authorities) {
        return new User(userId, "", authorities);
    }

    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticationTests {

        @Test
        @DisplayName("Should successfully authenticate a valid JWT token")
        void authenticateValidTokenSuccess() {
            // given
            JwtAuthentication jwtAuthentication = new JwtAuthentication(TEST_TOKEN, TEST_USER_AGENT);
            Collection<GrantedAuthority> authorities = Set.of(new SimpleGrantedAuthority("ROLE_USER"));
            UserDetails userDetails = createTestUserDetails(TEST_USER_ID, authorities);

            when(jwtAuthenticationSessionService.authenticate(TEST_TOKEN))
                    .thenReturn(TEST_USER_ID);
            when(userDetailsService.loadUserByUsername(TEST_USER_ID))
                    .thenReturn(userDetails);

            // when
            Authentication result = jwtAuthenticationProvider.authenticate(jwtAuthentication);

            // then
            assertNotNull(result);
            assertTrue(result.isAuthenticated());
            assertEquals(userDetails, result.getPrincipal());
            assertEquals(userDetails, result.getDetails());
            assertEquals(authorities, result.getAuthorities());

            verify(jwtAuthenticationSessionService, times(1)).authenticate(TEST_TOKEN);
            verify(userDetailsService, times(1)).loadUserByUsername(TEST_USER_ID);
        }

        @Test
        @DisplayName("Should return authenticated JwtAuthentication with user details")
        void authenticateReturnsJwtAuthenticationWithUserDetails() {
            // given
            JwtAuthentication jwtAuthentication = new JwtAuthentication(TEST_TOKEN, TEST_USER_AGENT);
            UserDetails userDetails = createTestUserDetails(TEST_USER_ID, List.of());

            when(jwtAuthenticationSessionService.authenticate(TEST_TOKEN))
                    .thenReturn(TEST_USER_ID);
            when(userDetailsService.loadUserByUsername(TEST_USER_ID))
                    .thenReturn(userDetails);

            // when
            Authentication result = jwtAuthenticationProvider.authenticate(jwtAuthentication);

            // then
            assertInstanceOf(JwtAuthentication.class, result);
            assertEquals(TEST_USER_ID, result.getName());
        }

        @Test
        @DisplayName("Should authenticate user with multiple authorities")
        void authenticateUserWithMultipleAuthorities() {
            // given
            JwtAuthentication jwtAuthentication = new JwtAuthentication(TEST_TOKEN, TEST_USER_AGENT);
            List<GrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_USER"),
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("PERMISSION_READ"),
                    new SimpleGrantedAuthority("PERMISSION_WRITE")
            );
            UserDetails userDetails = createTestUserDetails(TEST_USER_ID, authorities);

            when(jwtAuthenticationSessionService.authenticate(TEST_TOKEN))
                    .thenReturn(TEST_USER_ID);
            when(userDetailsService.loadUserByUsername(TEST_USER_ID))
                    .thenReturn(userDetails);

            // when
            Authentication result = jwtAuthenticationProvider.authenticate(jwtAuthentication);

            // then
            assertEquals(4, result.getAuthorities().size());
            assertTrue(result.getAuthorities().containsAll(authorities));
        }
    }

    @Nested
    @DisplayName("Authentication Failure Tests")
    class AuthenticationFailureTests {

        @Test
        @DisplayName("Should throw exception when token is invalid")
        void authenticateInvalidTokenThrowsException() {
            // given
            String invalidToken = "invalid.token";
            JwtAuthentication jwtAuthentication = new JwtAuthentication(invalidToken, TEST_USER_AGENT);

            when(jwtAuthenticationSessionService.authenticate(invalidToken))
                    .thenThrow(new UserAgentCredentialsInvalidException("User agent credentials are invalid"));

            // when & then
            assertThrows(UserAgentCredentialsInvalidException.class,
                    () -> jwtAuthenticationProvider.authenticate(jwtAuthentication));

            verify(jwtAuthenticationSessionService, times(1)).authenticate(invalidToken);
            verifyNoInteractions(userDetailsService);
        }

        @Test
        @DisplayName("Should throw exception when session is expired")
        void authenticateExpiredSessionThrowsException() {
            // given
            JwtAuthentication jwtAuthentication = new JwtAuthentication(TEST_TOKEN, TEST_USER_AGENT);

            when(jwtAuthenticationSessionService.authenticate(TEST_TOKEN))
                    .thenThrow(new UserAgentSessionExpiredException("Token has expired"));

            // when & then
            UserAgentSessionExpiredException exception = assertThrows(UserAgentSessionExpiredException.class,
                    () -> jwtAuthenticationProvider.authenticate(jwtAuthentication));

            assertEquals("Token has expired", exception.getMessage());
            verifyNoInteractions(userDetailsService);
        }

        @Test
        @DisplayName("Should throw exception when session is revoked")
        void authenticateRevokedSessionThrowsException() {
            // given
            JwtAuthentication jwtAuthentication = new JwtAuthentication(TEST_TOKEN, TEST_USER_AGENT);

            when(jwtAuthenticationSessionService.authenticate(TEST_TOKEN))
                    .thenThrow(new UserAgentSessionRevokedException("Token has been revoked"));

            // when & then
            UserAgentSessionRevokedException exception = assertThrows(UserAgentSessionRevokedException.class,
                    () -> jwtAuthenticationProvider.authenticate(jwtAuthentication));

            assertEquals("Token has been revoked", exception.getMessage());
            verifyNoInteractions(userDetailsService);
        }

        @Test
        @DisplayName("Should throw exception when user is not found")
        void authenticateUserNotFoundThrowsException() {
            // given
            JwtAuthentication jwtAuthentication = new JwtAuthentication(TEST_TOKEN, TEST_USER_AGENT);

            when(jwtAuthenticationSessionService.authenticate(TEST_TOKEN))
                    .thenReturn(TEST_USER_ID);
            when(userDetailsService.loadUserByUsername(TEST_USER_ID))
                    .thenThrow(new UsernameNotFoundException("User not found"));

            // when & then
            UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                    () -> jwtAuthenticationProvider.authenticate(jwtAuthentication));

            assertEquals("User not found", exception.getMessage());
            verify(jwtAuthenticationSessionService, times(1)).authenticate(TEST_TOKEN);
            verify(userDetailsService, times(1)).loadUserByUsername(TEST_USER_ID);
        }
    }

    @Nested
    @DisplayName("Supports Tests")
    class SupportsTests {

        @Test
        @DisplayName("Should support JwtAuthentication class")
        void supportsJwtAuthentication() {
            // when
            boolean supports = jwtAuthenticationProvider.supports(JwtAuthentication.class);

            // then
            assertTrue(supports);
        }

        @Test
        @DisplayName("Should not support other Authentication implementations")
        void doesNotSupportOtherAuthentication() {
            // when
            boolean supportsUsernamePassword = jwtAuthenticationProvider.supports(
                    org.springframework.security.authentication.UsernamePasswordAuthenticationToken.class
            );

            // then
            assertFalse(supportsUsernamePassword);
        }

        @Test
        @DisplayName("Should not support generic Authentication interface")
        void doesNotSupportGenericAuthentication() {
            // when
            boolean supports = jwtAuthenticationProvider.supports(Authentication.class);

            // then
            assertFalse(supports);
        }
    }

    @Nested
    @DisplayName("Integration Scenario Tests")
    class IntegrationScenarioTests {

        @Test
        @DisplayName("Should authenticate different users with different tokens")
        void authenticateDifferentUsers() {
            // given
            String token1 = "token1";
            String token2 = "token2";
            String userId1 = "+12345678901";
            String userId2 = "+10987654321";

            JwtAuthentication auth1 = new JwtAuthentication(token1, TEST_USER_AGENT);
            JwtAuthentication auth2 = new JwtAuthentication(token2, TEST_USER_AGENT);

            UserDetails userDetails1 = createTestUserDetails(userId1, List.of(new SimpleGrantedAuthority("ROLE_USER")));
            UserDetails userDetails2 = createTestUserDetails(userId2, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

            when(jwtAuthenticationSessionService.authenticate(token1)).thenReturn(userId1);
            when(jwtAuthenticationSessionService.authenticate(token2)).thenReturn(userId2);
            when(userDetailsService.loadUserByUsername(userId1)).thenReturn(userDetails1);
            when(userDetailsService.loadUserByUsername(userId2)).thenReturn(userDetails2);

            // when
            Authentication result1 = jwtAuthenticationProvider.authenticate(auth1);
            Authentication result2 = jwtAuthenticationProvider.authenticate(auth2);

            // then
            assertEquals(userId1, result1.getName());
            assertEquals(userId2, result2.getName());
            assertTrue(result1.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
            assertTrue(result2.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        }

        @Test
        @DisplayName("Should authenticate same user from different user agents")
        void authenticateSameUserDifferentUserAgents() {
            // given
            String userAgent1 = "Mozilla/5.0 (Windows)";
            String userAgent2 = "Mozilla/5.0 (iPhone)";

            JwtAuthentication auth1 = new JwtAuthentication(TEST_TOKEN, userAgent1);
            JwtAuthentication auth2 = new JwtAuthentication("another.token", userAgent2);

            UserDetails userDetails = createTestUserDetails(TEST_USER_ID, List.of());

            when(jwtAuthenticationSessionService.authenticate(TEST_TOKEN)).thenReturn(TEST_USER_ID);
            when(jwtAuthenticationSessionService.authenticate("another.token")).thenReturn(TEST_USER_ID);
            when(userDetailsService.loadUserByUsername(TEST_USER_ID)).thenReturn(userDetails);

            // when
            Authentication result1 = jwtAuthenticationProvider.authenticate(auth1);
            Authentication result2 = jwtAuthenticationProvider.authenticate(auth2);

            // then
            assertNotNull(result1);
            assertEquals(TEST_USER_ID, result1.getName());
            assertNotNull(result2);
            assertEquals(TEST_USER_ID, result2.getName());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should authenticate user with empty authorities")
        void authenticateUserWithEmptyAuthorities() {
            // given
            JwtAuthentication jwtAuthentication = new JwtAuthentication(TEST_TOKEN, TEST_USER_AGENT);
            UserDetails userDetails = createTestUserDetails(TEST_USER_ID, List.of());

            when(jwtAuthenticationSessionService.authenticate(TEST_TOKEN))
                    .thenReturn(TEST_USER_ID);
            when(userDetailsService.loadUserByUsername(TEST_USER_ID))
                    .thenReturn(userDetails);

            // when
            Authentication result = jwtAuthenticationProvider.authenticate(jwtAuthentication);

            // then
            assertNotNull(result);
            assertTrue(result.isAuthenticated());
            assertTrue(result.getAuthorities().isEmpty());
        }

        @Test
        @DisplayName("Should handle user with special characters in userId")
        void authenticateUserWithSpecialCharactersInUserId() {
            // given
            String specialUserId = "+1-234-567-8901";
            JwtAuthentication jwtAuthentication = new JwtAuthentication(TEST_TOKEN, TEST_USER_AGENT);
            UserDetails userDetails = createTestUserDetails(specialUserId, List.of());

            when(jwtAuthenticationSessionService.authenticate(TEST_TOKEN))
                    .thenReturn(specialUserId);
            when(userDetailsService.loadUserByUsername(specialUserId))
                    .thenReturn(userDetails);

            // when
            Authentication result = jwtAuthenticationProvider.authenticate(jwtAuthentication);

            // then
            assertNotNull(result);
            assertEquals(specialUserId, result.getName());
        }
    }
}
