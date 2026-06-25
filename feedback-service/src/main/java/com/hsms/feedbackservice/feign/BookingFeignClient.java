package com.hsms.feedbackservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.hsms.feedbackservice.dto.ServiceRequestResponseDTO;

@FeignClient(name = "booking-service")
public interface BookingFeignClient {
    @GetMapping("/api/service-requests/{id}")
    ServiceRequestResponseDTO getServiceRequestById(@PathVariable("id") Long id);
}
