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

    Slice<Comment> findByWordIdAndParentIsNullOrderByCreatedAtDesc(Long wordId, Pageable pageable);

    Slice<Comment> findByWordIdAndParentIsNullOrderByLikeCountDescCreatedAtDesc(Long wordId, Pageable pageable);

    Slice<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId, Pageable pageable);

    long countByWordIdAndParentIsNull(Long wordId);

    @Query("SELECT c.parent.id AS parentId, COUNT(c) AS cnt FROM Comment c " +
            "WHERE c.parent.id IN :parentIds GROUP BY c.parent.id")
    List<ParentReplyCount> countRepliesByParentIds(@Param("parentIds") Collection<Long> parentIds);

    interface ParentReplyCount {
        Long getParentId();

        Long getCnt();
    }
}
