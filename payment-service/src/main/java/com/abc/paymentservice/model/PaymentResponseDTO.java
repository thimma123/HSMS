package com.abc.paymentservice.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {

	private Long paymentId;

	private Long bookingId;

	private Long customerId;

	private Double amount;

	private String paymentMethod;

	private String status;

	private LocalDateTime paymentDate;

}