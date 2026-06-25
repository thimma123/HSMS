package com.hsms.analytics_service.feignclient;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import com.hsms.analytics_service.model.PaymentResponseDTO;


@FeignClient(name = "payment-service")
public interface PaymentClient {

    @GetMapping("/api/payments/all")
    List<PaymentResponseDTO> getAllPayments();
}