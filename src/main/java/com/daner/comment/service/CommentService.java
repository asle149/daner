package com.daner.comment.service;

import com.daner.comment.dto.CommentResponse;
import com.daner.comment.dto.CommentSliceResponse;
import com.daner.comment.dto.ReplyResponse;
import com.daner.comment.dto.ReplySliceResponse;
import com.daner.comment.entity.Comment;
import com.daner.comment.repository.CommentRepository;
import com.daner.common.util.WordNormalizer;
import com.daner.like.repository.CommentLikeRepository;
import com.daner.word.entity.Word;
import com.daner.word.repository.WordRepository;
import lombok.RequiredArgsConstructor;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private static final int MAX_LIMIT = 50;
    private static final int DEFAULT_LIMIT = 20;

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final WordRepository wordRepository;

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
