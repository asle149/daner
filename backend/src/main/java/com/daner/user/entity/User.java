package com.daner.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_nickname", columnNames = "nickname"),
                @UniqueConstraint(name = "uk_users_oauth_provider_oauth_id", columnNames = {"oauth_provider", "oauth_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "oauth_provider", nullable = false, length = 20)
    private String oauthProvider;

    @Column(name = "oauth_id", nullable = false, length = 100)
    private String oauthId;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    // OAuth 로그인 시 받은 이메일. 기존 가입자는 NULL 일 수 있고, 재로그인 시 채워짐.
    @Column(length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private User(String oauthProvider, String oauthId, String nickname, String profileImageUrl, String email) {
        this.oauthProvider = oauthProvider;
        this.oauthId = oauthId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.email = email;
        this.role = Role.USER;
    }

    public boolean isAdmin() {
        return role != null && role.isAdmin();
    }

    public void promoteToAdmin() {
        this.role = Role.ADMIN;
    }

    public void demoteToUser() {
        this.role = Role.USER;
    }

    /** OAuth 로그인 시 이메일이 비어 있으면 채움. 이미 다른 값이 있으면 갱신. */
    public void updateEmailIfChanged(String email) {
        if (email == null || email.isBlank()) return;
        if (email.equals(this.email)) return;
        this.email = email;
    }

    /** 관리자 후보면 즉시 승격, 아니면 그대로. (로그인 시 동기화용) */
    public void syncRole(boolean shouldBeAdmin) {
        if (shouldBeAdmin && !isAdmin()) {
            this.role = Role.ADMIN;
        }
        // 의도적으로 자동 강등은 하지 않음 — 운영 사고 방지. 강등은 명시적으로만.
    }
}
