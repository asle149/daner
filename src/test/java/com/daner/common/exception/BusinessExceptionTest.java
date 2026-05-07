package com.daner.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessExceptionTest {

    @Test
    void single_arg_constructor_uses_default_message_from_error_code() {
        BusinessException ex = new BusinessException(ErrorCode.NOT_FOUND);

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
        assertThat(ex.getMessage()).isEqualTo(ErrorCode.NOT_FOUND.getDefaultMessage());
        assertThat(ex.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void custom_message_overrides_default() {
        BusinessException ex = new BusinessException(ErrorCode.INVALID_INPUT, "단어에 띄어쓰기가 포함될 수 없습니다.");

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT);
        assertThat(ex.getMessage()).isEqualTo("단어에 띄어쓰기가 포함될 수 없습니다.");
    }

    @Test
    void cause_is_preserved() {
        IllegalStateException root = new IllegalStateException("boom");
        BusinessException ex = new BusinessException(ErrorCode.INTERNAL_ERROR, "wrapped", root);

        assertThat(ex.getCause()).isSameAs(root);
        assertThat(ex.getMessage()).isEqualTo("wrapped");
    }
}
