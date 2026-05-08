package com.daner.word.controller;

import com.daner.comment.dto.CommentSliceResponse;
import com.daner.comment.service.CommentService;
import com.daner.common.response.ApiResponse;
import com.daner.word.dto.WordRoomResponse;
import com.daner.word.service.WordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/words")
@RequiredArgsConstructor
public class WordController {

    private final WordService wordService;
    private final CommentService commentService;

    @GetMapping("/{word}")
    public ApiResponse<WordRoomResponse> getRoom(@PathVariable String word) {
        return ApiResponse.ok(wordService.getRoom(word));
    }

    @GetMapping("/{word}/comments")
    public ApiResponse<CommentSliceResponse> listComments(
            @PathVariable String word,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit,
            @AuthenticationPrincipal Long currentUserId) {
        return ApiResponse.ok(commentService.listForWord(word, sort, cursor, limit, currentUserId));
    }
}
