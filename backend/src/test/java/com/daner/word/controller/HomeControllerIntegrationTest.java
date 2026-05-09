package com.daner.word.controller;

import com.daner.auth.service.JwtTokenProvider;
import com.daner.comment.entity.Comment;
import com.daner.comment.repository.CommentRepository;
import com.daner.user.entity.User;
import com.daner.user.repository.UserRepository;
import com.daner.word.entity.PopularWordDaily;
import com.daner.word.entity.Word;
import com.daner.word.repository.PopularWordDailyRepository;
import com.daner.word.repository.WordRepository;
import com.daner.word.service.PopularWordBatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class HomeControllerIntegrationTest {

    @Autowired private WebApplicationContext context;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private UserRepository userRepository;
    @Autowired private WordRepository wordRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private PopularWordDailyRepository popularWordDailyRepository;
    @Autowired private PopularWordBatchService popularWordBatchService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void anonymous_home_returns_empty_my_words_and_popular_list() throws Exception {
        Word w = wordRepository.save(Word.builder().word("퇴근").build());
        popularWordDailyRepository.save(new PopularWordDaily(w.getId(), 5, 1));

        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.myWords.length()").value(0))
                .andExpect(jsonPath("$.data.popularWords.length()").value(1))
                .andExpect(jsonPath("$.data.popularWords[0].word").value("퇴근"));
    }

    @Test
    void authenticated_home_returns_my_words() throws Exception {
        User user = userRepository.save(User.builder()
                .oauthProvider("google").oauthId("uid").nickname("감자전").build());
        Word w1 = wordRepository.save(Word.builder().word("퇴근").build());
        Word w2 = wordRepository.save(Word.builder().word("도파민").build());
        commentRepository.save(Comment.builder().word(w1).user(user).content("a").build());
        commentRepository.save(Comment.builder().word(w2).user(user).content("b").build());
        String access = "Bearer " + jwtTokenProvider.createAccessToken(user.getId());

        mockMvc.perform(get("/home").header("Authorization", access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.myWords.length()").value(2));
    }

    @Test
    void batch_recalculates_popular_words_when_threshold_met() {
        User user = userRepository.save(User.builder()
                .oauthProvider("google").oauthId("uid").nickname("감자전").build());
        Word top = wordRepository.save(Word.builder().word("퇴근").build());
        Word second = wordRepository.save(Word.builder().word("도파민").build());
        Word ignored = wordRepository.save(Word.builder().word("감자전").build());
        // top: 12 comments (above threshold)
        for (int i = 0; i < 12; i++) {
            commentRepository.save(Comment.builder().word(top).user(user).content("c" + i).build());
        }
        // second: 10 comments (at threshold)
        for (int i = 0; i < 10; i++) {
            commentRepository.save(Comment.builder().word(second).user(user).content("d" + i).build());
        }
        // ignored: only 3 (below threshold) — should not appear
        for (int i = 0; i < 3; i++) {
            commentRepository.save(Comment.builder().word(ignored).user(user).content("e" + i).build());
        }

        popularWordBatchService.recalculate();

        var rows = popularWordDailyRepository.findAllByOrderByRankPositionAsc();
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).getWordId()).isEqualTo(top.getId());
        assertThat(rows.get(0).getCommentCount()).isEqualTo(12);
        assertThat(rows.get(1).getWordId()).isEqualTo(second.getId());
    }

    @Test
    void batch_skips_words_below_threshold() {
        User user = userRepository.save(User.builder()
                .oauthProvider("google").oauthId("uid").nickname("감자전").build());
        Word w = wordRepository.save(Word.builder().word("퇴근").build());
        for (int i = 0; i < 5; i++) {
            commentRepository.save(Comment.builder().word(w).user(user).content("c" + i).build());
        }

        popularWordBatchService.recalculate();

        assertThat(popularWordDailyRepository.findAllByOrderByRankPositionAsc()).isEmpty();
    }
}
