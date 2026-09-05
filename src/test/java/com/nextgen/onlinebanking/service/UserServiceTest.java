package com.nextgen.onlinebanking.service;

import com.nextgen.onlinebanking.model.User;
import com.nextgen.onlinebanking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldRegisterUser() {

        User user = userService.registerUser(
                "John",
                "Doe",
                "john@example.com",
                "password123"
        );

        assertNotNull(user.getId());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("john@example.com", user.getEmail());
    }

    @Test
    void shouldHashPassword() {

        User user = userService.registerUser(
                "Jane",
                "Smith",
                "jane@example.com",
                "password123"
        );

        assertNotEquals("password123", user.getPassword());
        assertTrue(user.getPassword().startsWith("$2"));
    }

    @Test
    void shouldSaveUserToDatabase() {

        userService.registerUser(
                "Peter",
                "Jones",
                "peter@example.com",
                "password123"
        );

        User savedUser =
                userRepository.findByEmail("peter@example.com")
                        .orElseThrow();

        assertEquals("Peter", savedUser.getFirstName());
        assertEquals("Jones", savedUser.getLastName());
        assertEquals("peter@example.com", savedUser.getEmail());
    }

    @Test
    void shouldRejectDuplicateEmail() {

        userService.registerUser(
                "John",
                "Doe",
                "duplicate@example.com",
                "password123"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser(
                        "Jane",
                        "Smith",
                        "duplicate@example.com",
                        "password456"
                )
        );
    }
}
