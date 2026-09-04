package com.nextgen.onlinebanking.controller;

import com.nextgen.onlinebanking.model.User;
import com.nextgen.onlinebanking.model.UserRole;
import com.nextgen.onlinebanking.repository.UserRepository;
import com.nextgen.onlinebanking.security.JwtService;
import com.nextgen.onlinebanking.security.TokenRevocationService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import java.util.Date;

@SpringBootTest
@AutoConfigureMockMvc
class AdminAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenRevocationService tokenRevocationService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldRejectUserRoleFromAdminEndpoint() throws Exception {

        User user = new User(
                "John",
                "User",
                "user@example.com",
                passwordEncoder.encode("Password123")
        );

        user.setRole(UserRole.USER);

        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());

        mockMvc.perform(
                get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + token)
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminRoleToAccessAdminEndpoint() throws Exception {

        User admin = new User(
                "Jane",
                "Admin",
                "admin@example.com",
                passwordEncoder.encode("Password123")
        );

        admin.setRole(UserRole.ADMIN);

        userRepository.save(admin);

        String token = jwtService.generateToken(admin.getEmail());

        mockMvc.perform(
                get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + token)
        )
        .andExpect(status().isOk())
        .andExpect(
                content().string("Admin dashboard access granted")
        );
    }

    @Test
    void shouldRejectRequestWithoutToken() throws Exception {

        mockMvc.perform(
                get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void shouldRejectRevokedAdminToken() throws Exception {

        User admin = new User(
                "Jane",
                "Admin",
                "admin@example.com",
                passwordEncoder.encode("Password123"));

        admin.setRole(UserRole.ADMIN);

        userRepository.save(admin);

        String token = jwtService.generateToken(admin.getEmail());

        tokenRevocationService.revokeToken(token);

        mockMvc.perform(
                get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectInvalidJwt() throws Exception {

        mockMvc.perform(
                get("/api/admin/dashboard")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectJwtForUnknownUser() throws Exception {

        String token = jwtService.generateToken("unknown@example.com");

        mockMvc.perform(
                get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectExpiredJwt() throws Exception {

        SecretKey key = Keys.hmacShaKeyFor(
                "NextGenBankingSecretKeyForJwtAuthentication123456789"
                        .getBytes(StandardCharsets.UTF_8));

        Date now = new Date();

        String expiredToken = Jwts.builder()
                .subject("admin@example.com")
                .issuedAt(new Date(now.getTime() - 7200000))
                .expiration(new Date(now.getTime() - 3600000))
                .signWith(key)
                .compact();

        mockMvc.perform(
                get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }




}
