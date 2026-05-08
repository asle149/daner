package com.daner.like.controller;

import com.daner.auth.service.JwtTokenProvider;
import com.daner.comment.entity.Comment;
import com.daner.comment.repository.CommentRepository;
import com.daner.like.entity.CommentLike;
import com.daner.like.repository.CommentLikeRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LikeControllerIntegrationTest {

    @Autowired private WebApplicationContext context;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private UserRepository userRepository;
    @Autowired private WordRepository wordRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private CommentLikeRepository commentLikeRepository;

    private MockMvc mockMvc;
    private User user;
    private Comment comment;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        user = userRepository.save(User.builder()
                .oauthProvider("google").oauthId("uid-1").nickname("감자전").build());
        Word word = wordRepository.save(Word.builder().word("퇴근").build());
        comment = commentRepository.save(Comment.builder().word(word).user(user).content("hi").build());
    }

    @Test
    void member_likes_comment_increments_count() throws Exception {
        String access = "Bearer " + jwtTokenProvider.createAccessToken(user.getId());

        mockMvc.perform(post("/comments/{id}/like", comment.getId()).header("Authorization", access))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.likeCount").value(1))
                .andExpect(jsonPath("$.data.isLiked").value(true));

        assertThat(commentLikeRepository.existsByUserIdAndCommentId(user.getId(), comment.getId())).isTrue();
    }

    @Test
    void duplicate_like_returns_409() throws Exception {
        String access = "Bearer " + jwtTokenProvider.createAccessToken(user.getId());
        commentLikeRepository.save(CommentLike.builder().user(user).comment(comment).build());

        mockMvc.perform(post("/comments/{id}/like", comment.getId()).header("Authorization", access))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("ALREADY_LIKED"));
    }

    @Test
    void unlike_removes_like_and_decrements_count() throws Exception {
        String access = "Bearer " + jwtTokenProvider.createAccessToken(user.getId());
        commentLikeRepository.save(CommentLike.builder().user(user).comment(comment).build());
        comment.increaseLikeCount();
        commentRepository.flush();

        mockMvc.perform(delete("/comments/{id}/like", comment.getId()).header("Authorization", access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isLiked").value(false))
                .andExpect(jsonPath("$.data.likeCount").value(0));

        assertThat(commentLikeRepository.existsByUserIdAndCommentId(user.getId(), comment.getId())).isFalse();
    }

    @Test
    void unlike_without_existing_like_returns_404() throws Exception {
        String access = "Bearer " + jwtTokenProvider.createAccessToken(user.getId());

        mockMvc.perform(delete("/comments/{id}/like", comment.getId()).header("Authorization", access))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("LIKE_NOT_FOUND"));
    }

    @Test
    void like_without_auth_returns_401() throws Exception {
        mockMvc.perform(post("/comments/{id}/like", comment.getId()))
                .andExpect(status().isUnauthorized());
    }
}
