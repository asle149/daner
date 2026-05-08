package com.daner.comment.dto;

import com.daner.comment.entity.Comment;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthorDto(String type, Long id, String nickname, String label) {

    public static AuthorDto user(Long id, String nickname) {
        return new AuthorDto("user", id, nickname, null);
    }

    public static AuthorDto anonymous(String label) {
        return new AuthorDto("anonymous", null, null, label);
    }

    public static AuthorDto from(Comment comment) {
        if (comment.getAnonymousLabel() != null) {
            return anonymous(comment.getAnonymousLabel());
        }
        if (comment.getUser() != null) {
            return user(comment.getUser().getId(), comment.getUser().getNickname());
        }
        return anonymous("익명");
    }
}
