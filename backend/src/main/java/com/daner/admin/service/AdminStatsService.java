package com.daner.admin.service;

import com.daner.admin.dto.AdminStatsResponse;
import com.daner.admin.dto.AdminStatsResponse.NewWord;
import com.daner.admin.dto.AdminStatsResponse.RecentAudit;
import com.daner.admin.dto.AdminStatsResponse.RecentUser;
import com.daner.admin.dto.AdminStatsResponse.Today;
import com.daner.admin.dto.AdminStatsResponse.TopWord;
import com.daner.admin.dto.AdminStatsResponse.Totals;
import com.daner.admin.repository.AdminAuditLogRepository;
import com.daner.comment.repository.CommentRepository;
import com.daner.user.repository.UserRepository;
import com.daner.word.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    // 통계는 운영자의 한국 시각 기준 "오늘" 로 잡음
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private static final int NEW_WORDS_LIMIT = 10;
    private static final int TOP_ACTIVE_LIMIT = 5;
    private static final int RECENT_USERS_LIMIT = 5;
    private static final int RECENT_AUDIT_LIMIT = 10;

    private final UserRepository userRepository;
    private final WordRepository wordRepository;
    private final CommentRepository commentRepository;
    private final AdminAuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public AdminStatsResponse load() {
        LocalDateTime todayStart = LocalDate.now(KST).atStartOfDay();

        Totals totals = new Totals(
                userRepository.count(),
                wordRepository.count(),
                commentRepository.count(),
                commentRepository.countByUserIsNull());

        Today today = new Today(
                wordRepository.countByCreatedAtGreaterThanEqual(todayStart),
                commentRepository.countByCreatedAtGreaterThanEqual(todayStart),
                userRepository.countByCreatedAtGreaterThanEqual(todayStart));

        List<NewWord> newWordsToday = wordRepository
                .findByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(todayStart)
                .stream()
                .limit(NEW_WORDS_LIMIT)
                .map(w -> new NewWord(w.getId(), w.getWord(), w.getCommentCount(), w.getCreatedAt()))
                .toList();

        List<TopWord> topActiveWordsToday = commentRepository
                .findTopActiveWordsSince(todayStart, PageRequest.of(0, TOP_ACTIVE_LIMIT))
                .stream()
                .map(p -> new TopWord(p.getWordId(), p.getWord(), p.getCnt()))
                .toList();

        List<RecentUser> recentUsers = userRepository
                .findByOrderByCreatedAtDesc(PageRequest.of(0, RECENT_USERS_LIMIT))
                .stream()
                .map(u -> new RecentUser(u.getId(), u.getNickname(), u.getCreatedAt(), u.isAdmin()))
                .toList();

        List<RecentAudit> recentAudits = auditLogRepository
                .findByOrderByCreatedAtDesc(PageRequest.of(0, RECENT_AUDIT_LIMIT))
                .stream()
                .map(a -> new RecentAudit(
                        a.getId(), a.getAdminId(), a.getAction(),
                        a.getTargetType(), a.getTargetId(), a.getDetail(), a.getCreatedAt()))
                .toList();

        return new AdminStatsResponse(totals, today, newWordsToday, topActiveWordsToday,
                recentUsers, recentAudits);
    }
}
