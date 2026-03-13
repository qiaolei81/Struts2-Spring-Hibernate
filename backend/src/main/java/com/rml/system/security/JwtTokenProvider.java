package com.rml.system.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT creation and validation.
 * Uses JJWT 0.12.x API (Jwts.builder(), Jwts.parser()).
 * ADR-9: stateless JWT replaces session-based TONLINE tracking.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final String rawSecret;
    private final SecretKey signingKey;
    private final long expirationMs;
    private final long refreshExpirationMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs,
            @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        this.rawSecret = secret;
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    /**
     * Rejects the shipped placeholder secret at startup so the application
     * cannot accidentally run in production with a well-known key.
     * Called automatically by Spring after construction; also callable directly in tests.
     */
    @PostConstruct
    void init() {
        if (rawSecret.contains("replace-in-production")) {
            throw new IllegalStateException(
                    "app.jwt.secret is the default placeholder value. " +
                    "Set APP_JWT_SECRET (or app.jwt.secret) to a secure random key before deployment.");
        }
    }

    /**
     * Generates a signed access token embedding the subject (login name).
     */
    public String generateAccessToken(String subject) {
        return buildToken(subject, expirationMs);
    }

    /**
     * Generates a longer-lived refresh token.
     */
    public String generateRefreshToken(String subject) {
        return buildToken(subject, refreshExpirationMs);
    }

    private String buildToken(String subject, long ttlMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ttlMs);
        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Extracts the subject (login name) from a valid token.
     *
     * @throws JwtException if the token is invalid or expired
     */
    public String getSubject(String token) {
        return parseClaimsJws(token).getPayload().getSubject();
    }

    /**
     * Returns true iff the token signature is valid and the token has not expired.
     */
    public boolean validateToken(String token) {
        try {
            parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.debug("JWT expired: {}", ex.getMessage());
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("JWT invalid: {}", ex.getMessage());
        }
        return false;
    }

    private Jws<Claims> parseClaimsJws(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token);
    }
}
