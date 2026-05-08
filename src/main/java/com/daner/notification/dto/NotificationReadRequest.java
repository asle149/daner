package com.daner.notification.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record NotificationReadRequest(
        @NotEmpty(message = "처리할 알림 id가 필요합니다.")
        List<Long> notificationIds
) {
}
