package com.hsms.gateway.config;

import java.util.List;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class RouterValidator {

    public static final List<String> OPEN_ENDPOINTS = List.of(
            "/api/auth/register", 
            "/api/auth/login"
    );

    public boolean isSecured(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        return OPEN_ENDPOINTS.stream().noneMatch(path::contains);
    }
}