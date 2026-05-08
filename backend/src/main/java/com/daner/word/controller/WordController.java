package com.daner.word.controller;

import com.daner.auth.service.AnonymousTokenResolver;
import com.daner.comment.dto.CommentCreateRequest;
import com.daner.comment.dto.CommentResponse;
import com.daner.comment.dto.CommentSliceResponse;
import com.daner.comment.service.CommentService;
import com.daner.common.response.ApiResponse;
import com.daner.word.dto.WordRoomResponse;
import com.daner.word.service.WordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/words")
@RequiredArgsConstructor
public class WordController {

    private final WordService wordService;
    private final CommentService commentService;
    private final AnonymousTokenResolver anonymousTokenResolver;

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

    @PostMapping("/{word}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CommentResponse> createComment(
            @PathVariable String word,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal Long currentUserId,
            @RequestHeader(value = AnonymousTokenResolver.HEADER, required = false) String anonymousHeader) {
        UUID anonymousToken = anonymousTokenResolver.resolve(anonymousHeader).orElse(null);
        return ApiResponse.ok(commentService.createTopLevel(word, request, currentUserId, anonymousToken));
    }
}
