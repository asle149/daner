package com.daner.auth.service;

import com.daner.auth.dto.DanerOAuth2User;
import com.daner.common.config.AdminProperties;
import com.daner.user.entity.User;
import com.daner.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final AdminProperties adminProperties;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User original = delegate.loadUser(request);
        String provider = request.getClientRegistration().getRegistrationId();
        String oauthId = original.getName();
        String email = original.getAttribute("email");
        String profileImageUrl = original.getAttribute("picture");

        Optional<User> existing = userRepository.findByOauthProviderAndOauthId(provider, oauthId);
        // 기존 회원이 로그인하면 이메일이 비어 있던 경우 이번 기회에 채워 두고,
        // ADMIN_EMAILS 매칭이면 즉시 승격 (자동 강등은 하지 않음 — 운영 사고 방지)
        existing.ifPresent(user -> {
            user.updateEmailIfChanged(email);
            user.syncRole(adminProperties.isAdminEmail(email));
        });
        Long existingUserId = existing.map(User::getId).orElse(null);

        return new DanerOAuth2User(original, provider, oauthId, email, profileImageUrl, existingUserId);
    }
}
