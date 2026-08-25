package com.nextgen.onlinebanking.repository;

import com.nextgen.onlinebanking.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveUser() {

        User user = new User(
                "John",
                "Doe",
                "john@example.com",
                "password123"
        );

        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());
        assertEquals("John", savedUser.getFirstName());
        assertEquals("Doe", savedUser.getLastName());
        assertEquals("john@example.com", savedUser.getEmail());
    }

    @Test
    void shouldFindUserByEmail() {

        User user = new User(
                "Jane",
                "Smith",
                "jane@example.com",
                "password456"
        );

        userRepository.save(user);

        Optional<User> foundUser =
                userRepository.findByEmail("jane@example.com");

        assertTrue(foundUser.isPresent());
        assertEquals("Jane", foundUser.get().getFirstName());
        assertEquals("jane@example.com", foundUser.get().getEmail());
    }

    @Test
    void shouldCheckIfEmailExists() {

        User user = new User(
                "Peter",
                "Jones",
                "peter@example.com",
                "password789");

        userRepository.save(user);

        assertTrue(userRepository.existsByEmail("peter@example.com"));
        assertFalse(userRepository.existsByEmail("unknown@example.com"));
    }
    
    @Test
    void shouldSetCreatedAtAndUpdatedAtWhenUserIsSaved() {

        User user = new User(
                "John",
                "Doe",
                "timestamp@example.com",
                "hashedPassword");

        User savedUser = userRepository.saveAndFlush(user);

        assertNotNull(savedUser.getCreatedAt());
        assertNotNull(savedUser.getUpdatedAt());

        assertEquals(
                savedUser.getCreatedAt(),
                savedUser.getUpdatedAt());
    }

    @Test
    void shouldUpdateUpdatedAtWithoutChangingCreatedAt() throws InterruptedException {

        User user = new User(
            "Jane",
            "Doe",
            "timestamps@example.com",
            "hashedPassword");

        User savedUser = userRepository.saveAndFlush(user);

        LocalDateTime createdAt = savedUser.getCreatedAt();
        LocalDateTime firstUpdatedAt = savedUser.getUpdatedAt();

        Thread.sleep(10);

        savedUser.setFirstName("Janet");

        User updatedUser = userRepository.saveAndFlush(savedUser);

        assertEquals(createdAt, updatedUser.getCreatedAt());
        assertNotNull(updatedUser.getUpdatedAt());

        assertTrue(
            updatedUser.getUpdatedAt().isAfter(firstUpdatedAt));
    }

}
