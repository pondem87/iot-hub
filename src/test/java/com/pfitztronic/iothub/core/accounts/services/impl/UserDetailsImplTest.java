package com.pfitztronic.iothub.core.accounts.services.impl;

import com.pfitztronic.iothub.core.accounts.models.PhoneNumber;
import com.pfitztronic.iothub.core.accounts.models.User;
import com.pfitztronic.iothub.core.accounts.models.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserDetailsImpl Tests")
class UserDetailsImplTest {

    private static final String TEST_USER_ID = "+12345678901";
    private static final String TEST_NAME = "Test User";
    private static final String TEST_PASSWORD_HASH = "hashedPassword123";

    private User createUser(UserStatus status) {
        return User.builder()
                .userId(new PhoneNumber(TEST_USER_ID))
                .name(TEST_NAME)
                .passwordHash(TEST_PASSWORD_HASH)
                .createdAt(Instant.now())
                .status(status)
                .verified(true)
                .build();
    }

    @Nested
    @DisplayName("Basic Properties Tests")
    class BasicPropertiesTests {

        @Test
        @DisplayName("Should return correct username from phone number")
        void getUsernameReturnsPhoneNumber() {
            // given
            User user = createUser(UserStatus.ACTIVE);
            Collection<GrantedAuthority> permissions = Set.of();
            UserDetailsImpl userDetails = new UserDetailsImpl(user, permissions);

            // when
            String username = userDetails.getUsername();

            // then
            assertEquals(TEST_USER_ID, username);
        }

        @Test
        @DisplayName("Should return password hash as password")
        void getPasswordReturnsPasswordHash() {
            // given
            User user = createUser(UserStatus.ACTIVE);
            Collection<GrantedAuthority> permissions = Set.of();
            UserDetailsImpl userDetails = new UserDetailsImpl(user, permissions);

            // when
            String password = userDetails.getPassword();

            // then
            assertEquals(TEST_PASSWORD_HASH, password);
        }
    }

    @Nested
    @DisplayName("Authorities Tests")
    class AuthoritiesTests {

        @Test
        @DisplayName("Should return empty authorities when no permissions")
        void getAuthoritiesReturnsEmptyWhenNoPermissions() {
            // given
            User user = createUser(UserStatus.ACTIVE);
            Collection<GrantedAuthority> permissions = Set.of();
            UserDetailsImpl userDetails = new UserDetailsImpl(user, permissions);

            // when
            Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

            // then
            assertTrue(authorities.isEmpty());
        }

        @Test
        @DisplayName("Should return all permissions as authorities")
        void getAuthoritiesReturnsAllPermissions() {
            // given
            User user = createUser(UserStatus.ACTIVE);
            Collection<GrantedAuthority> permissions = Set.of(
                    new SimpleGrantedAuthority("ROLE_USER"),
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("PERMISSION_READ")
            );
            UserDetailsImpl userDetails = new UserDetailsImpl(user, permissions);

            // when
            Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

            // then
            assertEquals(3, authorities.size());
            assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
            assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
            assertTrue(authorities.contains(new SimpleGrantedAuthority("PERMISSION_READ")));
        }

        @Test
        @DisplayName("Should return single authority correctly")
        void getAuthoritiesReturnsSingleAuthority() {
            // given
            User user = createUser(UserStatus.ACTIVE);
            Collection<GrantedAuthority> permissions = Set.of(
                    new SimpleGrantedAuthority("ROLE_USER")
            );
            UserDetailsImpl userDetails = new UserDetailsImpl(user, permissions);

            // when
            Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

            // then
            assertEquals(1, authorities.size());
            assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
        }
    }

    @Nested
    @DisplayName("Account Status Tests - Active User")
    class ActiveUserStatusTests {

        @Test
        @DisplayName("Should return true for isAccountNonExpired when user is ACTIVE")
        void isAccountNonExpiredReturnsTrueForActiveUser() {
            // given
            User user = createUser(UserStatus.ACTIVE);
            UserDetailsImpl userDetails = new UserDetailsImpl(user, Set.of());

            // when
            boolean isNonExpired = userDetails.isAccountNonExpired();

            // then
            assertTrue(isNonExpired);
        }

        @Test
        @DisplayName("Should return true for isAccountNonLocked when user is ACTIVE")
        void isAccountNonLockedReturnsTrueForActiveUser() {
            // given
            User user = createUser(UserStatus.ACTIVE);
            UserDetailsImpl userDetails = new UserDetailsImpl(user, Set.of());

            // when
            boolean isNonLocked = userDetails.isAccountNonLocked();

            // then
            assertTrue(isNonLocked);
        }

