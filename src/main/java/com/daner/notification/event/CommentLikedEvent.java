package com.daner.notification.event;

public record CommentLikedEvent(Long commentId, Long actorUserId) {
}
