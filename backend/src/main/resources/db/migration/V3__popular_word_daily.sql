-- ============================================================
-- V3: popular_word_daily 테이블
-- 매시간 배치로 24시간 내 댓글 수 TOP 3 집계
-- ============================================================

CREATE TABLE popular_word_daily (
    word_id        BIGINT    PRIMARY KEY,
    comment_count  INT       NOT NULL,
    rank_position  INT       NOT NULL,
    calculated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_popular_word_daily_word FOREIGN KEY (word_id) REFERENCES words(id) ON DELETE CASCADE
);

CREATE INDEX idx_popular_word_daily_rank ON popular_word_daily (rank_position);
