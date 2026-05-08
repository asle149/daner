package com.daner.auth.service;

import com.daner.auth.dto.DanerOAuth2User;
import com.daner.user.entity.User;
import com.daner.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User original = delegate.loadUser(request);
        String provider = request.getClientRegistration().getRegistrationId();
        String oauthId = original.getName();
        String profileImageUrl = original.getAttribute("picture");

        Optional<User> existing = userRepository.findByOauthProviderAndOauthId(provider, oauthId);
        Long existingUserId = existing.map(User::getId).orElse(null);

        return new DanerOAuth2User(original, provider, oauthId, profileImageUrl, existingUserId);
    }
}
