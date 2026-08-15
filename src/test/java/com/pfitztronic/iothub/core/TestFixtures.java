package com.pfitztronic.iothub.core;

import com.pfitztronic.iothub.core.accounts.models.PhoneNumber;
import com.pfitztronic.iothub.core.accounts.models.User;
import com.pfitztronic.iothub.core.accounts.models.Account;
import com.pfitztronic.iothub.core.accounts.models.AccountName;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Centralized test fixtures and helper methods for IoT Hub core tests.
 * Contains reusable test constants, builders, and mock factory methods.
 */
public class TestFixtures {

    // Test Secret Constants
    public static final String TEST_SECRET = "mySecretKeyForTestingPurposesThatIsLongEnough256Bits!";

    // Test User IDs / Phone Numbers
    public static final String TEST_USER_ID = "+12345678901";
    public static final String TEST_PHONE_NUMBER = "+1234567890123";
    public static final String TEST_PHONE_NUMBER_2 = "+19876543210";
    public static final String TEST_PHONE_NUMBER_3 = "+14155552671";

    // Test Credentials
    public static final String TEST_PASSWORD = "Password123!";
    public static final String TEST_SECURE_PASSWORD = "Secure@Password123";
    public static final String TEST_PASSWORD_HASH = "694d939ae6e91fd93e43eb276b0fc3f77bc85454ad74cd46663b53d058064858";

    // Test User Names
    public static final String TEST_USER_NAME = "Test User";
    public static final String TEST_USER_NAME_2 = "Another User";

    // Test Email Addresses
    public static final String TEST_EMAIL = "test@example.com";
    public static final String TEST_EMAIL_2 = "another@example.com";

    // Test Account Data
    public static final String TEST_ACCOUNT_NAME = "test account";
    public static final String TEST_ACCOUNT_NAME_2 = "another account";

    // JWT/Session Constants
    public static final String TEST_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";
    public static final String TEST_USER_AGENT_2 = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)";
    public static final String VALID_JWT_TOKEN = "valid.jwt.token";
    public static final String INVALID_JWT_TOKEN = "invalid.jwt.token";
    public static final String GENERATED_JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
    public static final long JWT_EXPIRATION_SECONDS = 3600L; // 1 hour

    // Fixed Instant Values for Deterministic Tests
    // Using 2099 to ensure tests don't fail due to dates being in the past
    public static final Instant FIXED_INSTANT = Instant.parse("2099-07-17T14:00:00Z");
    public static final Instant FIXED_INSTANT_PAST = Instant.parse("2099-07-17T12:00:00Z");
    public static final Instant FIXED_INSTANT_FUTURE = Instant.parse("2099-07-17T16:00:00Z");

    // Code Generation Constants
    public static final String TEST_VERIFICATION_CODE = "987654";
    public static final String TEST_VERIFICATION_CODE_HASH = "hashed987654";
    public static final String TEST_RESET_CODE = "123456";
    public static final String TEST_RESET_CODE_HASH = "hashedCode123456";

    /**
     * Create a test User with default values
     */
    public static User createTestUser() {
        return createTestUser(TEST_USER_ID, TEST_USER_NAME, TEST_PASSWORD_HASH);
    }

    /**
     * Create a test User with custom values
     */
    public static User createTestUser(String phoneNumber, String name, String passwordHash) {
        return User.builder()
                .userId(new PhoneNumber(phoneNumber))
                .name(name)
                .passwordHash(passwordHash)
                .createdAt(FIXED_INSTANT)
                .build();
    }

    /**
     * Create a test Account with default values
     */
    public static Account createTestAccount() {
        return createTestAccount(UUID.randomUUID(), TEST_ACCOUNT_NAME, createTestUser());
    }

    /**
     * Create a test Account with custom values
     */
    public static Account createTestAccount(UUID accountId, String accountName, User admin) {
        return Account.builder()
                .accountId(accountId)
                .accountName(new AccountName(accountName))
                .adminId(admin.getUserId())
                .createdAt(FIXED_INSTANT)
                .build();
    }

    /**
     * Create a mock Authentication for testing authentication flows
     *
     * @param userId   the user ID/principal
     * @param isAuthenticated whether the authentication is authenticated
     * @return configured Authentication mock
     */
    public static Authentication createMockAuthentication(String userId, boolean isAuthenticated) {
        if (!isAuthenticated) {
            return new UsernamePasswordAuthenticationToken(userId, null);
        }

        Collection<GrantedAuthority> authorities = isAuthenticated ?
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")) :
                Collections.emptyList();

        return new UsernamePasswordAuthenticationToken(
                userId,
                null,
                authorities
        );
    }

    /**
     * Create a mock Authentication with custom authorities
     *
     * @param userId the user ID/principal
     * @param isAuthenticated whether the authentication is authenticated
     * @param authorities custom authorities
     * @return configured Authentication mock
     */
    public static Authentication createMockAuthentication(
            String userId,
            boolean isAuthenticated,
            Collection<? extends GrantedAuthority> authorities) {
        if (!isAuthenticated) {
            return new UsernamePasswordAuthenticationToken(userId, null);
        }

        return new UsernamePasswordAuthenticationToken(
                userId,
                null,
                authorities
        );
    }

    private TestFixtures() {
        // Prevent instantiation
    }
}
