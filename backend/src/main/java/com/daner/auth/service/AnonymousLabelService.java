package com.daner.auth.service;

import com.daner.auth.entity.AnonymousSession;
import com.daner.auth.repository.AnonymousSessionRepository;
import com.daner.word.entity.Word;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnonymousLabelService {

    private final AnonymousSessionRepository anonymousSessionRepository;

    @Transactional
    public String resolveOrAssign(UUID token, Word word) {
        return anonymousSessionRepository.findByTokenAndWordId(token, word.getId())
                .map(AnonymousSession::getLabel)
                .orElseGet(() -> assign(token, word));
    }

    private String assign(UUID token, Word word) {
        long count = anonymousSessionRepository.countByWordId(word.getId());
        String label = "익명" + (count + 1);
        anonymousSessionRepository.save(AnonymousSession.builder()
                .token(token).word(word).label(label).build());
        return label;
    }
}
