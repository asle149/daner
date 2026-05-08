package com.daner.auth.dto;

public record SignupTokenPayload(
        String oauthProvider,
        String oauthId,
        String profileImageUrl
) {
}
