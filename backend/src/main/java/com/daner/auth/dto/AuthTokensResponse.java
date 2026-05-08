package com.daner.auth.dto;

import com.daner.user.entity.User;

public record AuthTokensResponse(
        UserSummary user,
        String accessToken,
        String refreshToken
) {

    public static AuthTokensResponse of(User user, String accessToken, String refreshToken) {
        return new AuthTokensResponse(UserSummary.from(user), accessToken, refreshToken);
    }

    public record UserSummary(Long id, String nickname, String profileImageUrl) {

        public static UserSummary from(User user) {
            return new UserSummary(user.getId(), user.getNickname(), user.getProfileImageUrl());
        }
    }
}
