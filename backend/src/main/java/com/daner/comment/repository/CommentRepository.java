package com.daner.comment.repository;

import com.daner.comment.entity.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c WHERE c.word.id = :wordId AND c.parent IS NULL " +
            "ORDER BY c.createdAt DESC, c.id DESC")
    Slice<Comment> findByWordIdAndParentIsNullOrderByCreatedAtDesc(@Param("wordId") Long wordId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.word.id = :wordId AND c.parent IS NULL " +
            "ORDER BY c.likeCount DESC, c.createdAt DESC, c.id DESC")
    Slice<Comment> findByWordIdAndParentIsNullOrderByLikeCountDescCreatedAtDesc(
            @Param("wordId") Long wordId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.parent.id = :parentId " +
            "ORDER BY c.createdAt ASC, c.id ASC")
    Slice<Comment> findByParentIdOrderByCreatedAtAsc(@Param("parentId") Long parentId, Pageable pageable);

    long countByWordIdAndParentIsNull(Long wordId);

    long countByWordId(Long wordId);

    long countByCreatedAtGreaterThanEqual(java.time.LocalDateTime since);

    long countByUserIsNull();

    /** 단어 방의 모든 댓글/답글 일괄 삭제 (관리자 비우기용) */
    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM Comment c WHERE c.word.id = :wordId")
    int deleteAllByWordId(@Param("wordId") Long wordId);

    @Query("SELECT c.parent.id AS parentId, COUNT(c) AS cnt FROM Comment c " +
            "WHERE c.parent.id IN :parentIds GROUP BY c.parent.id")
    List<ParentReplyCount> countRepliesByParentIds(@Param("parentIds") Collection<Long> parentIds);

    @Query("SELECT c.word.id AS wordId, COUNT(c) AS cnt FROM Comment c " +
            "WHERE c.createdAt >= :since GROUP BY c.word.id ORDER BY COUNT(c) DESC, c.word.id ASC")
    List<WordCommentCount> findTopWordsByCommentCountSince(@Param("since") java.time.LocalDateTime since,
                                                            org.springframework.data.domain.Pageable pageable);

    @Query("SELECT w.id AS wordId, w.word AS word, COUNT(c) AS cnt FROM Comment c JOIN c.word w " +
            "WHERE c.createdAt >= :since GROUP BY w.id, w.word ORDER BY COUNT(c) DESC, w.id ASC")
    List<TopActiveWord> findTopActiveWordsSince(@Param("since") java.time.LocalDateTime since,
                                                 org.springframework.data.domain.Pageable pageable);

    interface TopActiveWord {
        Long getWordId();
        String getWord();
        Long getCnt();
    }

    interface ParentReplyCount {
        Long getParentId();

        Long getCnt();
    }

    interface WordCommentCount {
        Long getWordId();

        Long getCnt();
    }

    @Query("SELECT w.id AS id, w.word AS word, w.commentCount AS commentCount, " +
            "(SELECT MAX(c2.createdAt) FROM Comment c2 WHERE c2.word.id = w.id) AS lastActivityAt, " +
            "MAX(c.createdAt) AS myLastCommentAt " +
            "FROM Comment c JOIN c.word w " +
            "WHERE c.user.id = :userId " +
            "GROUP BY w.id, w.word, w.commentCount " +
            "ORDER BY MAX(c.createdAt) DESC")
    List<MyWordProjection> findMyWords(@Param("userId") Long userId);

    interface MyWordProjection {
        Long getId();

        String getWord();

        int getCommentCount();

        java.time.LocalDateTime getLastActivityAt();

        java.time.LocalDateTime getMyLastCommentAt();
    }

    @Query("SELECT w.id AS id, w.word AS word, COUNT(c) AS myCommentCount, " +
            "MAX(c.createdAt) AS lastActivityAt " +
            "FROM Comment c JOIN c.word w " +
            "WHERE c.user.id = :userId " +
            "GROUP BY w.id, w.word " +
            "ORDER BY MAX(c.createdAt) DESC")
    List<MyBookshelfProjection> findMyBookshelf(@Param("userId") Long userId,
                                                org.springframework.data.domain.Pageable pageable);

    interface MyBookshelfProjection {
        Long getId();

        String getWord();

        Long getMyCommentCount();

        java.time.LocalDateTime getLastActivityAt();
    }
}
