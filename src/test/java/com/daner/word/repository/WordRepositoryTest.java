package com.daner.word.repository;

import com.daner.word.entity.Word;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase
class WordRepositoryTest {

    @Autowired
    private WordRepository wordRepository;

    @Test
    void findByWord_returns_word_room() {
        wordRepository.save(Word.builder().word("퇴근").build());

        assertThat(wordRepository.findByWord("퇴근")).isPresent();
        assertThat(wordRepository.findByWord("출근")).isEmpty();
    }

    @Test
    void increase_comment_count_persists() {
        Word saved = wordRepository.save(Word.builder().word("도파민").build());

        saved.increaseCommentCount();
        saved.increaseCommentCount();
        wordRepository.flush();

        Word reloaded = wordRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getCommentCount()).isEqualTo(2);
    }

    @Test
    void duplicate_word_violates_unique_constraint() {
        wordRepository.save(Word.builder().word("감자전").build());

        assertThatThrownBy(() -> wordRepository.saveAndFlush(Word.builder().word("감자전").build()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
