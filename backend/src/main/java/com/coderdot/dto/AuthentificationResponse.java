package com.coderdot.dto;

import java.util.Set;

public class AuthentificationResponse {
    private String token;
    private Long userId;
    private String email;
    private String hashedPassword;
    private Set<String> roles;
    private Set<String> permissions;
    private String username;

    public AuthentificationResponse(String token, Long userId, String email, String hashedPassword, Set<String> roles, Set<String> permissions, String username) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.roles = roles;
        this.permissions = permissions;
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public String getUsername() {
        return username;
    }
}
