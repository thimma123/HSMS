package com.hsms.assignmentservice.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hsms.assignmentservice.model.ServiceRequestDTO;

@FeignClient(name = "booking-service")
public interface BookingServiceClient {
    @GetMapping("/api/service-requests/{id}")
    ResponseEntity<ServiceRequestDTO> getServiceRequestById(@PathVariable("id") Long id);
    @PutMapping("/api/service-requests/{id}/status")
    void updateStatus(@PathVariable("id") Long id, @RequestParam("status") String status, @RequestParam(value = "technicianId", required = false) Long technicianId);
}
