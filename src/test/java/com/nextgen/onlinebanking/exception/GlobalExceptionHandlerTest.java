package com.nextgen.onlinebanking.exception;

import com.nextgen.onlinebanking.dto.ApiErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void shouldHandleDuplicateEmail() {

        IllegalArgumentException exception =
                new IllegalArgumentException("Email already registered");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleIllegalArgumentException(exception);

        assertEquals(
                HttpStatus.CONFLICT.value(),
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "CONFLICT",
                response.getBody().getError()
        );

        assertEquals(
                "Email already registered",
                response.getBody().getMessage()
        );
    }

    @Test
    void shouldHandleUnexpectedException() {

        Exception exception =
                new RuntimeException("Database failure");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleGenericException(exception);

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "INTERNAL_SERVER_ERROR",
                response.getBody().getError()
        );

        assertEquals(
                "An unexpected error occurred",
                response.getBody().getMessage()
        );
    }
}
