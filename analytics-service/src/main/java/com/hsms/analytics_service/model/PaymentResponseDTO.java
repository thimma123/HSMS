package com.hsms.analytics_service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {

    private Long paymentId;
    private Long serviceRequestId;
    private Double amount;
    private String paymentMethod;
    private String paymentStatus;
}