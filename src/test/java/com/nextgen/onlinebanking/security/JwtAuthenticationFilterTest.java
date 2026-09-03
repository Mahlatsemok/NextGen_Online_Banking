package com.nextgen.onlinebanking.security;

import com.nextgen.onlinebanking.model.User;
import com.nextgen.onlinebanking.repository.UserRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenRevocationService tokenRevocationService;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {

        filter = new JwtAuthenticationFilter(
                jwtService,
                userRepository,
                tokenRevocationService        
        );

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateUserWithValidJwt() throws Exception {

        String token = "valid.jwt.token";
        String email = "john@example.com";

        User user = new User(
                "John",
                "Doe",
                email,
                "$2a$10$hashedPassword");

        user.setId(1L);

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractEmail(token))
                .thenReturn(email);

        when(jwtService.isTokenValid(token, email))
                .thenReturn(true);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        filter.doFilter(
                request,
                response,
                filterChain);

        assertNotNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication());

        assertEquals(
                user,
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal());

        verify(jwtService)
                .extractEmail(token);

        verify(jwtService)
                .isTokenValid(token, email);

        verify(userRepository)
                .findByEmail(email);

        verify(filterChain)
                .doFilter(request, response);
    }
    
    @Test
void shouldContinueWithoutAuthenticationWhenJwtIsMissing()
        throws Exception {

    MockHttpServletRequest request = new MockHttpServletRequest();

    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(
            request,
            response,
            filterChain);

    assertNull(
            SecurityContextHolder
                    .getContext()
                    .getAuthentication());

    verifyNoInteractions(jwtService);
    verifyNoInteractions(userRepository);

    verify(filterChain)
            .doFilter(request, response);
}

@Test
void shouldNotAuthenticateUserWithInvalidJwt()
        throws Exception {

    String token = "invalid.jwt.token";

    MockHttpServletRequest request = new MockHttpServletRequest();

    request.addHeader(
            "Authorization",
            "Bearer " + token);

    MockHttpServletResponse response = new MockHttpServletResponse();

    when(jwtService.extractEmail(token))
            .thenThrow(new RuntimeException("Invalid JWT"));

    filter.doFilter(
            request,
            response,
            filterChain);

    assertNull(
            SecurityContextHolder
                    .getContext()
                    .getAuthentication());

    verify(jwtService)
            .extractEmail(token);

    verifyNoInteractions(userRepository);

    verify(filterChain)
            .doFilter(request, response);
}

@Test
void shouldNotAuthenticateUnknownUser()
        throws Exception {

    String token = "valid.jwt.token";
    String email = "unknown@example.com";

    MockHttpServletRequest request = new MockHttpServletRequest();

    request.addHeader(
            "Authorization",
            "Bearer " + token);

    MockHttpServletResponse response = new MockHttpServletResponse();

    when(jwtService.extractEmail(token))
            .thenReturn(email);

    when(userRepository.findByEmail(email))
            .thenReturn(Optional.empty());

    filter.doFilter(
            request,
            response,
            filterChain);

    assertNull(
            SecurityContextHolder
                    .getContext()
                    .getAuthentication());

    verify(jwtService)
            .extractEmail(token);

    verify(userRepository)
            .findByEmail(email);

    verify(jwtService, never())
            .isTokenValid(anyString(), anyString());

    verify(filterChain)
            .doFilter(request, response);
}

@Test
void shouldNotAuthenticateRevokedToken() throws Exception {

    String token = "revoked-token";

    MockHttpServletRequest request = new MockHttpServletRequest();

    request.addHeader(
            "Authorization",
            "Bearer " + token);

    MockHttpServletResponse response = new MockHttpServletResponse();

    when(tokenRevocationService.isTokenRevoked(token))
            .thenReturn(true);

    filter.doFilter(
            request,
            response,
            filterChain);

    assertNull(
            SecurityContextHolder
                    .getContext()
                    .getAuthentication());

    verify(tokenRevocationService)
            .isTokenRevoked(token);

    verifyNoInteractions(jwtService);
    verifyNoInteractions(userRepository);

    verify(filterChain)
            .doFilter(request, response);
}

}
