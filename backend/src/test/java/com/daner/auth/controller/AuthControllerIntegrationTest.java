package com.daner.auth.controller;

import com.daner.auth.repository.RefreshTokenRepository;
import com.daner.auth.service.JwtTokenProvider;
import com.daner.user.entity.User;
import com.daner.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired private WebApplicationContext context;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private UserRepository userRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void signup_with_valid_signup_token_creates_user_and_returns_tokens() throws Exception {
        String signupToken = jwtTokenProvider.createSignupToken("google", "uid-1",
                "https://lh3.googleusercontent.com/p");

        String body = objectMapper.writeValueAsString(Map.of(
                "signupToken", signupToken,
                "nickname", "감자전"
        ));

        mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.nickname").value("감자전"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());

        assertThat(userRepository.findByOauthProviderAndOauthId("google", "uid-1")).isPresent();
        assertThat(refreshTokenRepository.count()).isEqualTo(1);
    }

    @Test
    void signup_with_duplicate_nickname_returns_409() throws Exception {
        userRepository.save(User.builder()
                .oauthProvider("google").oauthId("existing").nickname("감자전").build());
        String signupToken = jwtTokenProvider.createSignupToken("google", "new-uid", null);

        String body = objectMapper.writeValueAsString(Map.of(
                "signupToken", signupToken,
                "nickname", "감자전"
        ));

        mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_NICKNAME"));
    }

    @Test
    void signup_with_invalid_signup_token_returns_401() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "signupToken", "garbage-token",
                "nickname", "감자전"
        ));

        mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_TOKEN"));
    }

    @Test
    void signup_with_invalid_nickname_returns_400() throws Exception {
        String signupToken = jwtTokenProvider.createSignupToken("google", "uid-1", null);

        String body = objectMapper.writeValueAsString(Map.of(
                "signupToken", signupToken,
                "nickname", "a"
        ));

        mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }

    @Test
    void check_nickname_returns_available_for_unused() throws Exception {
        mockMvc.perform(get("/auth/check-nickname").param("nickname", "햇살"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.available").value(true));
    }

    @Test
    void check_nickname_returns_unavailable_for_taken() throws Exception {
        userRepository.save(User.builder()
                .oauthProvider("google").oauthId("uid").nickname("도파민").build());

        mockMvc.perform(get("/auth/check-nickname").param("nickname", "도파민"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.available").value(false))
                .andExpect(jsonPath("$.data.reason").value("이미 사용 중인 닉네임입니다."));
    }

    @Test
    void check_nickname_returns_unavailable_for_bad_format() throws Exception {
        mockMvc.perform(get("/auth/check-nickname").param("nickname", "a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.available").value(false));
    }

    @Test
    void refresh_with_stored_token_returns_new_access() throws Exception {
        User user = signupAndPersist("uid-r", "햇살");
        String refresh = jwtTokenProvider.createRefreshToken(user.getId());
        // store via service path: simulate by calling /auth/signup flow (already did via helper)
        // for refresh test, store directly
        refreshTokenRepository.save(com.daner.auth.entity.RefreshToken.builder()
                .user(user)
                .tokenHash(sha256(refresh))
                .expiresAt(java.time.LocalDateTime.now().plusDays(14))
                .build());

        String body = objectMapper.writeValueAsString(Map.of("refreshToken", refresh));

        mockMvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    void refresh_with_revoked_token_returns_401() throws Exception {
        User user = signupAndPersist("uid-r2", "도파민");
        String refresh = jwtTokenProvider.createRefreshToken(user.getId());
        // not stored in DB → already revoked / never issued

        String body = objectMapper.writeValueAsString(Map.of("refreshToken", refresh));

        mockMvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_TOKEN"));
    }

    @Test
    void refresh_rejects_access_token() throws Exception {
        User user = signupAndPersist("uid-r3", "햇감자");
        String access = jwtTokenProvider.createAccessToken(user.getId());

        String body = objectMapper.writeValueAsString(Map.of("refreshToken", access));

        mockMvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_TOKEN"));
    }

    @Test
    void logout_with_valid_access_clears_refresh_tokens() throws Exception {
        User user = signupAndPersist("uid-l", "퇴근러");
        String access = jwtTokenProvider.createAccessToken(user.getId());
        refreshTokenRepository.save(com.daner.auth.entity.RefreshToken.builder()
                .user(user)
                .tokenHash(sha256("any-refresh"))
                .expiresAt(java.time.LocalDateTime.now().plusDays(14))
                .build());

        mockMvc.perform(post("/auth/logout").header("Authorization", "Bearer " + access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(refreshTokenRepository.count()).isZero();
    }

    @Test
    void logout_without_token_returns_401() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    private User signupAndPersist(String oauthId, String nickname) {
        return userRepository.save(User.builder()
                .oauthProvider("google").oauthId(oauthId).nickname(nickname).build());
    }

    private static String sha256(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            return java.util.HexFormat.of().formatHex(md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
