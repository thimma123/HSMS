package com.hsms.execution_service.model;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentDetailResponseDTO {
    private Long paymentId;
    private Long bookingId;
    private Long customerId;
    private Double amount;
    private String paymentMethod;
    private String status;
    private LocalDateTime paymentDate;
}