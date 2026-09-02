package com.nextgen.onlinebanking.controller;

import com.nextgen.onlinebanking.dto.AuthenticationResponse;
import com.nextgen.onlinebanking.dto.LoginRequest;
import com.nextgen.onlinebanking.dto.RegisterRequest;
import com.nextgen.onlinebanking.dto.UserResponse;
import com.nextgen.onlinebanking.model.User;
import com.nextgen.onlinebanking.service.AuthenticationService;
import com.nextgen.onlinebanking.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    public UserController(
            UserService userService,
            AuthenticationService authenticationService) {

        this.userService = userService;
        this.authenticationService = authenticationService;
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

        AuthenticationResponse response =
                AuthenticationResponse.fromUser(user);

        return ResponseEntity.ok(response);
    }
}
