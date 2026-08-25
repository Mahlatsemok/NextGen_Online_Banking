package com.nextgen.onlinebanking.dto;

import com.nextgen.onlinebanking.model.User;

public class UserResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;

    public UserResponse(Long id,
                        String firstName,
                        String lastName,
                        String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public static UserResponse fromUser(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail()
        );
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
