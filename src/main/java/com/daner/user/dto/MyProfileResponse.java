package com.daner.user.dto;

import com.daner.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public record MyProfileResponse(UserSummary user, List<BookshelfWord> myWords, String nextCursor) {

    public record UserSummary(Long id, String nickname, String profileImageUrl) {

        public static UserSummary from(User user) {
            return new UserSummary(user.getId(), user.getNickname(), user.getProfileImageUrl());
        }
    }

    public record BookshelfWord(Long id, String word, int myCommentCount, LocalDateTime lastActivityAt) {
    }
}
