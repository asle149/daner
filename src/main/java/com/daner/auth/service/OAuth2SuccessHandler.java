package com.daner.auth.service;

import com.daner.auth.dto.DanerOAuth2User;
import com.daner.common.exception.BusinessException;
import com.daner.common.exception.ErrorCode;
import com.daner.user.entity.User;
import com.daner.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        DanerOAuth2User principal = (DanerOAuth2User) authentication.getPrincipal();
        String redirectUrl = principal.isNewUser()
                ? buildSignupRedirect(principal)
                : buildSuccessRedirect(principal.getExistingUserId());
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private String buildSignupRedirect(DanerOAuth2User principal) {
        String signupToken = jwtTokenProvider.createSignupToken(
                principal.getOauthProvider(),
                principal.getOauthId(),
                principal.getProfileImageUrl());
        return UriComponentsBuilder.fromUriString(frontendUrl)
                .path("/auth/signup")
                .queryParam("signup_token", signupToken)
                .build()
                .toUriString();
    }

    private String buildSuccessRedirect(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        String accessToken = jwtTokenProvider.createAccessToken(userId);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);
        refreshTokenService.store(user, refreshToken);
        return UriComponentsBuilder.fromUriString(frontendUrl)
                .path("/auth/success")
                .queryParam("access_token", accessToken)
                .queryParam("refresh_token", refreshToken)
                .build()
                .toUriString();
    }
}
