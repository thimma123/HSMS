package com.hsms.assignmentservice.controller;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hsms.assignmentservice.model.AssignmentRequestDTO;
import com.hsms.assignmentservice.model.AssignmentResponseDTO;
import com.hsms.assignmentservice.service.AssignmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.hsms.assignmentservice.security.CustomPrincipal;
import com.hsms.assignmentservice.security.SecurityUtils;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

	private final AssignmentService assignmentService;

	@PreAuthorize("hasAuthority('SERVICE_MANAGER')")
	@PostMapping
	public ResponseEntity<AssignmentResponseDTO> assignTechnician(@Valid @RequestBody AssignmentRequestDTO requestDTO) {
		CustomPrincipal currentUser = SecurityUtils.getCurrentUser();
		if (currentUser != null) {
			requestDTO.setUserId(currentUser.getUserId());
		}
		return ResponseEntity.ok(assignmentService.assignTechnician(requestDTO));
	}

	@PreAuthorize("hasAuthority('TECHNICIAN')")
	@PutMapping("/{id}/accept")
	public ResponseEntity<AssignmentResponseDTO> acceptJob(@PathVariable Long id) {
		return ResponseEntity.ok(assignmentService.acceptJob(id));
	}

	@PreAuthorize("hasAuthority('TECHNICIAN')")
	@PutMapping("/{id}/reject")
	public ResponseEntity<AssignmentResponseDTO> rejectJob(@PathVariable Long id,
			@RequestParam(value = "reason", required = false, defaultValue = "No reason provided") String reason) {
		return ResponseEntity.ok(assignmentService.rejectJob(id, reason));
	}

	@PreAuthorize("hasAuthority('SERVICE_MANAGER')")
	@PutMapping("/{id}/reassign")
	public ResponseEntity<AssignmentResponseDTO> reassignTechnician(@PathVariable Long id,
			@RequestParam Long technicianId, @RequestParam LocalDateTime startTime) {
		return ResponseEntity.ok(assignmentService.reassignTechnician(id, technicianId, startTime));
	}

	@PreAuthorize("hasAuthority('SERVICE_MANAGER')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
		assignmentService.deleteAssignment(id);
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("hasAnyAuthority('ADMIN','SERVICE_MANAGER')")
	@GetMapping
	public ResponseEntity<?> getAllAssignments() {
		return ResponseEntity.ok(assignmentService.getAllAssignments());
	}

	@PreAuthorize("hasAnyAuthority('ADMIN','SERVICE_MANAGER','TECHNICIAN','CUSTOMER')")
	@GetMapping("/service-request/{serviceRequestId}")
	public ResponseEntity<AssignmentResponseDTO> getAssignmentByServiceRequestId(@PathVariable Long serviceRequestId) {
		return ResponseEntity.ok(assignmentService.getAssignmentByServiceRequestId(serviceRequestId));
	}

	@PreAuthorize("hasAnyAuthority('ADMIN','SERVICE_MANAGER','TECHNICIAN')")
	@GetMapping("/technician/{technicianId}")
	public ResponseEntity<?> getAssignmentsByTechnician(@PathVariable Long technicianId) {
		return ResponseEntity.ok(assignmentService.getAssignmentsByTechnician(technicianId));
	}
}
