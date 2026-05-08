package com.daner.comment.repository;

import com.daner.comment.entity.Comment;
import com.daner.user.entity.User;
import com.daner.user.repository.UserRepository;
import com.daner.word.entity.Word;
import com.daner.word.repository.WordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase
class CommentRepositoryTest {

    @Autowired private CommentRepository commentRepository;
    @Autowired private WordRepository wordRepository;
    @Autowired private UserRepository userRepository;

    private Word room;
    private User author;

    @BeforeEach
    void setUp() {
        room = wordRepository.save(Word.builder().word("퇴근").build());
        author = userRepository.save(User.builder()
                .oauthProvider("google").oauthId("uid-1").nickname("감자전").build());
    }

    @Test
    void top_level_latest_orders_newest_first_and_excludes_replies() {
        Comment first = commentRepository.save(top("첫 댓글"));
        sleep();
        Comment second = commentRepository.save(top("두번째 댓글"));
        commentRepository.save(reply(first, "답글"));

        Slice<Comment> page = commentRepository
                .findByWordIdAndParentIsNullOrderByCreatedAtDesc(room.getId(), PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent().get(0).getId()).isEqualTo(second.getId());
        assertThat(page.getContent().get(1).getId()).isEqualTo(first.getId());
    }

    @Test
    void top_level_popular_orders_by_like_count_desc() {
        Comment lessLiked = commentRepository.save(top("less"));
        Comment moreLiked = commentRepository.save(top("more"));
        moreLiked.increaseLikeCount();
        moreLiked.increaseLikeCount();
        lessLiked.increaseLikeCount();
        commentRepository.flush();

        List<Comment> page = commentRepository
                .findByWordIdAndParentIsNullOrderByLikeCountDescCreatedAtDesc(room.getId(), PageRequest.of(0, 10))
                .getContent();

        assertThat(page).extracting(Comment::getId)
                .containsExactly(moreLiked.getId(), lessLiked.getId());
    }

    @Test
    void replies_are_returned_in_creation_order() {
        Comment parent = commentRepository.save(top("parent"));
        Comment r1 = commentRepository.save(reply(parent, "r1"));
        sleep();
        Comment r2 = commentRepository.save(reply(parent, "r2"));

        Slice<Comment> replies = commentRepository
                .findByParentIdOrderByCreatedAtAsc(parent.getId(), PageRequest.of(0, 10));

        assertThat(replies.getContent()).extracting(Comment::getId).containsExactly(r1.getId(), r2.getId());
    }

    @Test
    void count_top_level_excludes_replies() {
        Comment parent = commentRepository.save(top("parent"));
        commentRepository.save(top("sibling"));
        commentRepository.save(reply(parent, "r1"));

        long count = commentRepository.countByWordIdAndParentIsNull(room.getId());

        assertThat(count).isEqualTo(2);
    }

    private Comment top(String content) {
        return Comment.builder().word(room).user(author).content(content).build();
    }

    private Comment reply(Comment parent, String content) {
        return Comment.builder().word(room).user(author).parent(parent).content(content).build();
    }

    private static void sleep() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
