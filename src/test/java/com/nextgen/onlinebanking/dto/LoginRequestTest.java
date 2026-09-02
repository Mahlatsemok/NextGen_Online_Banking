package com.nextgen.onlinebanking.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory()
                .getValidator();
    }

    @Test
    void shouldCreateLoginRequest() {

        LoginRequest request =
                new LoginRequest(
                        "john@example.com",
                        "password123"
                );

        assertEquals(
                "john@example.com",
                request.getEmail()
        );

        assertEquals(
                "password123",
                request.getPassword()
        );
    }

    @Test
    void shouldRejectBlankEmail() {

        LoginRequest request =
                new LoginRequest(
                        "",
                        "password123"
                );

        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldRejectInvalidEmail() {

        LoginRequest request =
                new LoginRequest(
                        "invalid-email",
                        "password123"
                );

        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldRejectBlankPassword() {

        LoginRequest request =
                new LoginRequest(
                        "john@example.com",
                        ""
                );

        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }
}
