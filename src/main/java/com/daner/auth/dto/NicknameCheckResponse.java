package com.daner.auth.dto;

public record NicknameCheckResponse(boolean available, String reason) {

    public static NicknameCheckResponse ok() {
        return new NicknameCheckResponse(true, null);
    }

    public static NicknameCheckResponse rejected(String reason) {
        return new NicknameCheckResponse(false, reason);
    }
}
