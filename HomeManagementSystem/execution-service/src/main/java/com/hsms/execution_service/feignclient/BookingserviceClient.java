package com.hsms.execution_service.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hsms.execution_service.config.FeignConfig;
import com.hsms.execution_service.model.BookingServiceResponseDTO;

@FeignClient(name = "booking-service", configuration = FeignConfig.class)
public interface BookingserviceClient {

	@GetMapping("/api/service-requests/{id}")
	BookingServiceResponseDTO getRequest(@PathVariable("id") Long id);

	@PutMapping("/api/service-requests/{id}/status")
	void updateStatus(@PathVariable Long id, @RequestParam String status);
}