package com.daner.user.service;

import com.daner.comment.repository.CommentRepository;
import com.daner.common.exception.BusinessException;
import com.daner.common.exception.ErrorCode;
import com.daner.user.dto.MyProfileResponse;
import com.daner.user.entity.User;
import com.daner.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final int MAX_LIMIT = 50;
    private static final int DEFAULT_LIMIT = 20;

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfile(Long userId, String cursor, Integer limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        int page = parseCursor(cursor);
        int size = clampLimit(limit);
        List<MyProfileResponse.BookshelfWord> words = commentRepository
                .findMyBookshelf(userId, PageRequest.of(page, size + 1)).stream()
                .map(p -> new MyProfileResponse.BookshelfWord(
                        p.getId(), p.getWord(),
                        p.getMyCommentCount().intValue(),
                        p.getLastActivityAt()))
                .toList();
        boolean hasNext = words.size() > size;
        List<MyProfileResponse.BookshelfWord> page1 = hasNext ? words.subList(0, size) : words;
        return new MyProfileResponse(MyProfileResponse.UserSummary.from(user), page1,
                hasNext ? String.valueOf(page + 1) : null);
    }

    private int parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) return 0;
        try {
            return Math.max(0, Integer.parseInt(cursor));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int clampLimit(Integer limit) {
        if (limit == null || limit <= 0) return DEFAULT_LIMIT;
        return Math.min(limit, MAX_LIMIT);
    }
}
