package com.daner.notification.service;

import com.daner.notification.dto.NotificationResponse;
import com.daner.notification.dto.NotificationSliceResponse;
import com.daner.notification.entity.Notification;
import com.daner.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int MAX_LIMIT = 50;
    private static final int DEFAULT_LIMIT = 20;

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public NotificationSliceResponse list(Long userId, String cursor, Integer limit) {
        int page = parseCursor(cursor);
        int size = clampLimit(limit);
        Slice<Notification> slice = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
        List<NotificationResponse> items = slice.getContent().stream()
                .map(NotificationResponse::from)
                .toList();
        return new NotificationSliceResponse(items, slice.hasNext() ? String.valueOf(page + 1) : null);
    }

    @Transactional
    public void markRead(Long userId, Collection<Long> ids) {
        List<Notification> mine = notificationRepository.findAllByIdInAndUserId(ids, userId);
        mine.forEach(Notification::markRead);
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
