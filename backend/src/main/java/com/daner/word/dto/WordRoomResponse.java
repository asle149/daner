package com.daner.word.dto;

import com.daner.word.entity.Word;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WordRoomResponse(
        Long id,
        String word,
        Integer commentCount,
        Integer likeCount,
        LocalDateTime createdAt,
        boolean exists,
        String message
) {

    public static WordRoomResponse from(Word w) {
        return new WordRoomResponse(w.getId(), w.getWord(), w.getCommentCount(),
                w.getLikeCount(), w.getCreatedAt(), true, null);
    }

    public static WordRoomResponse empty(String normalizedWord) {
        return new WordRoomResponse(null, normalizedWord, null, null, null, false,
                "이 단어로 처음 들어왔어요. 첫 댓글을 남기면 방이 생겨요.");
    }
}
