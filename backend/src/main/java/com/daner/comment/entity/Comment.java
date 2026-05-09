package com.daner.comment.entity;

import com.daner.user.entity.User;
import com.daner.word.entity.Word;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @Column(name = "anonymous_label", length = 10)
    private String anonymousLabel;

    @Column(name = "anonymous_token")
    private UUID anonymousToken;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Comment(Word word, User user, Comment parent, String anonymousLabel,
                    UUID anonymousToken, String content) {
        this.word = word;
        this.user = user;
        this.parent = parent;
        this.anonymousLabel = anonymousLabel;
        this.anonymousToken = anonymousToken;
        this.content = content;
    }

    public boolean wasWrittenByAnonymousToken(UUID candidate) {
        return this.user == null && candidate != null && candidate.equals(this.anonymousToken);
    }

    public boolean isReply() {
        return this.parent != null;
    }

    public boolean isAnonymous() {
        return this.user == null || this.anonymousLabel != null;
    }

    public boolean isWrittenBy(User candidate) {
        return this.user != null && candidate != null && this.user.getId().equals(candidate.getId());
    }

    public void increaseLikeCount() {
        this.likeCount += 1;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount -= 1;
        }
    }
}
