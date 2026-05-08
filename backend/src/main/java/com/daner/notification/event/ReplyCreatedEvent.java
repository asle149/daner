package com.daner.notification.event;

public record ReplyCreatedEvent(Long parentCommentId, Long replyCommentId) {
}
