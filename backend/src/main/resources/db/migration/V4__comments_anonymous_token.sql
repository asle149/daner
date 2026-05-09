-- ============================================================
-- V4: comments.anonymous_token
-- 비회원 댓글 작성 시 X-Anonymous-Token UUID 저장 → 같은 토큰
-- 보유자가 자기 글을 삭제할 수 있도록.
-- ============================================================

ALTER TABLE comments ADD COLUMN anonymous_token UUID;
CREATE INDEX idx_comments_anonymous_token ON comments (anonymous_token);
