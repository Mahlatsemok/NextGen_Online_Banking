package com.nextgen.onlinebanking.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldCreateUserWithConstructor() {

        User user = new User(
                "John",
                "Doe",
                "john@example.com",
                "password123"
        );

        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());

        // Default values
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertFalse(user.isEmailVerified());
        assertFalse(user.isPhoneVerified());
        assertFalse(user.isTwoFactorEnabled());
        assertEquals(0, user.getFailedLoginAttempts());

        // Fields not set by the constructor
        assertNull(user.getPhoneNumber());
        assertNull(user.getLockedUntil());
        assertNull(user.getLastLoginAt());
    }


    @Test
    void shouldSetAndGetUserFields() {

        User user = new User();

        user.setId(1L);
        user.setFirstName("Jane");
        user.setLastName("Smith");
        user.setEmail("jane@example.com");
        user.setPassword("password456");

        assertEquals(1L, user.getId());
        assertEquals("Jane", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertEquals("jane@example.com", user.getEmail());
        assertEquals("password456", user.getPassword());
    }


    @Test
    void shouldSetAndGetPhoneNumber() {

        User user = new User();

        user.setPhoneNumber("0712345678");

        assertEquals("0712345678", user.getPhoneNumber());
    }


    @Test
    void shouldSetAndGetUserStatus() {

        User user = new User();

        user.setStatus(UserStatus.LOCKED);

        assertEquals(UserStatus.LOCKED, user.getStatus());

        user.setStatus(UserStatus.SUSPENDED);

        assertEquals(UserStatus.SUSPENDED, user.getStatus());

        user.setStatus(UserStatus.CLOSED);

        assertEquals(UserStatus.CLOSED, user.getStatus());
    }


    @Test
    void shouldSetAndGetVerificationSettings() {

        User user = new User();

        user.setEmailVerified(true);
        user.setPhoneVerified(true);
        user.setTwoFactorEnabled(true);

        assertTrue(user.isEmailVerified());
        assertTrue(user.isPhoneVerified());
        assertTrue(user.isTwoFactorEnabled());
    }


    @Test
    void shouldSetAndGetFailedLoginAttempts() {

        User user = new User();

        user.setFailedLoginAttempts(3);

        assertEquals(3, user.getFailedLoginAttempts());
    }


    @Test
    void shouldSetAndGetLockedUntil() {

        User user = new User();

        LocalDateTime lockedUntil =
                LocalDateTime.now().plusMinutes(15);

        user.setLockedUntil(lockedUntil);

        assertEquals(lockedUntil, user.getLockedUntil());
    }


    @Test
    void shouldSetAndGetLastLoginAt() {

        User user = new User();

        LocalDateTime lastLogin = LocalDateTime.now();

        user.setLastLoginAt(lastLogin);

        assertEquals(lastLogin, user.getLastLoginAt());
    }
    
    
    @Test
    void shouldAssignUserRoleByDefault() {
    
            User user = new User(
                    "John",
                    "Doe",
                    "john@example.com",
                    "hashedPassword"
            );
    
            assertEquals(
                    UserRole.USER,
                    user.getRole()
            );
    }

}
