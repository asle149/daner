package com.daner.admin.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AdminStatsResponse(
        Totals totals,
        Today today,
        List<NewWord> newWordsToday,
        List<TopWord> topActiveWordsToday,
        List<RecentUser> recentUsers,
        List<RecentAudit> recentAudits
) {

    public record Totals(long users, long words, long comments, long anonymousComments) {
    }

    public record Today(long newWords, long newComments, long newUsers) {
    }

    public record NewWord(Long id, String word, int commentCount, LocalDateTime createdAt) {
    }

    public record TopWord(Long id, String word, long commentCount) {
    }

    public record RecentUser(Long id, String nickname, LocalDateTime createdAt, boolean isAdmin) {
    }

    public record RecentAudit(
            Long id,
            Long adminId,
            String action,
            String targetType,
            String targetId,
            String detail,
            LocalDateTime createdAt
    ) {
    }
}
