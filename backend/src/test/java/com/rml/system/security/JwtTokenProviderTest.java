package com.rml.system.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for JwtTokenProvider.
 * No Spring context — pure unit test.
 */
class JwtTokenProviderTest {

    // Must be ≥ 32 ASCII chars for HMAC-SHA256
    private static final String SECRET = "test-secret-key-for-unit-tests-only-minimum-32-characters";
    private static final long EXPIRATION_MS = 3_600_000L;       // 1 hour
    private static final long REFRESH_EXPIRATION_MS = 7_200_000L; // 2 hours

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(SECRET, EXPIRATION_MS, REFRESH_EXPIRATION_MS);
    }

    @Test
    @DisplayName("generateAccessToken returns a non-blank token")
    void generateAccessToken_returnsNonBlankToken() {
        String token = provider.generateAccessToken("alice");
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("getSubject extracts the correct username from a valid token")
    void getSubject_extractsCorrectSubject() {
        String token = provider.generateAccessToken("alice");
        assertThat(provider.getSubject(token)).isEqualTo("alice");
    }

    @Test
    @DisplayName("validateToken returns true for a freshly issued token")
    void validateToken_trueForFreshToken() {
        String token = provider.generateAccessToken("alice");
        assertThat(provider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("validateToken returns false for a tampered token")
    void validateToken_falseForTamperedToken() {
        String token = provider.generateAccessToken("alice");
        // Corrupt the signature segment
        String tampered = token.substring(0, token.lastIndexOf('.') + 1) + "invalidsig";
        assertThat(provider.validateToken(tampered)).isFalse();
    }

    @Test
    @DisplayName("validateToken returns false for an expired token")
    void validateToken_falseForExpiredToken() {
        JwtTokenProvider shortLived = new JwtTokenProvider(SECRET, 1L, 1L); // 1ms TTL
        String token = shortLived.generateAccessToken("alice");
        // Token is already expired by the time we check
        try {
            Thread.sleep(5);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        assertThat(shortLived.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("generateRefreshToken produces a different token from access token")
    void generateRefreshToken_differentFromAccessToken() {
        String access = provider.generateAccessToken("bob");
        String refresh = provider.generateRefreshToken("bob");
        assertThat(access).isNotEqualTo(refresh);
    }

    @Test
    @DisplayName("refresh token carries the same subject")
    void refreshToken_sameSubject() {
        String refresh = provider.generateRefreshToken("charlie");
        assertThat(provider.getSubject(refresh)).isEqualTo("charlie");
    }
}
