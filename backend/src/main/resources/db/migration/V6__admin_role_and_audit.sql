-- ============================================================
-- V6: 관리자 권한 + 감사 로그
-- ============================================================

-- users 테이블에 email, role 추가
-- email 은 nullable: 기존 가입자는 NULL 로 두고 다음 로그인 시 채움
ALTER TABLE users
    ADD COLUMN email VARCHAR(255),
    ADD COLUMN role  VARCHAR(20) NOT NULL DEFAULT 'USER';

CREATE UNIQUE INDEX uk_users_email ON users (email) WHERE email IS NOT NULL;
CREATE INDEX idx_users_role ON users (role) WHERE role <> 'USER';

-- 관리자 감사 로그 — 누가 언제 무엇을 했는지 기록
CREATE TABLE admin_audit_log (
    id           BIGSERIAL    PRIMARY KEY,
    admin_id     BIGINT       NOT NULL,
    action       VARCHAR(40)  NOT NULL,
    target_type  VARCHAR(40),
    target_id    VARCHAR(100),
    detail       VARCHAR(500),
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_admin FOREIGN KEY (admin_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX idx_admin_audit_admin_id ON admin_audit_log (admin_id, created_at DESC);
CREATE INDEX idx_admin_audit_action ON admin_audit_log (action, created_at DESC);
