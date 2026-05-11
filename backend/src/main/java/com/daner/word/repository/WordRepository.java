package com.daner.word.repository;

import com.daner.word.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WordRepository extends JpaRepository<Word, Long> {

    Optional<Word> findByWord(String word);

    boolean existsByWord(String word);

    long countByCreatedAtGreaterThanEqual(LocalDateTime since);

    List<Word> findByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(LocalDateTime since);
}
