package com.coderdot.dto;

import com.coderdot.entities.UserRole;

public class AuthentificationResponse {
    private Long userId;
    private UserRole role;
    private String jwtToken;  // Renommé en camelCase
private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AuthentificationResponse(String jwtToken, Long userId, UserRole role, String name) {
        this.userId = userId;
        this.role = role;
        this.jwtToken = jwtToken;
        this.name = name;

    }

    public String getJwtToken() {  // Ajouter ce getter pour inclure le token dans la réponse JSON
        return jwtToken;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
