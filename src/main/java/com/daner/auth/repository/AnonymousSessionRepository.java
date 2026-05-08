package com.daner.auth.repository;

import com.daner.auth.entity.AnonymousSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AnonymousSessionRepository extends JpaRepository<AnonymousSession, Long> {

    Optional<AnonymousSession> findByTokenAndWordId(UUID token, Long wordId);

    long countByWordId(Long wordId);
}
