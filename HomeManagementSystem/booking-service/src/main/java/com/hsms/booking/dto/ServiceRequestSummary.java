package com.hsms.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Summary DTO for service requests (for reporting service)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequestSummary {
	private Long requestId;
	private Long customerId;
	private Long categoryId;
	private Long technicianId;
	private String status;
	private LocalDateTime createdAt;
	private BigDecimal basePrice;
	private String categoryName;
	private LocalDateTime scheduledDateTime;
	private String city;
}