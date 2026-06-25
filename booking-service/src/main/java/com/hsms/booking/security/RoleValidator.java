package com.hsms.booking.security;

import java.util.Arrays;
import org.springframework.security.access.AccessDeniedException;

public final class RoleValidator {

	private RoleValidator() {
		throw new IllegalStateException("Utility class");
	}

	public static void validate(String role, String... allowedRoles) {

		boolean allowed = Arrays.stream(allowedRoles).anyMatch(r -> r.equals(role));

		if (!allowed) {

			throw new AccessDeniedException("Access Denied");
		}
	}
}