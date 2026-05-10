package com.daner.auth.service;

import com.daner.auth.dto.SignupTokenPayload;
import com.daner.common.config.JwtProperties;
import com.daner.common.exception.BusinessException;
import com.daner.common.exception.ErrorCode;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-with-at-least-32-bytes-of-content!";

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = build(30, 14, 10);
    }

    @Nested
    class RoundTrip {

        @Test
        void access_token_round_trips_user_id() {
            String token = provider.createAccessToken(42L);

            assertThat(provider.parseAccessToken(token)).isEqualTo(42L);
        }

        @Test
        void refresh_token_round_trips_user_id() {
            String token = provider.createRefreshToken(99L);

            assertThat(provider.parseRefreshToken(token)).isEqualTo(99L);
        }

        @Test
        void signup_token_round_trips_oauth_identity_and_picture() {
            String token = provider.createSignupToken("google", "google-uid-1",
                    "user@example.com", "https://lh3.googleusercontent.com/photo");

            SignupTokenPayload payload = provider.parseSignupToken(token);

            assertThat(payload.oauthProvider()).isEqualTo("google");
            assertThat(payload.oauthId()).isEqualTo("google-uid-1");
            assertThat(payload.email()).isEqualTo("user@example.com");
            assertThat(payload.profileImageUrl()).isEqualTo("https://lh3.googleusercontent.com/photo");
        }

        @Test
        void signup_token_with_null_picture_returns_null() {
            String token = provider.createSignupToken("google", "uid", null, null);

            assertThat(provider.parseSignupToken(token).profileImageUrl()).isNull();
            assertThat(provider.parseSignupToken(token).email()).isNull();
        }
    }

    @Nested
    class TypeChecking {

        @Test
        void parsing_refresh_as_access_throws_invalid_token() {
            String refresh = provider.createRefreshToken(1L);

            assertThatThrownBy(() -> provider.parseAccessToken(refresh))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.INVALID_TOKEN);
        }

        @Test
        void parsing_signup_as_access_throws_invalid_token() {
            String signup = provider.createSignupToken("google", "uid", null, null);

            assertThatThrownBy(() -> provider.parseAccessToken(signup))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.INVALID_TOKEN);
        }

        @Test
        void parsing_access_as_signup_throws_invalid_token() {
            String access = provider.createAccessToken(1L);

            assertThatThrownBy(() -> provider.parseSignupToken(access))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.INVALID_TOKEN);
        }
    }

    @Nested
    class FailureModes {

        @Test
        void expired_access_token_throws_expired_token() throws InterruptedException {
            JwtTokenProvider shortLived = build(0, 0, 0);

            String token = manuallySigned(SECRET,
                    Map.of("typ", "access"),
                    "1",
                    Instant.now().minusSeconds(60),
                    Instant.now().minusSeconds(30));

            assertThatThrownBy(() -> shortLived.parseAccessToken(token))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.EXPIRED_TOKEN);
        }

        @Test
        void tampered_token_signature_throws_invalid_token() {
            String token = provider.createAccessToken(1L);
            String tampered = token.substring(0, token.length() - 4) + "AAAA";

            assertThatThrownBy(() -> provider.parseAccessToken(tampered))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.INVALID_TOKEN);
        }

        @Test
        void garbage_token_throws_invalid_token() {
            assertThatThrownBy(() -> provider.parseAccessToken("not-a-jwt"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.INVALID_TOKEN);
        }

        @Test
        void token_signed_with_different_secret_throws_invalid_token() {
            String otherSecret = "different-secret-with-at-least-32-bytes-of-content!";
            String foreignToken = manuallySigned(otherSecret,
                    Map.of("typ", "access"),
                    "1",
                    Instant.now(),
                    Instant.now().plusSeconds(60));

            assertThatThrownBy(() -> provider.parseAccessToken(foreignToken))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.INVALID_TOKEN);
        }
    }

    private static JwtTokenProvider build(int accessMin, int refreshDays, int signupMin) {
        JwtProperties props = new JwtProperties(SECRET, accessMin, refreshDays, signupMin);
        JwtTokenProvider p = new JwtTokenProvider(props);
        ReflectionTestUtils.invokeMethod(p, "init");
        return p;
    }

    private static String manuallySigned(String secret, Map<String, ?> claims, String subject,
                                         Instant issuedAt, Instant expiresAt) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claims(claims)
                .signWith(key)
                .compact();
    }
}
