package com.daner.common.response;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void ok_with_data_sets_success_true_and_no_error() {
        ApiResponse<String> response = ApiResponse.ok("hello");

        assertThat(response.success()).isTrue();
        assertThat(response.data()).isEqualTo("hello");
        assertThat(response.error()).isNull();
    }

    @Test
    void ok_without_data_sets_data_null() {
        ApiResponse<Void> response = ApiResponse.ok();

        assertThat(response.success()).isTrue();
        assertThat(response.data()).isNull();
        assertThat(response.error()).isNull();
    }

    @Test
    void fail_with_code_and_message_sets_success_false_and_data_null() {
        ApiResponse<Void> response = ApiResponse.fail("WORD_NOT_FOUND", "해당 단어 방이 존재하지 않습니다.");

        assertThat(response.success()).isFalse();
        assertThat(response.data()).isNull();
        assertThat(response.error()).isNotNull();
        assertThat(response.error().code()).isEqualTo("WORD_NOT_FOUND");
        assertThat(response.error().message()).isEqualTo("해당 단어 방이 존재하지 않습니다.");
    }

    @Test
    void fail_with_error_response_object_wraps_correctly() {
        ErrorResponse err = new ErrorResponse("INVALID_INPUT", "잘못된 요청");
        ApiResponse<Void> response = ApiResponse.fail(err);

        assertThat(response.success()).isFalse();
        assertThat(response.data()).isNull();
        assertThat(response.error()).isSameAs(err);
    }
}
