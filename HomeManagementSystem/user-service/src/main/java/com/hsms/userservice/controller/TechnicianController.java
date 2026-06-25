package com.hsms.userservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
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

import com.hsms.userservice.model.TechnicianDetailResponseDTO;
import com.hsms.userservice.model.TechnicianProfileRequestDTO;
import com.hsms.userservice.security.CustomPrincipal;
import com.hsms.userservice.security.RoleValidator;
import com.hsms.userservice.security.Roles;
import com.hsms.userservice.security.SecurityUtils;
import com.hsms.userservice.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/technicians")
public class TechnicianController {

	private final UserService service;

	public TechnicianController(UserService service) {
		this.service = service;
	}

	@PreAuthorize("hasAnyAuthority('TECHNICIAN','ADMIN')")
	@PostMapping
	public ResponseEntity<TechnicianDetailResponseDTO> createTechnician(
			@Valid @RequestBody TechnicianProfileRequestDTO dto) {
		CustomPrincipal principal = SecurityUtils.getCurrentUser();
		String role = principal != null ? principal.getRole() : null;
		Long userId = principal != null ? principal.getUserId() : null;
		
		RoleValidator.validate(role, Roles.TECHNICIAN, Roles.ADMIN);

		if (!"ADMIN".equals(role) || dto.getUserId() == null) {
			dto.setUserId(userId);
		}

		return new ResponseEntity<>(service.createTechnician(dto), HttpStatus.CREATED);
	}

	@PreAuthorize("hasAnyAuthority('ADMIN','SERVICE_MANAGER') or #userId == authentication.principal.userId")
	@PutMapping("/{userId}")
	public ResponseEntity<TechnicianDetailResponseDTO> updateTechnician(@PathVariable Long userId,
			@Valid @RequestBody TechnicianProfileRequestDTO dto) {

		return ResponseEntity.ok(service.updateTechnician(userId, dto));
	}

	@PreAuthorize("hasAnyAuthority('ADMIN','SERVICE_MANAGER') or #userId == authentication.principal.userId")
	@GetMapping("/{userId}")
	public ResponseEntity<TechnicianDetailResponseDTO> getTechnician(@PathVariable Long userId) {

		return ResponseEntity.ok(service.getTechnician(userId));
	}

	@PreAuthorize("hasAnyAuthority('ADMIN','SERVICE_MANAGER')")
	@GetMapping
	public ResponseEntity<List<TechnicianDetailResponseDTO>> getAllTechnicians() {
		CustomPrincipal principal = SecurityUtils.getCurrentUser();
		String role = principal != null ? principal.getRole() : null;

		RoleValidator.validate(role, Roles.ADMIN, Roles.SERVICE_MANAGER);

		return ResponseEntity.ok(service.getAllTechnicians());
	}

	@PreAuthorize("hasAuthority('ADMIN')")
	@DeleteMapping("/{userId}")
	public ResponseEntity<String> deleteTechnician(@PathVariable Long userId) {

		service.deleteTechnician(userId);

		return ResponseEntity.ok("Technician Deleted Successfully");
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/technicianId/{technicianId}")
	public ResponseEntity<TechnicianDetailResponseDTO> getTechnicianById(@PathVariable Long technicianId) {

		return ResponseEntity.ok(service.getTechnicianById(technicianId));
	}

	@PreAuthorize("hasAnyAuthority('ADMIN','SERVICE_MANAGER','CUSTOMER')")
	@PutMapping("/technicianId/{technicianId}/rating")
	public ResponseEntity<TechnicianDetailResponseDTO> updateTechnicianRating(
			@PathVariable Long technicianId,
			@RequestParam Double rating) {
		return ResponseEntity.ok(service.updateTechnicianRating(technicianId, rating));
	}
}