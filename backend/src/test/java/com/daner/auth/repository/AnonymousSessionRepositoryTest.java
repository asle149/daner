package com.daner.auth.repository;

import com.daner.auth.entity.AnonymousSession;
import com.daner.word.entity.Word;
import com.daner.word.repository.WordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase
class AnonymousSessionRepositoryTest {

    @Autowired private AnonymousSessionRepository anonymousSessionRepository;
    @Autowired private WordRepository wordRepository;

    @Test
    void find_by_token_and_word_id_returns_label() {
        Word room = wordRepository.save(Word.builder().word("퇴근").build());
        UUID token = UUID.randomUUID();
        anonymousSessionRepository.save(AnonymousSession.builder()
                .token(token).word(room).label("익명1").build());

        var found = anonymousSessionRepository.findByTokenAndWordId(token, room.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getLabel()).isEqualTo("익명1");
    }

    @Test
    void same_token_can_have_different_labels_in_different_rooms() {
        Word roomA = wordRepository.save(Word.builder().word("퇴근").build());
        Word roomB = wordRepository.save(Word.builder().word("도파민").build());
        UUID token = UUID.randomUUID();

        anonymousSessionRepository.save(AnonymousSession.builder()
                .token(token).word(roomA).label("익명1").build());
        anonymousSessionRepository.save(AnonymousSession.builder()
                .token(token).word(roomB).label("익명3").build());
        anonymousSessionRepository.flush();

        assertThat(anonymousSessionRepository.findByTokenAndWordId(token, roomA.getId()))
                .map(AnonymousSession::getLabel).hasValue("익명1");
        assertThat(anonymousSessionRepository.findByTokenAndWordId(token, roomB.getId()))
                .map(AnonymousSession::getLabel).hasValue("익명3");
    }

    @Test
    void same_token_in_same_room_violates_unique_constraint() {
        Word room = wordRepository.save(Word.builder().word("퇴근").build());
        UUID token = UUID.randomUUID();
        anonymousSessionRepository.save(AnonymousSession.builder()
                .token(token).word(room).label("익명1").build());

        assertThatThrownBy(() -> anonymousSessionRepository.saveAndFlush(
                AnonymousSession.builder().token(token).word(room).label("익명2").build()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
