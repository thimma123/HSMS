package com.hsms.execution_service.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.hsms.execution_service.config.FeignConfig;
import com.hsms.execution_service.model.PaymentDetailResponseDTO;
import com.hsms.execution_service.model.PaymentRequestDTO;
import com.hsms.execution_service.model.PaymentResponseDTO;

@FeignClient(
    name = "payment-service",
    configuration = FeignConfig.class
)
public interface PaymentClient {
    @PostMapping("/api/payments/save")
    PaymentResponseDTO createPayment(@RequestBody PaymentRequestDTO dto);

    @GetMapping("/api/payments/{id}")
    PaymentDetailResponseDTO getPayment(@PathVariable("id") Long id);

    @GetMapping("/api/payments/service-request/{serviceRequestId}")
    PaymentDetailResponseDTO getPaymentByServiceRequestId(@PathVariable("serviceRequestId") Long serviceRequestId);

    @PutMapping("/api/payments/status/{paymentId}")
    PaymentDetailResponseDTO updatePaymentStatus(
            @PathVariable("paymentId") Long paymentId, 
            @RequestParam("status") String status,
            @RequestParam(value = "method", required = false) String method);
}
