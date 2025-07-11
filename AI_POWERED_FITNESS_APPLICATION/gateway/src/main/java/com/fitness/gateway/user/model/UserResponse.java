package com.fitness.gateway.user.model;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private String id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String keycloakId;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
