package com.nextgen.onlinebanking.controller;

import com.nextgen.onlinebanking.security.SecurityConfig;
import com.nextgen.onlinebanking.security.JwtService;
import com.nextgen.onlinebanking.security.TokenRevocationService;
import com.nextgen.onlinebanking.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private TokenRevocationService tokenRevocationService;

    @Test
    void shouldRejectUnauthenticatedUser() throws Exception {

        mockMvc.perform(
                get("/api/admin/dashboard")
        )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldRejectRegularUser() throws Exception {

        mockMvc.perform(
                get("/api/admin/dashboard")
        )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminUser() throws Exception {

        mockMvc.perform(
                get("/api/admin/dashboard")
        )
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Admin dashboard access granted"
                ));
    }
}
