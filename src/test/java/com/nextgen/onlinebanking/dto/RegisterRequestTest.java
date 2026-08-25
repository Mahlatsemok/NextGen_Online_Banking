package com.nextgen.onlinebanking.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RegisterRequestTest {

    private final ValidatorFactory validatorFactory =
            Validation.buildDefaultValidatorFactory();

    private final Validator validator =
            validatorFactory.getValidator();

    @Test
    void shouldCreateRegisterRequest() {

        RegisterRequest request = new RegisterRequest(
                "John",
                "Doe",
                "john@example.com",
                "password123"
        );

        assertEquals("John", request.getFirstName());
        assertEquals("Doe", request.getLastName());
        assertEquals("john@example.com", request.getEmail());
        assertEquals("password123", request.getPassword());
    }

    @Test
    void shouldSetAndGetFields() {

        RegisterRequest request = new RegisterRequest();

        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setEmail("jane@example.com");
        request.setPassword("password456");

        assertEquals("Jane", request.getFirstName());
        assertEquals("Smith", request.getLastName());
        assertEquals("jane@example.com", request.getEmail());
        assertEquals("password456", request.getPassword());
    }

    @Test
    void shouldRejectBlankFirstName() {

        RegisterRequest request = new RegisterRequest(
                "",
                "Doe",
                "john@example.com",
                "password123"
        );

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(v -> v.getMessage()
                                .equals("First name is required"))
        );
    }

    @Test
    void shouldRejectBlankLastName() {

        RegisterRequest request = new RegisterRequest(
                "John",
                "",
                "john@example.com",
                "password123"
        );

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(v -> v.getMessage()
                                .equals("Last name is required"))
        );
    }

    @Test
    void shouldRejectInvalidEmail() {

        RegisterRequest request = new RegisterRequest(
                "John",
                "Doe",
                "invalid-email",
                "password123"
        );

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(v -> v.getMessage()
                                .equals("Email must be valid"))
        );
    }

    @Test
    void shouldRejectBlankEmail() {

        RegisterRequest request = new RegisterRequest(
                "John",
                "Doe",
                "",
                "password123"
        );

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(v -> v.getMessage()
                                .equals("Email is required"))
        );
    }

    @Test
    void shouldRejectBlankPassword() {

        RegisterRequest request = new RegisterRequest(
                "John",
                "Doe",
                "john@example.com",
                ""
        );

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(v -> v.getMessage()
                                .equals("Password is required"))
        );
    }

    @Test
    void shouldRejectPasswordShorterThanEightCharacters() {

        RegisterRequest request = new RegisterRequest(
                "John",
                "Doe",
                "john@example.com",
                "1234567"
        );

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(v -> v.getMessage()
                                .equals("Password must be at least 8 characters"))
        );
    }

    @Test
    void shouldAcceptValidRegistrationRequest() {

        RegisterRequest request = new RegisterRequest(
                "John",
                "Doe",
                "john@example.com",
                "password123"
        );

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }
}
