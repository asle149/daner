package com.daner.word.controller;

import com.daner.comment.entity.Comment;
import com.daner.comment.repository.CommentRepository;
import com.daner.user.entity.User;
import com.daner.user.repository.UserRepository;
import com.daner.word.entity.Word;
import com.daner.word.repository.WordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WordControllerIntegrationTest {

    @Autowired private WebApplicationContext context;
    @Autowired private WordRepository wordRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private UserRepository userRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void existing_room_returns_word_with_exists_true() throws Exception {
        wordRepository.save(Word.builder().word("퇴근").build());

        mockMvc.perform(get("/words/{word}", "퇴근"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.exists").value(true))
                .andExpect(jsonPath("$.data.word").value("퇴근"));
    }

    @Test
    void missing_room_returns_exists_false_with_invitation_message() throws Exception {
        mockMvc.perform(get("/words/{word}", "감자전"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.exists").value(false))
                .andExpect(jsonPath("$.data.message").exists());
    }

    @Test
    void word_path_is_normalized_before_lookup() throws Exception {
        wordRepository.save(Word.builder().word("love").build());

        mockMvc.perform(get("/words/{word}", "  Love  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.exists").value(true))
                .andExpect(jsonPath("$.data.word").value("love"));
    }

    @Test
    void invalid_word_returns_400() throws Exception {
        mockMvc.perform(get("/words/{word}", "안녕 하세요"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
    }

    @Test
    void list_comments_returns_top_level_only_in_latest_order() throws Exception {
        Word word = wordRepository.save(Word.builder().word("퇴근").build());
        User author = userRepository.save(User.builder()
                .oauthProvider("google").oauthId("uid").nickname("감자전").build());
        Comment first = commentRepository.save(Comment.builder()
                .word(word).user(author).content("first").build());
        Comment second = commentRepository.save(Comment.builder()
                .word(word).user(author).content("second").build());
        commentRepository.save(Comment.builder()
                .word(word).user(author).parent(first).content("reply").build());

        mockMvc.perform(get("/words/{word}/comments", "퇴근"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.comments.length()").value(2))
                .andExpect(jsonPath("$.data.comments[0].id").value(second.getId()))
                .andExpect(jsonPath("$.data.comments[0].author.type").value("user"))
                .andExpect(jsonPath("$.data.comments[0].author.nickname").value("감자전"))
                .andExpect(jsonPath("$.data.comments[0].replyCount").value(0))
                .andExpect(jsonPath("$.data.comments[1].replyCount").value(1));
    }

    @Test
    void list_comments_for_missing_room_returns_empty_list() throws Exception {
        mockMvc.perform(get("/words/{word}/comments", "감자전"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.comments.length()").value(0));
    }
}
