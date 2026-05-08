package com.daner.notification.controller;

import com.daner.auth.service.JwtTokenProvider;
import com.daner.comment.entity.Comment;
import com.daner.comment.repository.CommentRepository;
import com.daner.notification.entity.Notification;
import com.daner.notification.entity.NotificationType;
import com.daner.notification.repository.NotificationRepository;
import com.daner.user.entity.User;
import com.daner.user.repository.UserRepository;
import com.daner.word.entity.Word;
import com.daner.word.repository.WordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificationControllerIntegrationTest {

    @Autowired private WebApplicationContext context;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private UserRepository userRepository;
    @Autowired private WordRepository wordRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private NotificationRepository notificationRepository;

    private MockMvc mockMvc;
    private User recipient;
    private User actor;
    private Word word;
    private Comment parentComment;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        recipient = userRepository.save(User.builder()
                .oauthProvider("google").oauthId("uid-r").nickname("감자전").build());
        actor = userRepository.save(User.builder()
                .oauthProvider("google").oauthId("uid-a").nickname("도파민").build());
        word = wordRepository.save(Word.builder().word("퇴근").build());
        parentComment = commentRepository.save(Comment.builder()
                .word(word).user(recipient).content("내 댓글").build());
    }

    @Test
    void list_returns_my_notifications_newest_first() throws Exception {
        save(NotificationType.LIKE, "first");
        save(NotificationType.REPLY, "second");
        String access = "Bearer " + jwtTokenProvider.createAccessToken(recipient.getId());

        mockMvc.perform(get("/users/me/notifications").header("Authorization", access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notifications.length()").value(2))
                .andExpect(jsonPath("$.data.notifications[0].type").value("reply"));
    }

    @Test
    void list_without_auth_returns_401() throws Exception {
        mockMvc.perform(get("/users/me/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void mark_read_only_affects_owners_notifications() throws Exception {
        Notification mine = save(NotificationType.LIKE, "mine");
        Notification stranger = notificationRepository.save(Notification.builder()
                .user(actor).type(NotificationType.LIKE).word(word).comment(parentComment)
                .actorUser(recipient).build());

        String access = "Bearer " + jwtTokenProvider.createAccessToken(recipient.getId());
        String body = objectMapper.writeValueAsString(
                Map.of("notificationIds", List.of(mine.getId(), stranger.getId())));

        mockMvc.perform(patch("/users/me/notifications/read")
                        .header("Authorization", access)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        notificationRepository.flush();
        assertThat(notificationRepository.findById(mine.getId()).get().isRead()).isTrue();
        assertThat(notificationRepository.findById(stranger.getId()).get().isRead()).isFalse();
    }

    @Test
    void unread_count_excludes_read_notifications() {
        Notification a = save(NotificationType.LIKE, "a");
        save(NotificationType.REPLY, "b");
        a.markRead();
        notificationRepository.flush();

        long unread = notificationRepository.countByUserIdAndIsReadFalse(recipient.getId());

        assertThat(unread).isEqualTo(1);
    }

    private Notification save(NotificationType type, String preview) {
        return notificationRepository.save(Notification.builder()
                .user(recipient).type(type).word(word).comment(parentComment)
                .actorUser(actor).preview(preview).build());
    }
}
