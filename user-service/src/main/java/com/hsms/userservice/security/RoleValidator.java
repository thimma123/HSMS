package com.hsms.userservice.security;

import java.util.Arrays;

import com.hsms.userservice.exception.AccessDeniedException;

public class RoleValidator {

    private RoleValidator() {
        throw new IllegalStateException("Utility class");
    }

    public static void validate(String role,
                                String... allowedRoles) {

        boolean allowed =
                Arrays.stream(allowedRoles)
                        .anyMatch(r -> r.equals(role));

        if (!allowed) {
            throw new AccessDeniedException(
                    "You are not authorized to access this resource");
        }
    }
}