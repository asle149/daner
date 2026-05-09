package com.daner.notification.service;

import com.daner.comment.entity.Comment;
import com.daner.comment.repository.CommentRepository;
import com.daner.notification.entity.Notification;
import com.daner.notification.entity.NotificationType;
import com.daner.notification.event.CommentLikedEvent;
import com.daner.notification.event.ReplyCreatedEvent;
import com.daner.notification.repository.NotificationRepository;
import com.daner.user.entity.User;
import com.daner.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private static final int PREVIEW_MAX = 100;

    private final NotificationRepository notificationRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onReplyCreated(ReplyCreatedEvent event) {
        Comment parent = commentRepository.findById(event.parentCommentId()).orElse(null);
        Comment reply = commentRepository.findById(event.replyCommentId()).orElse(null);
        if (parent == null || reply == null) {
            return;
        }
        if (parent.getUser() == null) {
            return; // anonymous parent -> no recipient
        }
        if (reply.getUser() != null && reply.getUser().getId().equals(parent.getUser().getId())) {
            return; // do not notify self
        }
        // 답글 작성자가 익명을 체크해서 남겼으면 actorUser는 숨김 — 받은 사람이 누군지 추적 못함
        boolean anonymousReply = reply.getAnonymousLabel() != null;
        notificationRepository.save(Notification.builder()
                .user(parent.getUser())
                .type(NotificationType.REPLY)
                .word(parent.getWord())
                .comment(parent)
                .actorUser(anonymousReply ? null : reply.getUser())
                .actorLabel(reply.getAnonymousLabel())
                .preview(truncate(reply.getContent()))
                .build());
    }

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onCommentLiked(CommentLikedEvent event) {
        Comment comment = commentRepository.findById(event.commentId()).orElse(null);
        if (comment == null || comment.getUser() == null) {
            return;
        }
        if (event.actorUserId() != null && event.actorUserId().equals(comment.getUser().getId())) {
            return; // do not notify self
        }
        User actor = event.actorUserId() != null
                ? userRepository.findById(event.actorUserId()).orElse(null)
                : null;
        // 좋아요는 actor를 화면에 노출하지 않지만(프론트 정책), 향후 운영용으로 actorUser 자체는 보관
        notificationRepository.save(Notification.builder()
                .user(comment.getUser())
                .type(NotificationType.LIKE)
                .word(comment.getWord())
                .comment(comment)
                .actorUser(actor)
                .build());
    }

    private static String truncate(String s) {
        if (s == null) return null;
        return s.length() <= PREVIEW_MAX ? s : s.substring(0, PREVIEW_MAX);
    }
}
