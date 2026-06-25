package com.hsms.booking.exception;

public class ServiceRequestNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ServiceRequestNotFoundException(String message) {
		super(message);
	}
}
