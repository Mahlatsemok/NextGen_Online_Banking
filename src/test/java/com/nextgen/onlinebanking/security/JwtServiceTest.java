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

        String token =
                jwtService.generateToken(
                        "john@example.com"
                );

        String[] parts = token.split("\\.");

        assertEquals(3, parts.length);
    }
}
