package com.daner.comment.dto;

import com.daner.comment.entity.Comment;

import java.time.LocalDateTime;

public record ReplyResponse(
        Long id,
        Long parentId,
        String content,
        AuthorDto author,
        int likeCount,
        boolean isLiked,
        boolean isMine,
        LocalDateTime createdAt
) {

    public static ReplyResponse of(Comment comment, boolean isLiked, boolean isMine) {
        return new ReplyResponse(
                comment.getId(),
                comment.getParent() != null ? comment.getParent().getId() : null,
                comment.getContent(),
                AuthorDto.from(comment),
                comment.getLikeCount(),
                isLiked,
                isMine,
                comment.getCreatedAt());
    }
}
