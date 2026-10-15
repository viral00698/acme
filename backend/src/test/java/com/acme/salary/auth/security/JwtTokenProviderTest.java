package com.acme.salary.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long expirationMs = 60000; // 1 minute

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(secret, expirationMs);
    }

    @Test
    @DisplayName("Successfully generate, parse, and validate a valid JWT")
    void testGenerateAndValidateToken() {
        UserPrincipal principal = new UserPrincipal(
                1L, "testuser", "test@acme.corp", "hashedpass", true,
                List.of(new SimpleGrantedAuthority("ROLE_HR_MANAGER"))
        );

        String token = tokenProvider.generateToken(principal);
        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals("testuser", tokenProvider.getUsernameFromToken(token));
        assertEquals(1L, tokenProvider.getUserIdFromToken(token));
    }

    @Test
    @DisplayName("Reject expired JWT token")
    void testExpiredToken() throws InterruptedException {
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(secret, 1); // 1 ms expiry
        UserPrincipal principal = new UserPrincipal(
                1L, "expiredUser", "exp@acme.corp", "pass", true,
                List.of(new SimpleGrantedAuthority("ROLE_HR_VIEWER"))
        );

        String token = shortLivedProvider.generateToken(principal);
        Thread.sleep(10); // Wait for expiration

        assertFalse(shortLivedProvider.validateToken(token));
    }

    @Test
    @DisplayName("Reject tampered JWT token")
    void testTamperedToken() {
        UserPrincipal principal = new UserPrincipal(
                1L, "user", "u@acme.corp", "pass", true,
                List.of(new SimpleGrantedAuthority("ROLE_HR_ADMIN"))
        );

        String token = tokenProvider.generateToken(principal);
        String tamperedToken = token.substring(0, token.length() - 5) + "abcde";

        assertFalse(tokenProvider.validateToken(tamperedToken));
    }
}
