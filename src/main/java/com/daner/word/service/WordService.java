package com.daner.word.service;

import com.daner.common.util.WordNormalizer;
import com.daner.word.dto.WordRoomResponse;
import com.daner.word.entity.Word;
import com.daner.word.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WordService {

    private final WordRepository wordRepository;

    @Transactional(readOnly = true)
    public WordRoomResponse getRoom(String rawWord) {
        String normalized = WordNormalizer.normalize(rawWord);
        return wordRepository.findByWord(normalized)
                .map(WordRoomResponse::from)
                .orElseGet(() -> WordRoomResponse.empty(normalized));
    }

    @Transactional(readOnly = true)
    public Optional<Word> findByNormalized(String normalizedWord) {
        return wordRepository.findByWord(normalizedWord);
    }
}
