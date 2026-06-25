package com.hsms.userservice.security;

public class Roles {

	private Roles() {
		throw new IllegalStateException("Utility class");
	}

	public static final String ADMIN = "ADMIN";
	public static final String CUSTOMER = "CUSTOMER";
	public static final String TECHNICIAN = "TECHNICIAN";
	public static final String SERVICE_MANAGER = "SERVICE_MANAGER";
}