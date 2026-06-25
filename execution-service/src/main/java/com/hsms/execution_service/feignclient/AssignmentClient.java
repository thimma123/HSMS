package com.hsms.execution_service.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.hsms.execution_service.config.FeignConfig;
import com.hsms.execution_service.model.AssignmentResponseDTO;

@FeignClient(
    name = "assignment-service",
    configuration = FeignConfig.class
)
public interface AssignmentClient {

    @GetMapping("/api/assignments/service-request/{serviceRequestId}")
    AssignmentResponseDTO getByServiceRequestId(
            @PathVariable("serviceRequestId") Long serviceRequestId);
}