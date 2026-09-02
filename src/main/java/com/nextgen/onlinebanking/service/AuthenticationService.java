package com.nextgen.onlinebanking.service;

import com.nextgen.onlinebanking.model.User;
import com.nextgen.onlinebanking.model.UserStatus;
import com.nextgen.onlinebanking.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nextgen.onlinebanking.exception.InvalidCredentialsException;

import java.time.LocalDateTime;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User authenticate(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        //new IllegalArgumentException(
                         new InvalidCredentialsException(
                                "Invalid email or password"));

        if (user.getStatus() == UserStatus.LOCKED ||
                user.getStatus() == UserStatus.SUSPENDED ||
                user.getStatus() == UserStatus.CLOSED) {

            throw new IllegalArgumentException(
                    "Account is not available for login");
        }

        if (!passwordEncoder.matches(
                password,
                user.getPassword())) {

            user.setFailedLoginAttempts(
                    user.getFailedLoginAttempts() + 1
            );

            userRepository.save(user);

            throw new IllegalArgumentException(
                    "Invalid email or password");
        }

        user.setFailedLoginAttempts(0);
        user.setLastLoginAt(LocalDateTime.now());

        return userRepository.save(user);
    }
}
