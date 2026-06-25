package com.hsms.execution_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.hsms.execution_service.model.ServiceRecordDetailResponseDTO;
import com.hsms.execution_service.model.ServiceRecordRequestDTO;
import com.hsms.execution_service.model.ServiceRecordResponseDTO;
import com.hsms.execution_service.service.ServiceRecordService;

@RestController
@RequestMapping("/api/records")
public class ServiceRecordController {

	@Autowired
	private ServiceRecordService service;

	@PreAuthorize("hasAuthority('TECHNICIAN')")
	@PostMapping("/start")
	public ResponseEntity<ServiceRecordResponseDTO> start(@RequestBody ServiceRecordRequestDTO dto) {
		return ResponseEntity.ok(service.start(dto));
	}

	@PreAuthorize("hasAuthority('TECHNICIAN')")
	@PutMapping("/complete/{id}")
	public ResponseEntity<ServiceRecordDetailResponseDTO> complete(@PathVariable Long id,
			@RequestBody ServiceRecordRequestDTO dto) {
		return ResponseEntity.ok(service.complete(id, dto));
	}

	@PreAuthorize("hasAnyAuthority('ADMIN','SERVICE_MANAGER','TECHNICIAN','CUSTOMER')")
	@GetMapping("/{id}")
	public ResponseEntity<ServiceRecordDetailResponseDTO> get(@PathVariable Long id) {
		return ResponseEntity.ok(service.get(id));
	}

	@PreAuthorize("hasAnyAuthority('ADMIN','SERVICE_MANAGER','TECHNICIAN','CUSTOMER')")
	@PutMapping("/{id}/payment-status")
	public ResponseEntity<ServiceRecordDetailResponseDTO> updatePaymentStatus(
			@PathVariable Long id,
			@RequestParam("status") String status,
			@RequestParam(value = "method", required = false) String method) {
		return ResponseEntity.ok(service.updatePaymentStatus(id, status, method));
	}
}