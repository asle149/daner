-- ============================================================
-- V5: 댓글 삭제 시 연관 row 자동 정리
-- comment_likes.comment_id, notifications.comment_id FK를
-- ON DELETE CASCADE 로 재정의
-- ============================================================

ALTER TABLE comment_likes
    DROP CONSTRAINT fk_comment_likes_comment;

ALTER TABLE comment_likes
    ADD CONSTRAINT fk_comment_likes_comment
    FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE;

ALTER TABLE notifications
    DROP CONSTRAINT fk_notifications_comment;

ALTER TABLE notifications
    ADD CONSTRAINT fk_notifications_comment
    FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE;
