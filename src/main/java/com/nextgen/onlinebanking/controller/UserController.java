package com.nextgen.onlinebanking.controller;

import com.nextgen.onlinebanking.dto.RegisterRequest;
import com.nextgen.onlinebanking.dto.UserResponse;
import com.nextgen.onlinebanking.model.User;
import com.nextgen.onlinebanking.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        User user = userService.registerUser(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPassword());

        UserResponse response = UserResponse.fromUser(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    
}
