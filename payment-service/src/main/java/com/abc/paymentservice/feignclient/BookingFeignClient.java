package com.abc.paymentservice.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.abc.paymentservice.model.BookingResponseDTO;

@FeignClient(name="booking-service")
public interface BookingFeignClient {
	
	@GetMapping("/api/service-requests/{requestId}")
	BookingResponseDTO getBookingById(@PathVariable("requestId") Long requestId);
	
	@PutMapping("/api/service-requests/{requestId}/status")
	void updateStatus(@PathVariable("requestId") Long requestId, @RequestParam("status") String status);
}