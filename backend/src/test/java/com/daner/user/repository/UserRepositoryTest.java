package com.daner.user.repository;

import com.daner.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByOauthProviderAndOauthId_returns_user_when_present() {
        userRepository.save(User.builder()
                .oauthProvider("google")
                .oauthId("google-uid-1")
                .nickname("감자전")
                .build());

        Optional<User> found = userRepository.findByOauthProviderAndOauthId("google", "google-uid-1");

        assertThat(found).isPresent();
        assertThat(found.get().getNickname()).isEqualTo("감자전");
    }

    @Test
    void existsByNickname_reflects_uniqueness() {
        userRepository.save(User.builder()
                .oauthProvider("google")
                .oauthId("uid-2")
                .nickname("도파민")
                .build());

        assertThat(userRepository.existsByNickname("도파민")).isTrue();
        assertThat(userRepository.existsByNickname("없는이름")).isFalse();
    }

    @Test
    void duplicate_nickname_violates_unique_constraint() {
        userRepository.save(User.builder()
                .oauthProvider("google")
                .oauthId("uid-3")
                .nickname("중복닉")
                .build());

        assertThatThrownBy(() -> {
            userRepository.saveAndFlush(User.builder()
                    .oauthProvider("google")
                    .oauthId("uid-4")
                    .nickname("중복닉")
                    .build());
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
