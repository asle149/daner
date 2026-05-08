package com.daner.like.repository;

import com.daner.like.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    Optional<CommentLike> findByUserIdAndCommentId(Long userId, Long commentId);

    boolean existsByUserIdAndCommentId(Long userId, Long commentId);

    void deleteByUserIdAndCommentId(Long userId, Long commentId);

    @Query("SELECT cl.comment.id FROM CommentLike cl " +
            "WHERE cl.user.id = :userId AND cl.comment.id IN :commentIds")
    List<Long> findLikedCommentIds(@Param("userId") Long userId,
                                   @Param("commentIds") Collection<Long> commentIds);
}
