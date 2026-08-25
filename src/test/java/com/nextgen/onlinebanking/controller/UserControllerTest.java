package com.nextgen.onlinebanking.controller;

import com.nextgen.onlinebanking.dto.RegisterRequest;
import com.nextgen.onlinebanking.model.User;
import com.nextgen.onlinebanking.security.SecurityConfig;
import com.nextgen.onlinebanking.service.UserService;
import tools.jackson.databind.json.JsonMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void shouldRegisterUser() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "John",
                "Doe",
                "john@example.com",
                "password123"
        );

        User user = new User(
                "John",
                "Doe",
                "john@example.com",
                "$2a$10$hashedPassword"
        );

        user.setId(1L);

        when(userService.registerUser(
                anyString(),
                anyString(),
                anyString(),
                anyString()
        )).thenReturn(user);

        mockMvc.perform(
                post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.firstName").value("John"))
        .andExpect(jsonPath("$.lastName").value("Doe"))
        .andExpect(jsonPath("$.email").value("john@example.com"))
        .andExpect(jsonPath("$.password").doesNotExist());

        verify(userService).registerUser(
                "John",
                "Doe",
                "john@example.com",
                "password123"
        );
    }

    @Test
    void shouldRejectInvalidEmail() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "John",
                "Doe",
                "invalid-email",
                "password123"
        );

        mockMvc.perform(
                post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void shouldRejectBlankPassword() throws Exception {

            RegisterRequest request = new RegisterRequest(
                            "John",
                            "Doe",
                            "john@example.com",
                            "");

            mockMvc.perform(
                            post("/api/auth/register")
                                            .with(csrf())
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(objectMapper.writeValueAsString(request)))
                            .andExpect(status().isBadRequest());

            verifyNoInteractions(userService);
    }
    
    @Test
    void shouldReturnValidationErrorsForInvalidRequest() throws Exception {

            RegisterRequest request = new RegisterRequest(
                            "",
                            "",
                            "invalid-email",
                            "");

            mockMvc.perform(
                            post("/api/auth/register")
                                            .with(csrf())
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(objectMapper.writeValueAsString(request)))
                            .andExpect(status().isBadRequest())
                            .andExpect(jsonPath("$.status").value(400))
                            .andExpect(jsonPath("$.message").value("Validation failed"))
                            .andExpect(jsonPath("$.errors.firstName").exists())
                            .andExpect(jsonPath("$.errors.lastName").exists())
                            .andExpect(jsonPath("$.errors.email").exists())
                            .andExpect(jsonPath("$.errors.password").exists());

            verifyNoInteractions(userService);
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {

            RegisterRequest request = new RegisterRequest(
                            "John",
                            "Doe",
                            "existing@example.com",
                            "password123");

            when(userService.registerUser(
                            anyString(),
                            anyString(),
                            anyString(),
                            anyString())).thenThrow(
                                            new IllegalArgumentException("Email already registered"));

            mockMvc.perform(
                            post("/api/auth/register")
                                            .with(csrf())
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(objectMapper.writeValueAsString(request)))
                            .andExpect(status().isConflict())
                            .andExpect(jsonPath("$.status").value(409))
                            .andExpect(jsonPath("$.message")
                                            .value("Email already registered"));
    }


}
