package com.daner.like.controller;

import com.daner.common.exception.BusinessException;
import com.daner.common.exception.ErrorCode;
import com.daner.common.response.ApiResponse;
import com.daner.like.dto.LikeStateResponse;
import com.daner.like.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comments/{id}/like")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LikeStateResponse> like(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return ApiResponse.ok(likeService.like(id, userId));
    }

    @DeleteMapping
    public ApiResponse<LikeStateResponse> unlike(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return ApiResponse.ok(likeService.unlike(id, userId));
    }
}
