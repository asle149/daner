package com.daner.comment.repository;

import com.daner.comment.entity.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Slice<Comment> findByWordIdAndParentIsNullOrderByCreatedAtDesc(Long wordId, Pageable pageable);

    Slice<Comment> findByWordIdAndParentIsNullOrderByLikeCountDescCreatedAtDesc(Long wordId, Pageable pageable);

    Slice<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId, Pageable pageable);

    long countByWordIdAndParentIsNull(Long wordId);
}