        @Test
        @DisplayName("Should return true for isCredentialsNonExpired when user is ACTIVE")
        void isCredentialsNonExpiredReturnsTrueForActiveUser() {
            // given
            User user = createUser(UserStatus.ACTIVE);
            UserDetailsImpl userDetails = new UserDetailsImpl(user, Set.of());

            // when
            boolean isCredentialsNonExpired = userDetails.isCredentialsNonExpired();

            // then
            assertTrue(isCredentialsNonExpired);
        }

        @Test
        @DisplayName("Should return true for isEnabled when user is ACTIVE")
        void isEnabledReturnsTrueForActiveUser() {
            // given
            User user = createUser(UserStatus.ACTIVE);
            UserDetailsImpl userDetails = new UserDetailsImpl(user, Set.of());

            // when
            boolean isEnabled = userDetails.isEnabled();

            // then
            assertTrue(isEnabled);
        }
    }

    @Nested
    @DisplayName("Account Status Tests - Suspended User")
    class SuspendedUserStatusTests {

        @Test
        @DisplayName("Should return false for isAccountNonExpired when user is SUSPENDED")
        void isAccountNonExpiredReturnsFalseForSuspendedUser() {
            // given
            User user = createUser(UserStatus.SUSPENDED);
            UserDetailsImpl userDetails = new UserDetailsImpl(user, Set.of());

            // when
            boolean isNonExpired = userDetails.isAccountNonExpired();

            // then
            assertFalse(isNonExpired);
        }

        @Test
        @DisplayName("Should return false for isAccountNonLocked when user is SUSPENDED")
        void isAccountNonLockedReturnsFalseForSuspendedUser() {
            // given
            User user = createUser(UserStatus.SUSPENDED);
            UserDetailsImpl userDetails = new UserDetailsImpl(user, Set.of());

            // when
            boolean isNonLocked = userDetails.isAccountNonLocked();

            // then
            assertFalse(isNonLocked);
        }

        @Test
        @DisplayName("Should return false for isCredentialsNonExpired when user is SUSPENDED")
        void isCredentialsNonExpiredReturnsFalseForSuspendedUser() {
            // given
            User user = createUser(UserStatus.SUSPENDED);
            UserDetailsImpl userDetails = new UserDetailsImpl(user, Set.of());

            // when
            boolean isCredentialsNonExpired = userDetails.isCredentialsNonExpired();

            // then
            assertFalse(isCredentialsNonExpired);
        }
    }

    @Nested
    @DisplayName("Account Status Tests - Disabled User")
    class DisabledUserStatusTests {

        @Test
        @DisplayName("Should return true for isAccountNonExpired when user is DISABLED")
        void isAccountNonExpiredReturnsTrueForDisabledUser() {
            // given
            User user = createUser(UserStatus.DISABLED);
            UserDetailsImpl userDetails = new UserDetailsImpl(user, Set.of());

            // when
            boolean isNonExpired = userDetails.isAccountNonExpired();

            // then
            assertTrue(isNonExpired);
        }

        @Test
        @DisplayName("Should return true for isAccountNonLocked when user is DISABLED")
        void isAccountNonLockedReturnsTrueForDisabledUser() {
            // given
            User user = createUser(UserStatus.DISABLED);
            UserDetailsImpl userDetails = new UserDetailsImpl(user, Set.of());

            // when
            boolean isNonLocked = userDetails.isAccountNonLocked();

            // then
            assertTrue(isNonLocked);
        }

        @Test
        @DisplayName("Should return true for isCredentialsNonExpired when user is DISABLED")
        void isCredentialsNonExpiredReturnsTrueForDisabledUser() {
            // given
            User user = createUser(UserStatus.DISABLED);
            UserDetailsImpl userDetails = new UserDetailsImpl(user, Set.of());

            // when
            boolean isCredentialsNonExpired = userDetails.isCredentialsNonExpired();

            // then
            assertTrue(isCredentialsNonExpired);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle user with null password hash")
        void handlesNullPasswordHash() {
            // given
            User user = User.builder()
                    .userId(new PhoneNumber(TEST_USER_ID))
                    .name(TEST_NAME)
                    .passwordHash(null)
                    .createdAt(Instant.now())
                    .status(UserStatus.ACTIVE)
                    .build();
            UserDetailsImpl userDetails = new UserDetailsImpl(user, Set.of());

            // when
            String password = userDetails.getPassword();

            // then
            assertNull(password);
        }

    }
}
