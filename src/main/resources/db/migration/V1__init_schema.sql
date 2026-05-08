-- ============================================================
-- V1: 초기 스키마
-- USER, WORD, COMMENT, COMMENT_LIKE, NOTIFICATION,
-- ANONYMOUS_SESSION 테이블 생성 + 인덱스
-- ============================================================

CREATE TABLE users (
    id                BIGSERIAL PRIMARY KEY,
    oauth_provider    VARCHAR(20)  NOT NULL,
    oauth_id          VARCHAR(100) NOT NULL,
    nickname          VARCHAR(20)  NOT NULL,
    profile_image_url VARCHAR(500),
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_users_nickname UNIQUE (nickname),
    CONSTRAINT uk_users_oauth_provider_oauth_id UNIQUE (oauth_provider, oauth_id)
);

CREATE TABLE words (
    id            BIGSERIAL PRIMARY KEY,
    word          VARCHAR(40) NOT NULL,
    comment_count INT         NOT NULL DEFAULT 0,
    like_count    INT         NOT NULL DEFAULT 0,
    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_words_word UNIQUE (word)
);

CREATE TABLE comments (
    id              BIGSERIAL PRIMARY KEY,
    word_id         BIGINT      NOT NULL,
    user_id         BIGINT,
    parent_id       BIGINT,
    anonymous_label VARCHAR(10),
    content         TEXT        NOT NULL,
    like_count      INT         NOT NULL DEFAULT 0,
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comments_word   FOREIGN KEY (word_id)   REFERENCES words(id),
    CONSTRAINT fk_comments_user   FOREIGN KEY (user_id)   REFERENCES users(id),
    CONSTRAINT fk_comments_parent FOREIGN KEY (parent_id) REFERENCES comments(id)
);

CREATE INDEX idx_comments_word_id_created_at_desc
    ON comments (word_id, created_at DESC);

CREATE INDEX idx_comments_word_id_like_count_desc
    ON comments (word_id, like_count DESC, created_at DESC);

CREATE INDEX idx_comments_user_id_created_at_desc
    ON comments (user_id, created_at DESC);

CREATE INDEX idx_comments_parent_id
    ON comments (parent_id);

CREATE TABLE comment_likes (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT    NOT NULL,
    comment_id BIGINT    NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comment_likes_user    FOREIGN KEY (user_id)    REFERENCES users(id),
    CONSTRAINT fk_comment_likes_comment FOREIGN KEY (comment_id) REFERENCES comments(id),
    CONSTRAINT uk_comment_likes_user_id_comment_id UNIQUE (user_id, comment_id)
);

CREATE TABLE notifications (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT      NOT NULL,
    type          VARCHAR(20) NOT NULL,
    word_id       BIGINT      NOT NULL,
    comment_id    BIGINT      NOT NULL,
    actor_user_id BIGINT,
    actor_label   VARCHAR(10),
    preview       VARCHAR(100),
    is_read       BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user       FOREIGN KEY (user_id)       REFERENCES users(id),
    CONSTRAINT fk_notifications_word       FOREIGN KEY (word_id)       REFERENCES words(id),
    CONSTRAINT fk_notifications_comment    FOREIGN KEY (comment_id)    REFERENCES comments(id),
    CONSTRAINT fk_notifications_actor_user FOREIGN KEY (actor_user_id) REFERENCES users(id)
);

CREATE INDEX idx_notifications_user_id_created_at_desc
    ON notifications (user_id, created_at DESC);

CREATE INDEX idx_notifications_user_id_is_read
    ON notifications (user_id, is_read);

CREATE TABLE anonymous_sessions (
    id         BIGSERIAL PRIMARY KEY,
    token      UUID        NOT NULL,
    word_id    BIGINT      NOT NULL,
    label      VARCHAR(10) NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_anonymous_sessions_word FOREIGN KEY (word_id) REFERENCES words(id),
    CONSTRAINT uk_anonymous_sessions_token_word_id UNIQUE (token, word_id)
);
