package com.nextgen.onlinebanking.controller;

import com.nextgen.onlinebanking.dto.AuthenticationResponse;
import com.nextgen.onlinebanking.dto.LoginRequest;
import com.nextgen.onlinebanking.dto.LogoutResponse;
import com.nextgen.onlinebanking.dto.RegisterRequest;
import com.nextgen.onlinebanking.dto.UserResponse;
import com.nextgen.onlinebanking.model.User;
import com.nextgen.onlinebanking.service.AuthenticationService;
import com.nextgen.onlinebanking.service.UserService;
import com.nextgen.onlinebanking.security.TokenRevocationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.nextgen.onlinebanking.security.JwtService;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final TokenRevocationService tokenRevocationService;

    public UserController(
        UserService userService,
        AuthenticationService authenticationService,
        JwtService jwtService,
            TokenRevocationService tokenRevocationService) {

        this.userService = userService;
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
        this.tokenRevocationService = tokenRevocationService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        User user = userService.registerUser(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPassword());

        UserResponse response =
                UserResponse.fromUser(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody LoginRequest request) {

        User user = authenticationService.authenticate(
                request.getEmail(),
                request.getPassword());

        String token = jwtService.generateToken(user.getEmail());

        AuthenticationResponse response = new AuthenticationResponse(
                token,
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail());

        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                UserResponse.fromUser(user));
    }
    
    @PostMapping("/logout")
public ResponseEntity<LogoutResponse> logout(
        HttpServletRequest request) {

    String authorizationHeader = request.getHeader("Authorization");

    if (authorizationHeader != null &&
            authorizationHeader.startsWith("Bearer ")) {

        String token = authorizationHeader.substring(7);

        tokenRevocationService.revokeToken(token);
    }

    return ResponseEntity.ok(
            new LogoutResponse("Logout successful"));
}

}
