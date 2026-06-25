package com.hsms.execution_service.model;

import com.hsms.execution_service.entity.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRecordRequestDTO {
	private Long serviceRequestId;
	private String remarks;
	private Double actualCost;

	@Schema(description = "Payment method", allowableValues = { "ONLINE", "CASH", "CARD", "UPI" })
	private PaymentMethod paymentMethod;

	private String technicianNotes;
	private String executionNotes;
}
