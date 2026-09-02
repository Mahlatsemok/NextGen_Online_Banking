package com.nextgen.onlinebanking.service;

import com.nextgen.onlinebanking.model.User;
import com.nextgen.onlinebanking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService =
                new AuthenticationService(
                        userRepository,
                        passwordEncoder
                );
    }

    @Test
    void shouldAuthenticateUserWithValidCredentials() {

        User user = new User(
                "John",
                "Doe",
                "john@example.com",
                "$2a$10$hashedPassword"
        );

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "password123",
                user.getPassword()
        )).thenReturn(true);

        when(userRepository.save(user))
                .thenReturn(user);

        User authenticatedUser =
                authenticationService.authenticate(
                        "john@example.com",
                        "password123"
                );

        assertNotNull(authenticatedUser);
        assertEquals(
                "john@example.com",
                authenticatedUser.getEmail()
        );

        assertEquals(
                0,
                authenticatedUser.getFailedLoginAttempts()
        );

        assertNotNull(
                authenticatedUser.getLastLoginAt()
        );

        verify(userRepository)
                .findByEmail("john@example.com");

        verify(passwordEncoder)
                .matches(
                        "password123",
                        user.getPassword()
                );

        verify(userRepository)
                .save(user);
    }

    @Test
    void shouldRejectUnknownEmail() {

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> authenticationService.authenticate(
                                "unknown@example.com",
                                "password123"
                        )
                );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );

        verify(userRepository)
                .findByEmail("unknown@example.com");

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldRejectIncorrectPassword() {

        User user = new User(
             "John",
             "Doe",
             "john@example.com",
             "$2a$10$hashedPassword");

        when(userRepository.findByEmail("john@example.com"))
             .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
             "wrongPassword",
             user.getPassword())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
             IllegalArgumentException.class,
             () -> authenticationService.authenticate(
                     "john@example.com",
                     "wrongPassword"));

        assertEquals(
             "Invalid email or password",
             exception.getMessage());

        verify(passwordEncoder)
             .matches(
                     "wrongPassword",
                     user.getPassword());

     // Failed login attempt should be recorded
        assertEquals(
             1,
             user.getFailedLoginAttempts());

        verify(userRepository)
             .save(user);
    }
    
    @Test
    void shouldResetFailedLoginAttemptsAfterSuccessfulLogin() {

        User user = new User(
                "John",
                "Doe",
                "john@example.com",
                "$2a$10$hashedPassword");

        user.setFailedLoginAttempts(3);

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "password123",
                user.getPassword())).thenReturn(true);

        when(userRepository.save(user))
                .thenReturn(user);

        User authenticatedUser = authenticationService.authenticate(
                "john@example.com",
                "password123");

        assertEquals(
                0,
                authenticatedUser.getFailedLoginAttempts());

        assertNotNull(
                authenticatedUser.getLastLoginAt());

        verify(userRepository)
                .save(user);
    }

}
