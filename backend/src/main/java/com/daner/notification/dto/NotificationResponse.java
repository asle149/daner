package com.daner.notification.dto;

import com.daner.notification.entity.Notification;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record NotificationResponse(
        Long id,
        String type,
        String word,
        Long commentId,
        Long parentCommentId,
        Actor actor,
        String preview,
        String commentPreview,
        boolean isRead,
        LocalDateTime createdAt
) {

    private static final int COMMENT_PREVIEW_MAX = 80;

    public static NotificationResponse from(Notification n) {
        // 익명으로 작성됐으면(actorLabel != null) 닉네임을 절대 노출하지 않음.
        Actor actor = n.getActorLabel() != null
                ? new Actor(null, n.getActorLabel())
                : n.getActorUser() != null
                        ? new Actor(n.getActorUser().getNickname(), null)
                        : new Actor(null, null);
        // commentPreview 는 받은 사람 입장에서 "내 댓글" — 답글이면 부모, 좋아요면 그 댓글
        String myCommentContent = n.getComment().getParent() != null
                ? n.getComment().getParent().getContent()
                : n.getComment().getContent();
        Long parentCommentId = n.getComment().getParent() != null
                ? n.getComment().getParent().getId()
                : null;
        return new NotificationResponse(
                n.getId(),
                n.getType().name().toLowerCase(),
                n.getWord().getWord(),
                n.getComment().getId(),
                parentCommentId,
                actor,
                n.getPreview(),
                truncate(myCommentContent),
                n.isRead(),
                n.getCreatedAt());
    }

    private static String truncate(String s) {
        if (s == null) return null;
        return s.length() <= COMMENT_PREVIEW_MAX ? s : s.substring(0, COMMENT_PREVIEW_MAX);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Actor(String nickname, String label) {
    }
}
