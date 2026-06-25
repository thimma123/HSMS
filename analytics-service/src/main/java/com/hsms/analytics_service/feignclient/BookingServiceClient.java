package com.hsms.analytics_service.feignclient;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import com.hsms.analytics_service.model.ServiceRequestDetailResponseDTO;

@FeignClient(name = "booking-service")
public interface BookingServiceClient {

    @GetMapping("/api/service-requests/summary")
    List<ServiceRequestDetailResponseDTO> getAllRequests();
}