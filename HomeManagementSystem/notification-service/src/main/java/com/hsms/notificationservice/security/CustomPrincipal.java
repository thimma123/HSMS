package com.hsms.notificationservice.security;

public class CustomPrincipal {
    private final Long userId;
    private final String email;
    private final String role;

    public CustomPrincipal(Long userId, String email, String role) {
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String toString() {
        return "CustomPrincipal{userId=" + userId + ", email='" + email + "', role='" + role + "'}";
    }
}
