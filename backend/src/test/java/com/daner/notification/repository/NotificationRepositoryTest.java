package com.daner.notification.repository;

import com.daner.comment.entity.Comment;
import com.daner.comment.repository.CommentRepository;
import com.daner.notification.entity.Notification;
import com.daner.notification.entity.NotificationType;
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

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase
class NotificationRepositoryTest {

    @Autowired private NotificationRepository notificationRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private WordRepository wordRepository;
    @Autowired private UserRepository userRepository;

    private User recipient;
    private Word word;
    private Comment comment;

    @BeforeEach
    void setUp() {
        word = wordRepository.save(Word.builder().word("퇴근").build());
        recipient = userRepository.save(User.builder()
                .oauthProvider("google").oauthId("uid-1").nickname("감자전").build());
        comment = commentRepository.save(Comment.builder()
                .word(word).user(recipient).content("내 댓글").build());
    }

    @Test
    void list_orders_newest_first_and_unread_count_excludes_read() {
        Notification first = save("첫 알림");
        Notification second = save("두번째 알림");
        first.markRead();
        notificationRepository.flush();

        List<Notification> page = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(recipient.getId(), PageRequest.of(0, 10))
                .getContent();

        assertThat(page).extracting(Notification::getId)
                .containsExactly(second.getId(), first.getId());
        assertThat(notificationRepository.countByUserIdAndIsReadFalse(recipient.getId())).isEqualTo(1);
    }

    @Test
    void find_all_by_id_in_and_user_id_filters_by_owner() {
        Notification mine = save("내 알림");
        User other = userRepository.save(User.builder()
                .oauthProvider("google").oauthId("uid-2").nickname("도파민").build());
        Notification stranger = notificationRepository.save(Notification.builder()
                .user(other).type(NotificationType.LIKE).word(word).comment(comment)
                .actorUser(recipient).build());

        List<Notification> visible = notificationRepository.findAllByIdInAndUserId(
                Set.of(mine.getId(), stranger.getId()), recipient.getId());

        assertThat(visible).extracting(Notification::getId).containsExactly(mine.getId());
    }

    private Notification save(String preview) {
        return notificationRepository.save(Notification.builder()
                .user(recipient).type(NotificationType.REPLY).word(word).comment(comment)
                .actorLabel("익명1").preview(preview).build());
    }
}
