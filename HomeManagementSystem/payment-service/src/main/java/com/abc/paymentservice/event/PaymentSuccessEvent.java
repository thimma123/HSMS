package com.abc.paymentservice.event;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSuccessEvent {
    private Long paymentId;
    private Long serviceRequestId;
    private Long customerId;
    private BigDecimal amount;
}
