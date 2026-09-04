package com.nextgen.onlinebanking.security;

import com.nextgen.onlinebanking.model.User;
import com.nextgen.onlinebanking.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final TokenRevocationService tokenRevocationService;

    public JwtAuthenticationFilter(
        JwtService jwtService,
        UserRepository userRepository,
            TokenRevocationService tokenRevocationService) {

        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.tokenRevocationService = tokenRevocationService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token =
                authorizationHeader.substring(7);
 
        if (tokenRevocationService.isTokenRevoked(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {

            String email =
                    jwtService.extractEmail(token);

            if (SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null) {

                User user =
                        userRepository.findByEmail(email)
                                .orElse(null);

                if (user != null &&
                        jwtService.isTokenValid(token, email)) {

                   UsernamePasswordAuthenticationToken authentication =
                       new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            java.util.Collections.singletonList(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                    "ROLE_" + user.getRole().name()
                                           )
                        
                                   )
                
                           );
        

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);
                }
            }

        } catch (Exception exception) {
            // Invalid or expired JWT.
            // Continue the request without authentication.
        }

        filterChain.doFilter(request, response);
    }
}
