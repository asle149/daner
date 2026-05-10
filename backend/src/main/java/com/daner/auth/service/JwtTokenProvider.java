package com.daner.auth.service;

import com.daner.auth.dto.SignupTokenPayload;
import com.daner.common.config.JwtProperties;
import com.daner.common.exception.BusinessException;
import com.daner.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private static final String CLAIM_TYPE = "typ";
    private static final String CLAIM_PROVIDER = "provider";
    private static final String CLAIM_OAUTH_ID = "oauth_id";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_PROFILE_IMAGE = "picture";

    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";
    private static final String TYPE_SIGNUP = "signup";

    private final JwtProperties properties;
    private SecretKey key;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void init() {
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long userId) {
        return buildUserToken(userId, TYPE_ACCESS,
                Duration.ofMinutes(properties.accessTokenValidityMinutes()));
    }

    public String createRefreshToken(Long userId) {
        return buildUserToken(userId, TYPE_REFRESH,
                Duration.ofDays(properties.refreshTokenValidityDays()));
    }

    public String createSignupToken(String oauthProvider, String oauthId, String email, String profileImageUrl) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofMinutes(properties.signupTokenValidityMinutes()));
        return Jwts.builder()
                .subject(oauthProvider + ":" + oauthId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claims(Map.of(
                        CLAIM_TYPE, TYPE_SIGNUP,
                        CLAIM_PROVIDER, oauthProvider,
                        CLAIM_OAUTH_ID, oauthId,
                        CLAIM_EMAIL, email == null ? "" : email,
                        CLAIM_PROFILE_IMAGE, profileImageUrl == null ? "" : profileImageUrl
                ))
                .signWith(key)
                .compact();
    }

    public Long parseAccessToken(String token) {
        return parseUserId(token, TYPE_ACCESS);
    }

    public Long parseRefreshToken(String token) {
        return parseUserId(token, TYPE_REFRESH);
    }

    public SignupTokenPayload parseSignupToken(String token) {
        Claims claims = parseClaims(token);
        requireType(claims, TYPE_SIGNUP);
        String picture = claims.get(CLAIM_PROFILE_IMAGE, String.class);
        String email = claims.get(CLAIM_EMAIL, String.class);
        return new SignupTokenPayload(
                claims.get(CLAIM_PROVIDER, String.class),
                claims.get(CLAIM_OAUTH_ID, String.class),
                email == null || email.isBlank() ? null : email,
                picture == null || picture.isBlank() ? null : picture
        );
    }

    private String buildUserToken(Long userId, String type, Duration ttl) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .claim(CLAIM_TYPE, type)
                .signWith(key)
                .compact();
    }

    private Long parseUserId(String token, String expectedType) {
        Claims claims = parseClaims(token);
        requireType(claims, expectedType);
        try {
            return Long.parseLong(claims.getSubject());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    private static void requireType(Claims claims, String expected) {
        String actual = claims.get(CLAIM_TYPE, String.class);
        if (!expected.equals(actual)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }
}
