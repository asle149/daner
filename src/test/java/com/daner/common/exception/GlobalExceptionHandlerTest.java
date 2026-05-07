package com.daner.common.exception;

import com.daner.common.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.lang.reflect.Method;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void business_exception_maps_to_status_and_code_from_error_code() {
        BusinessException ex = new BusinessException(ErrorCode.NOT_FOUND, "단어 방이 없습니다.");

        ResponseEntity<ApiResponse<Void>> response = handler.handleBusiness(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ApiResponse<Void> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.success()).isFalse();
        assertThat(body.data()).isNull();
        assertThat(body.error().code()).isEqualTo("NOT_FOUND");
        assertThat(body.error().message()).isEqualTo("단어 방이 없습니다.");
    }

    @Test
    void method_argument_not_valid_emits_validation_failed_with_joined_messages() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "email", "must not be blank"));
        bindingResult.addError(new FieldError("request", "password", "size must be at least 8"));
        Method method = Dummy.class.getDeclaredMethod("noop");
        MethodParameter parameter = new MethodParameter(method, -1);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ApiResponse<Void>> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("VALIDATION_FAILED");
        assertThat(response.getBody().error().message())
                .contains("email: must not be blank")
                .contains("password: size must be at least 8");
    }

    @Test
    void constraint_violation_emits_validation_failed() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        jakarta.validation.Path path = mock(jakarta.validation.Path.class);
        when(path.toString()).thenReturn("nickname");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("길이가 너무 짧습니다");
        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ApiResponse<Void>> response = handler.handleConstraintViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().error().code()).isEqualTo("VALIDATION_FAILED");
        assertThat(response.getBody().error().message()).contains("nickname").contains("길이가 너무 짧습니다");
    }

    @Test
    void not_readable_message_emits_invalid_input() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "broken JSON", new MockHttpInputMessage(new byte[0]));

        ResponseEntity<ApiResponse<Void>> response = handler.handleNotReadable(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().error().code()).isEqualTo("INVALID_INPUT");
    }

    @Test
    void illegal_argument_emits_invalid_input_with_message() {
        IllegalArgumentException ex = new IllegalArgumentException("단어에 띄어쓰기가 포함될 수 없습니다.");

        ResponseEntity<ApiResponse<Void>> response = handler.handleIllegalArgument(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().error().code()).isEqualTo("INVALID_INPUT");
        assertThat(response.getBody().error().message()).isEqualTo("단어에 띄어쓰기가 포함될 수 없습니다.");
    }

    @Test
    void no_handler_found_emits_not_found() {
        NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/missing", null);

        ResponseEntity<ApiResponse<Void>> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().error().code()).isEqualTo("NOT_FOUND");
    }

    @Test
    void unexpected_exception_emits_internal_error() {
        Exception ex = new Exception("boom");

        ResponseEntity<ApiResponse<Void>> response = handler.handleUnexpected(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().error().code()).isEqualTo("INTERNAL_ERROR");
    }

    @SuppressWarnings("unused")
    private static class Dummy {
        void noop() {
        }
    }
}
