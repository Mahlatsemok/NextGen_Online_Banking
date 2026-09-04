package com.nextgen.onlinebanking.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
                    HttpSecurity http) throws Exception {

            http
                            .authorizeHttpRequests(auth -> auth
                                            .requestMatchers(
                                                            "/api/auth/register",
                                                            "/api/auth/login")
                                            .permitAll()
                                            .requestMatchers("/api/admin/**")
                                            .hasRole("ADMIN")
                                            .anyRequest()
                                            .authenticated())
                            .csrf(csrf -> csrf
                                            .ignoringRequestMatchers(
                                                            "/api/auth/register",
                                                            "/api/auth/login"))
                            .exceptionHandling(exception -> exception
                                            .authenticationEntryPoint(
                                                            (request, response, authException) -> response.sendError(
                                                                            401,
                                                                            "Unauthorized")))
                            .addFilterBefore(
                                            jwtAuthenticationFilter,
                                            UsernamePasswordAuthenticationFilter.class);

            return http.build();
    }

}
