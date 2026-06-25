package com.abc.paymentservice.model;

import java.time.LocalDateTime;


import lombok.Data;

@Data

public class BookingResponseDTO {
	

	private Long requestId;
	

	private Long customerId;
	

	private Long categoryId;
	

	private String serviceType;
	
	
	private String address;
	

	private String status;
	

	private LocalDateTime createdAt;
	
}