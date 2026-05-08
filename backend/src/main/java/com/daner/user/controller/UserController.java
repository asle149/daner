package com.daner.user.controller;

import com.daner.common.exception.BusinessException;
import com.daner.common.exception.ErrorCode;
import com.daner.common.response.ApiResponse;
import com.daner.user.dto.MyProfileResponse;
import com.daner.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<MyProfileResponse> me(
            @AuthenticationPrincipal Long currentUserId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit) {
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return ApiResponse.ok(userService.getMyProfile(currentUserId, cursor, limit));
    }
}
