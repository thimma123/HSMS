package com.hsms.assignmentservice.feignclient;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.hsms.assignmentservice.model.TechnicianDetailResponseDTO;


@FeignClient(name = "user-service")
public interface TechnicianClient {

	@GetMapping("/api/technicians/technicianId/{technicianId}")
	ResponseEntity<TechnicianDetailResponseDTO> getTechnicianById(@PathVariable("technicianId") Long technicianId);

	@GetMapping("/api/technicians")
	ResponseEntity<List<TechnicianDetailResponseDTO>> getAllTechnicians();
}