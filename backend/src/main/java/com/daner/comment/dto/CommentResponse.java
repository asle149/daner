package com.daner.comment.dto;

import com.daner.comment.entity.Comment;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String content,
        AuthorDto author,
        int likeCount,
        boolean isLiked,
        boolean isMine,
        int replyCount,
        LocalDateTime createdAt
) {

    public static CommentResponse of(Comment comment, boolean isLiked, boolean isMine, int replyCount) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                AuthorDto.from(comment),
                comment.getLikeCount(),
                isLiked,
                isMine,
                replyCount,
                comment.getCreatedAt());
    }
}
