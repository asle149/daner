package com.daner.notification.dto;

import java.util.List;

public record NotificationSliceResponse(List<NotificationResponse> notifications, String nextCursor) {
}
