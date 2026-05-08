package com.daner.like.service;

import com.daner.comment.entity.Comment;
import com.daner.comment.repository.CommentRepository;
import com.daner.common.exception.BusinessException;
import com.daner.common.exception.ErrorCode;
import com.daner.common.ratelimit.RateLimiter;
import com.daner.like.dto.LikeStateResponse;
import com.daner.like.entity.CommentLike;
import com.daner.like.repository.CommentLikeRepository;
import com.daner.notification.event.CommentLikedEvent;
import com.daner.user.entity.User;
import com.daner.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RateLimiter rateLimiter;

    @Transactional
    public LikeStateResponse like(Long commentId, Long userId) {
        rateLimiter.checkMemberLike(userId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
        if (commentLikeRepository.existsByUserIdAndCommentId(userId, commentId)) {
            throw new BusinessException(ErrorCode.ALREADY_LIKED);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        commentLikeRepository.save(CommentLike.builder().user(user).comment(comment).build());
        comment.increaseLikeCount();
        comment.getWord().increaseLikeCount();
        eventPublisher.publishEvent(new CommentLikedEvent(commentId, userId));
        return new LikeStateResponse(comment.getLikeCount(), true);
    }

    @Transactional
    public LikeStateResponse unlike(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
        CommentLike like = commentLikeRepository.findByUserIdAndCommentId(userId, commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LIKE_NOT_FOUND));
        commentLikeRepository.delete(like);
        comment.decreaseLikeCount();
        comment.getWord().decreaseLikeCount();
        return new LikeStateResponse(comment.getLikeCount(), false);
    }
}
