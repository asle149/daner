-- ============================================================
-- V7: words.comment_count 를 실제 comments 개수와 동기화
-- 옛 코드(v0.2.20 이전)에서 부모 댓글 CASCADE 삭제 시 답글 수만큼 감소 안 됐던
-- 누적 mismatch 해결. v0.2.20 부터는 코드 레벨에서 정확히 카운트되므로
-- 한 번만 돌리면 됨.
-- ============================================================

UPDATE words w
SET comment_count = (
    SELECT COUNT(*) FROM comments c WHERE c.word_id = w.id
);
