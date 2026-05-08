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
        Actor actor,
        String preview,
        boolean isRead,
        LocalDateTime createdAt
) {

    public static NotificationResponse from(Notification n) {
        Actor actor = n.getActorUser() != null
                ? new Actor(n.getActorUser().getNickname(), null)
                : new Actor(null, n.getActorLabel());
        return new NotificationResponse(
                n.getId(),
                n.getType().name().toLowerCase(),
                n.getWord().getWord(),
                n.getComment().getId(),
                actor,
                n.getPreview(),
                n.isRead(),
                n.getCreatedAt());
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Actor(String nickname, String label) {
    }
}
