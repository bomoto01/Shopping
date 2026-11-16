package com.shopping;

import com.shopping.enums.RoleType;
import com.shopping.enums.UserStatus;
import com.shopping.security.JwtTokenProvider;
import com.shopping.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTests {

    private JwtTokenProvider jwtTokenProvider;
    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long ACCESS_EXPIRATION_MS = 60000; // 1 min
    private static final long REFRESH_EXPIRATION_MS = 600000; // 10 mins

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(TEST_SECRET, ACCESS_EXPIRATION_MS, REFRESH_EXPIRATION_MS);
    }

    @Test
    @DisplayName("Test generating and validating valid JWT access token")
    void testGenerateAndValidateAccessToken() {
        UserPrincipal principal = UserPrincipal.builder()
                .userId(100L)
                .email("customer@test.com")
                .fullName("Nguyễn Văn A")
                .status(UserStatus.ACTIVE)
                .authorities(List.of(new SimpleGrantedAuthority(RoleType.ROLE_CUSTOMER.name())))
                .build();

        String token = jwtTokenProvider.generateAccessToken(principal);
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("customer@test.com", jwtTokenProvider.getEmailFromToken(token));
        assertEquals(100L, jwtTokenProvider.getUserIdFromToken(token));
    }

    @Test
    @DisplayName("Test token validation with invalid or tampered token")
    void testInvalidTokenValidation() {
        assertFalse(jwtTokenProvider.validateToken("invalid.jwt.token"));
        assertFalse(jwtTokenProvider.validateToken(""));
        assertFalse(jwtTokenProvider.validateToken(null));
    }

    @Test
    @DisplayName("Test SHA-256 token hashing for secure refresh token storage")
    void testTokenHashing() {
        String rawToken = "my-secret-random-refresh-token";
        String hash1 = jwtTokenProvider.hashToken(rawToken);
        String hash2 = jwtTokenProvider.hashToken(rawToken);

        assertNotNull(hash1);
        assertEquals(hash1, hash2);
        assertNotEquals(rawToken, hash1);
        assertEquals(64, hash1.length()); // SHA-256 is 64 hex characters
    }
}
