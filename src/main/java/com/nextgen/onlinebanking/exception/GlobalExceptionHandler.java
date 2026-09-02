package com.nextgen.onlinebanking.exception;

import com.nextgen.onlinebanking.dto.ApiErrorResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nextgen.onlinebanking.exception.InvalidCredentialsException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
                    MethodArgumentNotValidException exception) {

            Map<String, String> errors = new LinkedHashMap<>();

            exception.getBindingResult()
                            .getFieldErrors()
                            .forEach(error -> errors.put(
                                            error.getField(),
                                            error.getDefaultMessage()));

            ApiErrorResponse response = new ApiErrorResponse(
                            HttpStatus.BAD_REQUEST.value(),
                            "VALIDATION_ERROR",
                            "Request validation failed",
                            errors);

            return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(response);
    }
    
    @ExceptionHandler(InvalidCredentialsException.class)
public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
                InvalidCredentialsException exception) {

        ApiErrorResponse response = new ApiErrorResponse(
                        HttpStatus.UNAUTHORIZED.value(),
                        "UNAUTHORIZED",
                        exception.getMessage(),
                        null);

        return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(response);
}


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException exception) {

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                "CONFLICT",
                exception.getMessage(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
                    Exception exception) {

            ApiErrorResponse response = new ApiErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "INTERNAL_SERVER_ERROR",
                            "An unexpected error occurred",
                            null);

            return ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(response);
    }
    
}
