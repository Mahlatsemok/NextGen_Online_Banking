package com.nextgen.onlinebanking.dto;

import com.nextgen.onlinebanking.model.User;

public class AuthenticationResponse {

    private String token;
    private Long id;
    private String firstName;
    private String lastName;
    private String email;

    public AuthenticationResponse(
            String token,
            Long id,
            String firstName,
            String lastName,
            String email) {

        this.token = token;
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }
}
