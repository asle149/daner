package com.daner.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "refresh_token이 필요합니다.")
        String refreshToken
) {
}
