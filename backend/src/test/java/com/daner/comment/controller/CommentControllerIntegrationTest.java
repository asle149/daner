package com.daner.comment.controller;

import com.daner.auth.service.JwtTokenProvider;
import com.daner.comment.entity.Comment;
import com.daner.comment.repository.CommentRepository;
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

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CommentControllerIntegrationTest {

    @Autowired private WebApplicationContext context;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private UserRepository userRepository;
    @Autowired private WordRepository wordRepository;
    @Autowired private CommentRepository commentRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void member_creates_top_level_comment_and_room_is_auto_created() throws Exception {
        User user = saveUser("uid-1", "감자전");
        String access = "Bearer " + jwtTokenProvider.createAccessToken(user.getId());
        String body = objectMapper.writeValueAsString(Map.of("content", "오늘은 칼퇴 성공"));

        mockMvc.perform(post("/words/{word}/comments", "퇴근")
                        .header("Authorization", access)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.author.type").value("user"))
                .andExpect(jsonPath("$.data.author.nickname").value("감자전"));

        assertThat(wordRepository.findByWord("퇴근")).isPresent();
    }

    @Test
    void guest_creates_comment_with_anonymous_label() throws Exception {
        UUID token = UUID.randomUUID();
        String body = objectMapper.writeValueAsString(Map.of("content", "ㅠㅠ 야근중"));

        mockMvc.perform(post("/words/{word}/comments", "퇴근")
                        .header("X-Anonymous-Token", token.toString())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.author.type").value("anonymous"))
                .andExpect(jsonPath("$.data.author.label").value("익명"));

        // same token in same room reuses the same label
        mockMvc.perform(post("/words/{word}/comments", "퇴근")
                        .header("X-Anonymous-Token", token.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", "퇴근만이 살길"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.author.label").value("익명"));
    }

    @Test
    void no_auth_returns_401() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("content", "hi"));

        mockMvc.perform(post("/words/{word}/comments", "퇴근")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_reply_under_top_level_works() throws Exception {
        User user = saveUser("uid-1", "감자전");
        Word word = wordRepository.save(Word.builder().word("퇴근").build());
        Comment parent = commentRepository.save(Comment.builder().word(word).user(user).content("p").build());
        String access = "Bearer " + jwtTokenProvider.createAccessToken(user.getId());

        mockMvc.perform(post("/comments/{id}/replies", parent.getId())
                        .header("Authorization", access)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", "축하해요"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.parentId").value(parent.getId()));
    }

    @Test
    void reply_under_reply_is_rejected_with_400() throws Exception {
        User user = saveUser("uid-1", "감자전");
        Word word = wordRepository.save(Word.builder().word("퇴근").build());
        Comment parent = commentRepository.save(Comment.builder().word(word).user(user).content("p").build());
        Comment reply = commentRepository.save(Comment.builder().word(word).user(user).parent(parent).content("r").build());
        String access = "Bearer " + jwtTokenProvider.createAccessToken(user.getId());

        mockMvc.perform(post("/comments/{id}/replies", reply.getId())
                        .header("Authorization", access)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", "no"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("REPLY_DEPTH_EXCEEDED"));
    }

    @Test
    void delete_own_comment_works() throws Exception {
        User user = saveUser("uid-1", "감자전");
        Word word = wordRepository.save(Word.builder().word("퇴근").build());
        Comment comment = commentRepository.save(Comment.builder().word(word).user(user).content("mine").build());
        String access = "Bearer " + jwtTokenProvider.createAccessToken(user.getId());

        mockMvc.perform(delete("/comments/{id}", comment.getId())
                        .header("Authorization", access))
                .andExpect(status().isOk());

        assertThat(commentRepository.findById(comment.getId())).isEmpty();
    }

    @Test
    void delete_others_comment_returns_403() throws Exception {
        User owner = saveUser("uid-1", "감자전");
        User other = saveUser("uid-2", "도파민");
        Word word = wordRepository.save(Word.builder().word("퇴근").build());
        Comment comment = commentRepository.save(Comment.builder().word(word).user(owner).content("yours").build());
        String access = "Bearer " + jwtTokenProvider.createAccessToken(other.getId());

        mockMvc.perform(delete("/comments/{id}", comment.getId())
                        .header("Authorization", access))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_comment_with_replies_returns_409() throws Exception {
        User user = saveUser("uid-1", "감자전");
        Word word = wordRepository.save(Word.builder().word("퇴근").build());
        Comment parent = commentRepository.save(Comment.builder().word(word).user(user).content("parent").build());
        commentRepository.save(Comment.builder().word(word).user(user).parent(parent).content("reply").build());
        String access = "Bearer " + jwtTokenProvider.createAccessToken(user.getId());

        mockMvc.perform(delete("/comments/{id}", parent.getId())
                        .header("Authorization", access))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("COMMENT_HAS_REPLIES"));
    }

    private User saveUser(String oauthId, String nickname) {
        return userRepository.save(User.builder()
                .oauthProvider("google").oauthId(oauthId).nickname(nickname).build());
    }
}
