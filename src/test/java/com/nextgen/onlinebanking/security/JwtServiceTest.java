package com.nextgen.onlinebanking.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {

        jwtService = new JwtService(
                "NextGenBankingSecretKeyForJwtAuthentication123456789",
                3600000
        );
    }

    @Test
    void shouldGenerateJwtToken() {

        String token =
                jwtService.generateToken(
                        "john@example.com"
                );

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void generatedTokenShouldHaveThreeParts() {

        String token = jwtService.generateToken(
                "john@example.com");

        String[] parts = token.split("\\.");

        assertEquals(3, parts.length);
    }
    
    @Test
    void shouldExtractEmailFromValidToken() {

        String token = jwtService.generateToken(
                "john@example.com");

        String email = jwtService.extractEmail(token);

        assertEquals(
                "john@example.com",
                email);
    }

    @Test
    void shouldValidateValidToken() {

        String token = jwtService.generateToken(
                "john@example.com");

        boolean valid = jwtService.isTokenValid(
                token,
                "john@example.com");

        assertTrue(valid);
    }

    @Test
    void shouldRejectTokenForDifferentEmail() {

        String token = jwtService.generateToken(
                "john@example.com");

        boolean valid = jwtService.isTokenValid(
                token,
                "jane@example.com");

        assertFalse(valid);
    }

    @Test
    void shouldRejectMalformedToken() {

        boolean valid = jwtService.isTokenValid(
                "this.is.not.a.valid.jwt",
                "john@example.com");

        assertFalse(valid);
    }

}
