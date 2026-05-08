package com.daner.user.controller;

import com.daner.auth.service.JwtTokenProvider;
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
class UserControllerIntegrationTest {

    @Autowired private WebApplicationContext context;
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
    void me_returns_user_and_bookshelf_grouped_by_word() throws Exception {
        User user = userRepository.save(User.builder()
                .oauthProvider("google").oauthId("uid").nickname("감자전").build());
        Word w1 = wordRepository.save(Word.builder().word("퇴근").build());
        Word w2 = wordRepository.save(Word.builder().word("도파민").build());
        commentRepository.save(Comment.builder().word(w1).user(user).content("a").build());
        commentRepository.save(Comment.builder().word(w1).user(user).content("b").build());
        commentRepository.save(Comment.builder().word(w2).user(user).content("c").build());

        String access = "Bearer " + jwtTokenProvider.createAccessToken(user.getId());

        mockMvc.perform(get("/users/me").header("Authorization", access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.nickname").value("감자전"))
                .andExpect(jsonPath("$.data.myWords.length()").value(2));
    }

    @Test
    void me_without_auth_returns_401() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());
    }
}
