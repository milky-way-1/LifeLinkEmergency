package com.emergency.model;

public class JwtResponse {
    private String token;
    private String refreshToken;
    private String type;
    private String id;
    private String email;
    private String name;
    private String role;

    public JwtResponse() {
        this.type = "Bearer"; // Default value
    }

    // Constructor with all fields
    public JwtResponse(String token, String refreshToken, String id, String email, String name, String role) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.type = "Bearer"; // Default value
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    // Getters
    public String getToken() {
        return token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    // Setters
    public void setToken(String token) {
        this.token = token;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Optional: toString method for debugging
    @Override
    public String toString() {
        return "JwtResponse{" +
                "token='" + token + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", type='" + type + '\'' +
                ", id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}

