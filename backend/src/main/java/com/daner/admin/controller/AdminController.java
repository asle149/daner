package com.daner.admin.controller;

import com.daner.admin.dto.AdminStatsResponse;
import com.daner.admin.service.AdminStatsService;
import com.daner.common.response.ApiResponse;
import com.daner.common.security.AdminGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminStatsService adminStatsService;
    private final AdminGuard adminGuard;

    @GetMapping("/stats")
    public ApiResponse<AdminStatsResponse> stats(@AuthenticationPrincipal Long currentUserId) {
        adminGuard.requireAdmin(currentUserId);
        return ApiResponse.ok(adminStatsService.load());
    }
}
