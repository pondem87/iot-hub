package com.pfitztronic.iothub.core.accounts.services.impl;

import com.pfitztronic.iothub.core.accounts.models.PhoneNumber;
import com.pfitztronic.iothub.core.accounts.models.User;
import com.pfitztronic.iothub.core.accounts.models.UserStatus;
import com.pfitztronic.iothub.core.accounts.services.interfaces.IUserPermissionsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl Tests")
class UserDetailsServiceImplTest {

    private static final String TEST_USER_ID = "+12345678901";
    private static final String TEST_NAME = "Test User";
    private static final String TEST_PASSWORD_HASH = "hashedPassword123";

    @Mock
    private UserManagementService userManagementService;

    @Mock
    private IUserPermissionsService userPermissionsService;

    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new UserDetailsServiceImpl(
                userManagementService,
                userPermissionsService
        );
    }

    private User createTestUser(String userId, UserStatus status) {
        return User.builder()
                .userId(new PhoneNumber(userId))
                .name(TEST_NAME)
                .passwordHash(TEST_PASSWORD_HASH)
                .createdAt(Instant.now())
                .status(status)
                .verified(true)
                .build();
    }

    @Nested
    @DisplayName("Load User By Username Tests")
    class LoadUserByUsernameTests {

        @Test
        @DisplayName("Should successfully load user by username")
        void loadUserByUsernameSuccess() {
            // given
            User user = createTestUser(TEST_USER_ID, UserStatus.ACTIVE);
            Collection<GrantedAuthority> permissions = Set.of(
                    new SimpleGrantedAuthority("ROLE_USER")
            );

            when(userManagementService.getUserById(TEST_USER_ID)).thenReturn(user);
            when(userPermissionsService.getUserPermissions(TEST_USER_ID)).thenReturn(permissions);

            // when
            UserDetails result = userDetailsService.loadUserByUsername(TEST_USER_ID);

            // then
            assertNotNull(result);
            assertEquals(TEST_USER_ID, result.getUsername());
            assertEquals(TEST_PASSWORD_HASH, result.getPassword());
            assertEquals(1, result.getAuthorities().size());
            assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));

            verify(userManagementService, times(1)).getUserById(TEST_USER_ID);
            verify(userPermissionsService, times(1)).getUserPermissions(TEST_USER_ID);
        }

        @Test
        @DisplayName("Should return UserDetailsImpl instance")
        void loadUserByUsernameReturnsUserDetailsImpl() {
            // given
            User user = createTestUser(TEST_USER_ID, UserStatus.ACTIVE);
            Collection<GrantedAuthority> permissions = Set.of();

            when(userManagementService.getUserById(TEST_USER_ID)).thenReturn(user);
            when(userPermissionsService.getUserPermissions(TEST_USER_ID)).thenReturn(permissions);

            // when
            UserDetails result = userDetailsService.loadUserByUsername(TEST_USER_ID);

            // then
            assertInstanceOf(UserDetailsImpl.class, result);
        }

        @Test
        @DisplayName("Should load user with multiple permissions")
        void loadUserByUsernameWithMultiplePermissions() {
            // given
            User user = createTestUser(TEST_USER_ID, UserStatus.ACTIVE);
            Collection<GrantedAuthority> permissions = Set.of(
                    new SimpleGrantedAuthority("ROLE_USER"),
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("PERMISSION_READ"),
                    new SimpleGrantedAuthority("PERMISSION_WRITE")
            );

            when(userManagementService.getUserById(TEST_USER_ID)).thenReturn(user);
            when(userPermissionsService.getUserPermissions(TEST_USER_ID)).thenReturn(permissions);

            // when
            UserDetails result = userDetailsService.loadUserByUsername(TEST_USER_ID);

            // then
            assertEquals(4, result.getAuthorities().size());
            assertTrue(result.getAuthorities().containsAll(permissions));
        }

        @Test
        @DisplayName("Should load user with empty permissions")
        void loadUserByUsernameWithEmptyPermissions() {
            // given
            User user = createTestUser(TEST_USER_ID, UserStatus.ACTIVE);
            Collection<GrantedAuthority> permissions = Set.of();

            when(userManagementService.getUserById(TEST_USER_ID)).thenReturn(user);
            when(userPermissionsService.getUserPermissions(TEST_USER_ID)).thenReturn(permissions);

            // when
            UserDetails result = userDetailsService.loadUserByUsername(TEST_USER_ID);

            // then
            assertTrue(result.getAuthorities().isEmpty());
        }
    }

    @Nested
    @DisplayName("User Not Found Tests")
    class UserNotFoundTests {

        @Test
        @DisplayName("Should throw UsernameNotFoundException when user is not found")
        void loadUserByUsernameThrowsExceptionWhenUserNotFound() {
            // given
            String nonExistentUserId = "+10000000000";

            when(userManagementService.getUserById(nonExistentUserId)).thenReturn(null);

            // when & then
            UsernameNotFoundException exception = assertThrows(
                    UsernameNotFoundException.class,
                    () -> userDetailsService.loadUserByUsername(nonExistentUserId)
            );

            assertEquals("User not found with id: " + nonExistentUserId, exception.getMessage());
            verify(userManagementService, times(1)).getUserById(nonExistentUserId);
            verifyNoInteractions(userPermissionsService);
        }

        @Test
        @DisplayName("Should not call permissions service when user is not found")
        void loadUserByUsernameDoesNotCallPermissionsServiceWhenUserNotFound() {
            // given
            when(userManagementService.getUserById(TEST_USER_ID)).thenReturn(null);

            // when & then
            assertThrows(UsernameNotFoundException.class,
                    () -> userDetailsService.loadUserByUsername(TEST_USER_ID));

            verifyNoInteractions(userPermissionsService);
        }
    }

    @Nested
    @DisplayName("User Status Tests")
    class UserStatusTests {

        @Test
        @DisplayName("Should load active user correctly")
        void loadActiveUser() {
            // given
            User user = createTestUser(TEST_USER_ID, UserStatus.ACTIVE);
            Collection<GrantedAuthority> permissions = Set.of();

            when(userManagementService.getUserById(TEST_USER_ID)).thenReturn(user);
            when(userPermissionsService.getUserPermissions(TEST_USER_ID)).thenReturn(permissions);

            // when
            UserDetails result = userDetailsService.loadUserByUsername(TEST_USER_ID);

            // then
            assertTrue(result.isAccountNonExpired());
            assertTrue(result.isAccountNonLocked());
            assertTrue(result.isCredentialsNonExpired());
        }

        @Test
        @DisplayName("Should load suspended user correctly")
        void loadSuspendedUser() {
            // given
            User user = createTestUser(TEST_USER_ID, UserStatus.SUSPENDED);
            Collection<GrantedAuthority> permissions = Set.of();

            when(userManagementService.getUserById(TEST_USER_ID)).thenReturn(user);
            when(userPermissionsService.getUserPermissions(TEST_USER_ID)).thenReturn(permissions);

            // when
            UserDetails result = userDetailsService.loadUserByUsername(TEST_USER_ID);

            // then
            assertFalse(result.isAccountNonExpired());
            assertFalse(result.isAccountNonLocked());
            assertFalse(result.isCredentialsNonExpired());
        }

        @Test
        @DisplayName("Should load disabled user correctly")
        void loadDisabledUser() {
            // given
            User user = createTestUser(TEST_USER_ID, UserStatus.DISABLED);
            Collection<GrantedAuthority> permissions = Set.of();

            when(userManagementService.getUserById(TEST_USER_ID)).thenReturn(user);
            when(userPermissionsService.getUserPermissions(TEST_USER_ID)).thenReturn(permissions);

            // when
            UserDetails result = userDetailsService.loadUserByUsername(TEST_USER_ID);

            // then
            assertTrue(result.isAccountNonExpired());
            assertTrue(result.isAccountNonLocked());
            assertTrue(result.isCredentialsNonExpired());
        }
    }

    @Nested
    @DisplayName("Different User IDs Tests")
    class DifferentUserIdsTests {

        @Test
        @DisplayName("Should load different users with different user IDs")
        void loadDifferentUsers() {
            // given
            String userId1 = "+12345678901";
            String userId2 = "+10987654321";

            User user1 = createTestUser(userId1, UserStatus.ACTIVE);
            User user2 = createTestUser(userId2, UserStatus.ACTIVE);

            Collection<GrantedAuthority> permissions1 = Set.of(new SimpleGrantedAuthority("ROLE_USER"));
            Collection<GrantedAuthority> permissions2 = Set.of(new SimpleGrantedAuthority("ROLE_ADMIN"));

            when(userManagementService.getUserById(userId1)).thenReturn(user1);
            when(userManagementService.getUserById(userId2)).thenReturn(user2);
            when(userPermissionsService.getUserPermissions(userId1)).thenReturn(permissions1);
            when(userPermissionsService.getUserPermissions(userId2)).thenReturn(permissions2);

            // when
            UserDetails result1 = userDetailsService.loadUserByUsername(userId1);
            UserDetails result2 = userDetailsService.loadUserByUsername(userId2);

            // then
            assertEquals(userId1, result1.getUsername());
            assertEquals(userId2, result2.getUsername());
            assertTrue(result1.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
            assertTrue(result2.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        }

    }

    @Nested
    @DisplayName("Service Interaction Tests")
    class ServiceInteractionTests {

        @Test
        @DisplayName("Should call services in correct order")
        void callsServicesInCorrectOrder() {
            // given
            User user = createTestUser(TEST_USER_ID, UserStatus.ACTIVE);
            Collection<GrantedAuthority> permissions = Set.of();

            when(userManagementService.getUserById(TEST_USER_ID)).thenReturn(user);
            when(userPermissionsService.getUserPermissions(TEST_USER_ID)).thenReturn(permissions);

            // when
            userDetailsService.loadUserByUsername(TEST_USER_ID);

            // then
            var inOrder = inOrder(userManagementService, userPermissionsService);
            inOrder.verify(userManagementService).getUserById(TEST_USER_ID);
            inOrder.verify(userPermissionsService).getUserPermissions(TEST_USER_ID);
        }

        @Test
        @DisplayName("Should call each service exactly once")
        void callsEachServiceOnce() {
            // given
            User user = createTestUser(TEST_USER_ID, UserStatus.ACTIVE);
            Collection<GrantedAuthority> permissions = Set.of();

            when(userManagementService.getUserById(TEST_USER_ID)).thenReturn(user);
            when(userPermissionsService.getUserPermissions(TEST_USER_ID)).thenReturn(permissions);

            // when
            userDetailsService.loadUserByUsername(TEST_USER_ID);

            // then
            verify(userManagementService, times(1)).getUserById(TEST_USER_ID);
            verify(userPermissionsService, times(1)).getUserPermissions(TEST_USER_ID);
        }
    }
}
