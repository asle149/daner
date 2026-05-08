package com.daner.comment.controller;

import com.daner.auth.service.AnonymousTokenResolver;
import com.daner.comment.dto.CommentCreateRequest;
import com.daner.comment.dto.ReplyResponse;
import com.daner.comment.dto.ReplySliceResponse;
import com.daner.comment.service.CommentService;
import com.daner.common.exception.BusinessException;
import com.daner.common.exception.ErrorCode;
import com.daner.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final AnonymousTokenResolver anonymousTokenResolver;

    @GetMapping("/{id}/replies")
    public ApiResponse<ReplySliceResponse> listReplies(
            @PathVariable Long id,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit,
            @AuthenticationPrincipal Long currentUserId) {
        return ApiResponse.ok(commentService.listReplies(id, cursor, limit, currentUserId));
    }

    @PostMapping("/{id}/replies")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReplyResponse> createReply(
            @PathVariable Long id,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal Long currentUserId,
            @RequestHeader(value = AnonymousTokenResolver.HEADER, required = false) String anonymousHeader) {
        UUID anonymousToken = anonymousTokenResolver.resolve(anonymousHeader).orElse(null);
        return ApiResponse.ok(commentService.createReply(id, request, currentUserId, anonymousToken));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal Long currentUserId) {
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        commentService.delete(id, currentUserId);
        return ApiResponse.ok();
    }
}
