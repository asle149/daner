package com.daner.like.repository;

import com.daner.comment.entity.Comment;
import com.daner.comment.repository.CommentRepository;
import com.daner.like.entity.CommentLike;
import com.daner.user.entity.User;
import com.daner.user.repository.UserRepository;
import com.daner.word.entity.Word;
import com.daner.word.repository.WordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase
class CommentLikeRepositoryTest {

    @Autowired private CommentLikeRepository commentLikeRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private WordRepository wordRepository;
    @Autowired private UserRepository userRepository;

    private User user;
    private Comment comment;

    @BeforeEach
    void setUp() {
        Word room = wordRepository.save(Word.builder().word("퇴근").build());
        user = userRepository.save(User.builder()
                .oauthProvider("google").oauthId("uid-1").nickname("감자전").build());
        comment = commentRepository.save(Comment.builder()
                .word(room).user(user).content("오늘은 칼퇴 성공").build());
    }

    @Test
    void exists_and_find_by_user_and_comment() {
        commentLikeRepository.save(CommentLike.builder().user(user).comment(comment).build());

        assertThat(commentLikeRepository.existsByUserIdAndCommentId(user.getId(), comment.getId())).isTrue();
        assertThat(commentLikeRepository.findByUserIdAndCommentId(user.getId(), comment.getId())).isPresent();
    }

    @Test
    void duplicate_like_violates_unique_constraint() {
        commentLikeRepository.save(CommentLike.builder().user(user).comment(comment).build());

        assertThatThrownBy(() -> commentLikeRepository.saveAndFlush(
                CommentLike.builder().user(user).comment(comment).build()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
