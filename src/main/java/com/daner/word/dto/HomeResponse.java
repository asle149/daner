package com.daner.word.dto;

import java.time.LocalDateTime;
import java.util.List;

public record HomeResponse(List<MyWord> myWords, List<PopularWord> popularWords) {

    public record MyWord(Long id, String word, int commentCount,
                         LocalDateTime lastActivityAt, LocalDateTime myLastCommentAt) {
    }

    public record PopularWord(Long id, String word, int commentCount) {
    }
}
