package com.abc.paymentservice.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentDetailResponseDTO {
	private Long paymentId;
	private Long serviceRequestId;
	private Double amount;
	private String method;
	private String status;
}
