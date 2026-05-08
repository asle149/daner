package com.daner.auth.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class DanerOAuth2User implements OAuth2User {

    private final OAuth2User delegate;
    private final String oauthProvider;
    private final String oauthId;
    private final String profileImageUrl;
    private final Long existingUserId;

    public DanerOAuth2User(OAuth2User delegate, String oauthProvider, String oauthId,
                           String profileImageUrl, Long existingUserId) {
        this.delegate = delegate;
        this.oauthProvider = oauthProvider;
        this.oauthId = oauthId;
        this.profileImageUrl = profileImageUrl;
        this.existingUserId = existingUserId;
    }

    public String getOauthProvider() {
        return oauthProvider;
    }

    public String getOauthId() {
        return oauthId;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public Long getExistingUserId() {
        return existingUserId;
    }

    public boolean isNewUser() {
        return existingUserId == null;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return delegate.getAuthorities();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }
}
