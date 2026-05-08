package com.daner.word.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "popular_word_daily")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularWordDaily {

    @Id
    @Column(name = "word_id")
    private Long wordId;

    @Column(name = "comment_count", nullable = false)
    private int commentCount;

    @Column(name = "rank_position", nullable = false)
    private int rankPosition;

    @CreationTimestamp
    @Column(name = "calculated_at", nullable = false, updatable = false)
    private LocalDateTime calculatedAt;

    public PopularWordDaily(Long wordId, int commentCount, int rankPosition) {
        this.wordId = wordId;
        this.commentCount = commentCount;
        this.rankPosition = rankPosition;
    }
}
