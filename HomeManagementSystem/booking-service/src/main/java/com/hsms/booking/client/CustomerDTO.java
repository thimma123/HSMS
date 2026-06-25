package com.hsms.booking.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Customer data from User Service
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDTO {

	private Long userId;

	private String name;

	private String email;

	private String address;

	private String city;

	private String pincode;
}