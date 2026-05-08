package com.daner.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(
        @NotBlank(message = "내용을 입력해주세요.")
        @Size(min = 1, max = 1000, message = "내용은 1~1000자여야 합니다.")
        String content,
        Boolean anonymous
) {
}
