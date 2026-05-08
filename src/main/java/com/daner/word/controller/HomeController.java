package com.daner.word.controller;

import com.daner.common.response.ApiResponse;
import com.daner.word.dto.HomeResponse;
import com.daner.word.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping
    public ApiResponse<HomeResponse> home(@AuthenticationPrincipal Long currentUserId) {
        return ApiResponse.ok(homeService.getHome(currentUserId));
    }
}
