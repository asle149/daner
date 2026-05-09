package com.daner.comment.service;

import com.daner.auth.service.AnonymousLabelService;
import com.daner.comment.dto.CommentCreateRequest;
import com.daner.comment.dto.CommentResponse;
import com.daner.comment.dto.CommentSliceResponse;
import com.daner.comment.dto.ReplyResponse;
import com.daner.comment.dto.ReplySliceResponse;
import com.daner.comment.entity.Comment;
import com.daner.comment.repository.CommentRepository;
import com.daner.common.exception.BusinessException;
import com.daner.common.exception.ErrorCode;
import com.daner.common.ratelimit.RateLimiter;
import com.daner.common.util.WordNormalizer;
import com.daner.like.repository.CommentLikeRepository;
import com.daner.notification.event.ReplyCreatedEvent;
import com.daner.user.entity.User;
import com.daner.user.repository.UserRepository;
import com.daner.word.entity.Word;
import com.daner.word.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private static final int MAX_LIMIT = 50;
    private static final int DEFAULT_LIMIT = 20;

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final WordRepository wordRepository;
    private final UserRepository userRepository;
    private final AnonymousLabelService anonymousLabelService;
    private final ApplicationEventPublisher eventPublisher;
    private final RateLimiter rateLimiter;

    @Transactional(readOnly = true)
    public CommentSliceResponse listForWord(String rawWord, String sort, String cursor, Integer limit, Long currentUserId) {
        String normalized = WordNormalizer.normalize(rawWord);
        Optional<Word> word = wordRepository.findByWord(normalized);
        if (word.isEmpty()) {
            return new CommentSliceResponse(List.of(), null);
        }
        int page = parseCursor(cursor);
        int size = clampLimit(limit);
        Pageable pageable = PageRequest.of(page, size);
        Slice<Comment> slice = "popular".equalsIgnoreCase(sort)
                ? commentRepository.findByWordIdAndParentIsNullOrderByLikeCountDescCreatedAtDesc(word.get().getId(), pageable)
                : commentRepository.findByWordIdAndParentIsNullOrderByCreatedAtDesc(word.get().getId(), pageable);
        List<CommentResponse> items = mapComments(slice.getContent(), currentUserId);
        return new CommentSliceResponse(items, slice.hasNext() ? String.valueOf(page + 1) : null);
    }

    @Transactional
    public CommentResponse createTopLevel(String rawWord, CommentCreateRequest request,
                                          Long currentUserId, UUID anonymousToken) {
        applyRateLimit(currentUserId, anonymousToken);
        String normalized = WordNormalizer.normalize(rawWord);
        Word word = wordRepository.findByWord(normalized)
                .orElseGet(() -> wordRepository.save(Word.builder().word(normalized).build()));

        Author author = resolveAuthor(word, currentUserId, anonymousToken, Boolean.TRUE.equals(request.anonymous()));
        Comment comment = commentRepository.save(Comment.builder()
                .word(word)
                .user(author.user)
                .anonymousLabel(author.label)
                .anonymousToken(author.user == null ? anonymousToken : null)
                .content(request.content())
                .build());
        word.increaseCommentCount();
        return CommentResponse.of(comment, false, 0);
    }

    @Transactional
    public ReplyResponse createReply(Long parentId, CommentCreateRequest request,
                                     Long currentUserId, UUID anonymousToken) {
        applyRateLimit(currentUserId, anonymousToken);
        Comment parent = commentRepository.findById(parentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
        if (parent.isReply()) {
            throw new BusinessException(ErrorCode.REPLY_DEPTH_EXCEEDED);
        }
        Word word = parent.getWord();
        Author author = resolveAuthor(word, currentUserId, anonymousToken, Boolean.TRUE.equals(request.anonymous()));
        Comment reply = commentRepository.save(Comment.builder()
                .word(word)
                .user(author.user)
                .parent(parent)
                .anonymousLabel(author.label)
                .anonymousToken(author.user == null ? anonymousToken : null)
                .content(request.content())
                .build());
        word.increaseCommentCount();
        eventPublisher.publishEvent(new ReplyCreatedEvent(parent.getId(), reply.getId()));
        return ReplyResponse.of(reply, false);
    }

    @Transactional
    public void delete(Long commentId, Long currentUserId, UUID anonymousToken) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        boolean canDelete = (comment.getUser() != null && currentUserId != null
                && comment.getUser().getId().equals(currentUserId))
                || comment.wasWrittenByAnonymousToken(anonymousToken);
        if (!canDelete) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (!comment.isReply()) {
            long replyCount = commentRepository.countRepliesByParentIds(Set.of(comment.getId())).stream()
                    .mapToLong(CommentRepository.ParentReplyCount::getCnt).sum();
            if (replyCount > 0) {
                throw new BusinessException(ErrorCode.COMMENT_HAS_REPLIES);
            }
        }
        commentRepository.delete(comment);
    }

    private Author resolveAuthor(Word word, Long currentUserId, UUID anonymousToken, boolean wantsAnonymous) {
        if (currentUserId != null) {
            User user = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            if (wantsAnonymous) {
                return new Author(user, anonymousLabelService.resolveOrAssign(
                        anonymousToken != null ? anonymousToken : UUID.nameUUIDFromBytes(("u" + currentUserId).getBytes()),
                        word));
            }
            return new Author(user, null);
        }
        if (anonymousToken == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인하거나 X-Anonymous-Token 헤더가 필요합니다.");
        }
        String label = anonymousLabelService.resolveOrAssign(anonymousToken, word);
        return new Author(null, label);
    }

    private record Author(User user, String label) {
    }

    private void applyRateLimit(Long currentUserId, UUID anonymousToken) {
        if (currentUserId != null) {
            rateLimiter.checkMemberComment(currentUserId);
        } else if (anonymousToken != null) {
            rateLimiter.checkGuestComment(anonymousToken.toString());
        }
    }

    @Transactional(readOnly = true)
    public ReplySliceResponse listReplies(Long parentId, String cursor, Integer limit, Long currentUserId) {
        int page = parseCursor(cursor);
        int size = clampLimit(limit);
        Pageable pageable = PageRequest.of(page, size);
        Slice<Comment> slice = commentRepository.findByParentIdOrderByCreatedAtAsc(parentId, pageable);
        Set<Long> liked = likedIds(currentUserId, slice.getContent());
        List<ReplyResponse> replies = slice.getContent().stream()
                .map(c -> ReplyResponse.of(c, liked.contains(c.getId())))
                .toList();
        return new ReplySliceResponse(replies, slice.hasNext() ? String.valueOf(page + 1) : null);
    }

    private List<CommentResponse> mapComments(List<Comment> comments, Long currentUserId) {
        if (comments.isEmpty()) {
            return List.of();
        }
        Set<Long> ids = comments.stream().map(Comment::getId).collect(Collectors.toSet());
        Map<Long, Long> replyCounts = commentRepository.countRepliesByParentIds(ids).stream()
                .collect(Collectors.toMap(
                        CommentRepository.ParentReplyCount::getParentId,
                        CommentRepository.ParentReplyCount::getCnt));
        Set<Long> liked = likedIds(currentUserId, comments);
        return comments.stream()
                .map(c -> CommentResponse.of(c,
                        liked.contains(c.getId()),
                        replyCounts.getOrDefault(c.getId(), 0L).intValue()))
                .toList();
    }

    private Set<Long> likedIds(Long currentUserId, List<Comment> comments) {
        if (currentUserId == null || comments.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Long> ids = comments.stream().map(Comment::getId).collect(Collectors.toSet());
        return new HashSet<>(commentLikeRepository.findLikedCommentIds(currentUserId, ids));
    }

    private int parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return 0;
        }
        try {
            int page = Integer.parseInt(cursor);
            return Math.max(0, page);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int clampLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
