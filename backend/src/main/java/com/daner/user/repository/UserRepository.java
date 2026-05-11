package com.daner.user.repository;

import com.daner.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByOauthProviderAndOauthId(String oauthProvider, String oauthId);

    Optional<User> findByNickname(String nickname);

    boolean existsByNickname(String nickname);

    /** 관리자 후보 이메일 목록으로 사용자 일괄 조회 (대소문자 무시). */
    List<User> findByEmailInIgnoreCase(List<String> emails);

    long countByCreatedAtGreaterThanEqual(LocalDateTime since);

    List<User> findByOrderByCreatedAtDesc(Pageable pageable);
}
