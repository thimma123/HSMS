package com.hsms.assignmentservice.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hsms.assignmentservice.entity.Assignment;
import com.hsms.assignmentservice.entity.AssignmentAudit;
import com.hsms.assignmentservice.entity.AssignmentStatus;
import com.hsms.assignmentservice.event.AssignmentEvent;
import com.hsms.assignmentservice.exception.AssignmentNotFoundException;
import com.hsms.assignmentservice.exception.DuplicateAssignmentException;
import com.hsms.assignmentservice.exception.TechnicianNotAvailableException;
import com.hsms.assignmentservice.exception.UnauthorizedActionException;
import com.hsms.assignmentservice.feignclient.BookingServiceClient;
import com.hsms.assignmentservice.feignclient.CategoryClient;
import com.hsms.assignmentservice.feignclient.TechnicianClient;
import com.hsms.assignmentservice.feignclient.UserServiceClient;
import com.hsms.assignmentservice.model.AssignmentDetailResponseDTO;
import com.hsms.assignmentservice.model.AssignmentRequestDTO;
import com.hsms.assignmentservice.model.AssignmentResponseDTO;
import com.hsms.assignmentservice.model.CategoryResponseDTO;
import com.hsms.assignmentservice.model.ServiceRequestDTO;
import com.hsms.assignmentservice.model.TechnicianDetailResponseDTO;
import com.hsms.assignmentservice.model.UserDTO;
import com.hsms.assignmentservice.repository.AssignmentAuditRepository;
import com.hsms.assignmentservice.repository.AssignmentRepository;
import com.hsms.assignmentservice.security.CustomPrincipal;
import com.hsms.assignmentservice.security.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

	private final AssignmentRepository assignmentRepository;
	private final AssignmentAuditRepository assignmentAuditRepository;
	private final ModelMapper modelMapper;
	private final TechnicianClient technicianClient;
	private final BookingServiceClient bookingserviceClient;
	private final UserServiceClient userServiceClient;
	private final CategoryClient categoryClient;
	private final ApplicationEventPublisher eventPublisher;

	@Override
	public AssignmentResponseDTO assignTechnician(AssignmentRequestDTO requestDTO) {
		requireRole("SERVICE_MANAGER");

		assignmentRepository.findByServiceRequestId(requestDTO.getServiceRequestId()).ifPresent(existing -> {
			throw new DuplicateAssignmentException(
					"Assignment already exists for service request id: " + requestDTO.getServiceRequestId());
		});

		// Validate technician
		ResponseEntity<TechnicianDetailResponseDTO> techResponse = technicianClient
				.getTechnicianById(requestDTO.getTechnicianId());
		if (!techResponse.getStatusCode().is2xxSuccessful() || techResponse.getBody() == null) {
			throw new TechnicianNotAvailableException("Technician not found");
		}
		
		TechnicianDetailResponseDTO technician = techResponse.getBody();
		if (!"Available".equalsIgnoreCase(technician.getAvailabilityStatus())) {
			throw new TechnicianNotAvailableException("Technician is not available");
		}

		// Validate service request
		ResponseEntity<ServiceRequestDTO> srResponse = bookingserviceClient
				.getServiceRequestById(requestDTO.getServiceRequestId());
		if (!srResponse.getStatusCode().is2xxSuccessful() || srResponse.getBody() == null) {
			throw new RuntimeException("Service request not found");
		}
		ServiceRequestDTO serviceRequest = srResponse.getBody();
		if (!"CREATED".equalsIgnoreCase(serviceRequest.getStatus())) {
			throw new RuntimeException("Only CREATED requests can be assigned");
		}

		// Retrieve category details to match skill
		ResponseEntity<CategoryResponseDTO> catResponse = categoryClient
				.getCategoryById(serviceRequest.getCategoryId());
		if (!catResponse.getStatusCode().is2xxSuccessful() || catResponse.getBody() == null) {
			throw new RuntimeException("Service request category not found");
		}
		CategoryResponseDTO category = catResponse.getBody();
		if (technician.getSkill() == null || !isSkillMatching(technician.getSkill(), category.getCategoryName())) {
			throw new TechnicianNotAvailableException("Technician skill does not match service category");
		}

		// Validate user
		if (requestDTO.getUserId() == null) {
			throw new IllegalArgumentException("User ID must be provided for assignment");
		}

		UserDTO user = userServiceClient.getUserById(requestDTO.getUserId());

		if (user == null) {
			throw new RuntimeException("User not found");
		}
		

		// Conflict check
		boolean conflict = assignmentRepository.existsByTechnicianIdAndStartTimeAndStatusIn(
				technician.getUserId(), requestDTO.getStartTime(),
				List.of(AssignmentStatus.ASSIGNED, AssignmentStatus.ACCEPTED));
		if (conflict) {
			throw new TechnicianNotAvailableException("Technician already has a job at this time");
		}

		// Save assignment
		Assignment assignment = new Assignment();
		assignment.setTechnicianId(technician.getUserId());
		assignment.setServiceRequestId(requestDTO.getServiceRequestId());
		assignment.setAssignedDate(LocalDateTime.now());
		assignment.setStartTime(requestDTO.getStartTime());
		assignment.setStatus(AssignmentStatus.ASSIGNED);

		Assignment saved = assignmentRepository.save(assignment);

		// Update Booking Service status
		bookingserviceClient.updateStatus(requestDTO.getServiceRequestId(), "ASSIGNED", technician.getUserId());

		// Audit Log
		CustomPrincipal currentUser = SecurityUtils.getCurrentUser();
		Long actionBy = currentUser != null ? currentUser.getUserId() : null;
		AssignmentAudit audit = AssignmentAudit.builder()
				.assignmentId(saved.getId())
				.serviceRequestId(saved.getServiceRequestId())
				.technicianId(saved.getTechnicianId())
				.assignedBy(actionBy)
				.auditTimestamp(LocalDateTime.now())
				.previousStatus(null)
				.newStatus("ASSIGNED")
				.build();
		assignmentAuditRepository.save(audit);

		// Publish Event
		eventPublisher.publishEvent(AssignmentEvent.builder()
				.userId(technician.getUserId())
				.message("You have been assigned a new service request ID: " + requestDTO.getServiceRequestId())
				.eventType("TECHNICIAN_ASSIGNED")
				.build());

		return modelMapper.map(saved, AssignmentResponseDTO.class);
	}

	@Override
	public AssignmentResponseDTO acceptJob(Long assignmentId) {
		requireRole("TECHNICIAN");

		Assignment assignment = assignmentRepository.findById(assignmentId)
				.orElseThrow(() -> new AssignmentNotFoundException("Assignment not found: " + assignmentId));

		// Check that the logged-in technician is indeed the assigned one
		CustomPrincipal currentUser = SecurityUtils.getCurrentUser();
		if (currentUser != null) {
			Long currentUserId = currentUser.getUserId();
			Long assignedId = assignment.getTechnicianId();
			if (!currentUserId.equals(assignedId)) {
				boolean authorized = false;
				try {
					ResponseEntity<TechnicianDetailResponseDTO> techResponse = technicianClient.getTechnicianById(assignedId);
					if (techResponse.getStatusCode().is2xxSuccessful() && techResponse.getBody() != null) {
						Long techUserId = techResponse.getBody().getUserId();
						if (currentUserId.equals(techUserId)) {
							authorized = true;
						}
					}
				} catch (Exception e) {
					log.warn("Failed to check legacy technician profile in acceptJob: {}", e.getMessage());
				}
				if (!authorized) {
					throw new UnauthorizedActionException("You are not authorized to accept this job");
				}
			}
		}

		assignment.setStatus(AssignmentStatus.ACCEPTED);
		Assignment updated = assignmentRepository.save(assignment);

		bookingserviceClient.updateStatus(assignment.getServiceRequestId(), "ACCEPTED", null);

		// Audit Log
		Long actionBy = currentUser != null ? currentUser.getUserId() : null;
		AssignmentAudit audit = AssignmentAudit.builder()
				.assignmentId(updated.getId())
				.serviceRequestId(updated.getServiceRequestId())
				.technicianId(updated.getTechnicianId())
				.assignedBy(actionBy)
				.auditTimestamp(LocalDateTime.now())
				.previousStatus("ASSIGNED")
				.newStatus("ACCEPTED")
				.build();
		assignmentAuditRepository.save(audit);

		// Publish Event
		eventPublisher.publishEvent(AssignmentEvent.builder()
				.userId(assignment.getTechnicianId())
				.message("You accepted the job assignment for request ID: " + assignment.getServiceRequestId())
				.eventType("ASSIGNMENT_ACCEPTED")
				.build());

		return modelMapper.map(updated, AssignmentResponseDTO.class);
	}

	@Override
	public AssignmentResponseDTO rejectJob(Long assignmentId, String rejectionReason) {
		requireRole("TECHNICIAN");

		Assignment assignment = assignmentRepository.findById(assignmentId)
				.orElseThrow(() -> new AssignmentNotFoundException("Assignment not found: " + assignmentId));

		// Check that the logged-in technician is indeed the assigned one
		CustomPrincipal currentUser = SecurityUtils.getCurrentUser();
		if (currentUser != null) {
			Long currentUserId = currentUser.getUserId();
			Long assignedId = assignment.getTechnicianId();
			if (!currentUserId.equals(assignedId)) {
				boolean authorized = false;
				try {
					ResponseEntity<TechnicianDetailResponseDTO> techResponse = technicianClient.getTechnicianById(assignedId);
					if (techResponse.getStatusCode().is2xxSuccessful() && techResponse.getBody() != null) {
						Long techUserId = techResponse.getBody().getUserId();
						if (currentUserId.equals(techUserId)) {
							authorized = true;
						}
					}
				} catch (Exception e) {
					log.warn("Failed to check legacy technician profile in rejectJob: {}", e.getMessage());
				}
				if (!authorized) {
					throw new UnauthorizedActionException("You are not authorized to reject this job");
				}
			}
		}

		assignment.setStatus(AssignmentStatus.REJECTED);
		Assignment updated = assignmentRepository.save(assignment);

		bookingserviceClient.updateStatus(assignment.getServiceRequestId(), "REJECTED", null);

		// Audit Log
		Long actionBy = currentUser != null ? currentUser.getUserId() : null;
		AssignmentAudit audit = AssignmentAudit.builder()
				.assignmentId(updated.getId())
				.serviceRequestId(updated.getServiceRequestId())
				.technicianId(updated.getTechnicianId())
				.assignedBy(actionBy)
				.auditTimestamp(LocalDateTime.now())
				.previousStatus("ASSIGNED")
				.newStatus("REJECTED")
				.rejectionReason(rejectionReason)
				.build();
		assignmentAuditRepository.save(audit);

		// Publish Event
		eventPublisher.publishEvent(AssignmentEvent.builder()
				.userId(assignment.getTechnicianId())
				.message("You rejected the job assignment for request ID: " + assignment.getServiceRequestId() + ". Reason: " + rejectionReason)
				.eventType("ASSIGNMENT_REJECTED")
				.build());

		triggerReassignmentInternal(updated, rejectionReason);

		return modelMapper.map(updated, AssignmentResponseDTO.class);
	}

	@Override
	public AssignmentResponseDTO reassignTechnician(Long assignmentId, Long technicianId, LocalDateTime startTime) {
		requireRole("SERVICE_MANAGER");

		Assignment assignment = assignmentRepository.findById(assignmentId)
				.orElseThrow(() -> new AssignmentNotFoundException("Assignment not found: " + assignmentId));

		if (assignment.getStatus() != AssignmentStatus.REJECTED) {
			throw new RuntimeException("Only REJECTED jobs can be reassigned");
		}

		ResponseEntity<TechnicianDetailResponseDTO> techResponse = technicianClient.getTechnicianById(technicianId);
		if (!techResponse.getStatusCode().is2xxSuccessful() || techResponse.getBody() == null) {
			throw new TechnicianNotAvailableException("Technician not found");
		}
		TechnicianDetailResponseDTO technician = techResponse.getBody();
		if (!"Available".equalsIgnoreCase(technician.getAvailabilityStatus())) {
			throw new TechnicianNotAvailableException("Technician is not available");
		}

		boolean conflict = assignmentRepository.existsByTechnicianIdAndStartTimeAndStatusIn(technician.getUserId(), startTime,
				List.of(AssignmentStatus.ASSIGNED, AssignmentStatus.ACCEPTED));
		if (conflict) {
			throw new TechnicianNotAvailableException("Technician already has a job at this time");
		}

		assignment.setTechnicianId(technician.getUserId());
		assignment.setStartTime(startTime);
		assignment.setStatus(AssignmentStatus.REASSIGNED);
		assignment.setAssignedDate(LocalDateTime.now());

		Assignment updated = assignmentRepository.save(assignment);

		bookingserviceClient.updateStatus(assignment.getServiceRequestId(), "REASSIGNED", technician.getUserId());

		// Audit Log
		CustomPrincipal currentUser = SecurityUtils.getCurrentUser();
		Long actionBy = currentUser != null ? currentUser.getUserId() : null;
		AssignmentAudit audit = AssignmentAudit.builder()
				.assignmentId(updated.getId())
				.serviceRequestId(updated.getServiceRequestId())
				.technicianId(updated.getTechnicianId())
				.assignedBy(actionBy)
				.auditTimestamp(LocalDateTime.now())
				.previousStatus("REJECTED")
				.newStatus("REASSIGNED")
				.build();
		assignmentAuditRepository.save(audit);

		// Publish Event
		eventPublisher.publishEvent(AssignmentEvent.builder()
				.userId(technician.getUserId())
				.message("Assignment " + assignmentId + " has been reassigned to you for request ID: " + assignment.getServiceRequestId())
				.eventType("TECHNICIAN_ASSIGNED")
				.build());

		return modelMapper.map(updated, AssignmentResponseDTO.class);
	}

	@Override
	public List<AssignmentDetailResponseDTO> getAllAssignments() {
		return assignmentRepository.findAll().stream().map(a -> {
			AssignmentDetailResponseDTO dto = modelMapper.map(a, AssignmentDetailResponseDTO.class);
			dto.setTechnician_Id(a.getTechnicianId());
			try {
				ResponseEntity<TechnicianDetailResponseDTO> techResponse = technicianClient
						.getTechnicianById(a.getTechnicianId());
				if (techResponse.getStatusCode().is2xxSuccessful() && techResponse.getBody() != null) {
					TechnicianDetailResponseDTO tech = techResponse.getBody();
					dto.setTechnician(tech);
					dto.setTechnician_Id(tech.getTechnicianId());
				}
			} catch (Exception e) {
				System.err.println("Failed to fetch technician details: " + e.getMessage());
			}
			return dto;
		}).toList();
	}

	@Override
	public void deleteAssignment(Long assignmentId) {
		requireRole("SERVICE_MANAGER");

		Assignment assignment = assignmentRepository.findById(assignmentId)
				.orElseThrow(() -> new AssignmentNotFoundException("Assignment not found: " + assignmentId));
		assignmentRepository.delete(assignment);

		bookingserviceClient.updateStatus(assignment.getServiceRequestId(), "CANCELLED", null);
	}

	@Override
	public AssignmentResponseDTO getAssignmentByServiceRequestId(Long serviceRequestId) {
		Assignment assignment = assignmentRepository.findByServiceRequestId(serviceRequestId)
				.orElseThrow(() -> new AssignmentNotFoundException(
						"Assignment not found for service request id: " + serviceRequestId));
		return modelMapper.map(assignment, AssignmentResponseDTO.class);
	}

	private void requireRole(String... allowedRoles) {
		CustomPrincipal user = SecurityUtils.getCurrentUser();
		if (user == null) {
			throw new UnauthorizedActionException("No authenticated user session");
		}
		String role = user.getRole();
		if (role == null || Arrays.stream(allowedRoles).noneMatch(r -> r.equalsIgnoreCase(role))) {
			throw new UnauthorizedActionException("Access denied for role: " + role);
		}
	}

	@Override
	public List<AssignmentResponseDTO> getAssignmentsByTechnician(Long technicianId) {
		CustomPrincipal user = SecurityUtils.getCurrentUser();
		if (user != null) {
			String role = user.getRole();
			Long currentUserId = user.getUserId();
			if ("TECHNICIAN".equalsIgnoreCase(role)) {
				if (!currentUserId.equals(technicianId)) {
					throw new UnauthorizedActionException("Technicians can only view their own assigned jobs");
				}
			}
		}

		Long techUserId = technicianId;
		Long techProfileId = null;
		try {
			ResponseEntity<TechnicianDetailResponseDTO> techResponse = technicianClient.getTechnicianById(technicianId);
			if (techResponse.getStatusCode().is2xxSuccessful() && techResponse.getBody() != null) {
				techUserId = techResponse.getBody().getUserId();
				techProfileId = techResponse.getBody().getTechnicianId();
			}
		} catch (Exception e) {
			log.warn("Failed to resolve technician profile in getAssignmentsByTechnician: {}", e.getMessage());
		}

		List<Assignment> assignments = new java.util.ArrayList<>(assignmentRepository.findByTechnicianId(techUserId));
		if (techProfileId != null && !techProfileId.equals(techUserId)) {
			List<Assignment> byProfile = assignmentRepository.findByTechnicianId(techProfileId);
			for (Assignment a : byProfile) {
				if (assignments.stream().noneMatch(existing -> existing.getId().equals(a.getId()))) {
					assignments.add(a);
				}
			}
		}

		return assignments.stream()
				.map(a -> modelMapper.map(a, AssignmentResponseDTO.class)).toList();
	}

	private void triggerReassignmentInternal(Assignment assignment, String rejectionReason) {
		try {
			ResponseEntity<ServiceRequestDTO> srResponse = bookingserviceClient
					.getServiceRequestById(assignment.getServiceRequestId());
			if (srResponse == null || srResponse.getBody() == null)
				return;
			ServiceRequestDTO sr = srResponse.getBody();

			ResponseEntity<CategoryResponseDTO> catResponse = categoryClient.getCategoryById(sr.getCategoryId());
			if (catResponse == null || catResponse.getBody() == null)
				return;
			CategoryResponseDTO category = catResponse.getBody();

			ResponseEntity<List<TechnicianDetailResponseDTO>> techsResponse = technicianClient.getAllTechnicians();
			if (techsResponse == null || techsResponse.getBody() == null)
				return;
			List<TechnicianDetailResponseDTO> technicians = techsResponse.getBody();

			Long currentTechId = assignment.getTechnicianId();
			TechnicianDetailResponseDTO candidate = technicians.stream()
					.filter(t -> t.getUserId() != null && !t.getUserId().equals(currentTechId))
					.filter(t -> "Available".equalsIgnoreCase(t.getAvailabilityStatus()))
					.filter(t -> t.getSkill() != null && t.getSkill().equalsIgnoreCase(category.getCategoryName()))
					.filter(t -> {
						return !assignmentRepository.existsByTechnicianIdAndStartTimeAndStatusIn(t.getUserId(),
								assignment.getStartTime(),
								List.of(AssignmentStatus.ASSIGNED, AssignmentStatus.ACCEPTED));
					}).findFirst().orElse(null);

			if (candidate != null) {
				assignment.setTechnicianId(candidate.getUserId());
				assignment.setStatus(AssignmentStatus.ASSIGNED);
				assignment.setAssignedDate(LocalDateTime.now());
				assignmentRepository.save(assignment);

				bookingserviceClient.updateStatus(assignment.getServiceRequestId(), "ASSIGNED", candidate.getUserId());

				// Audit
				AssignmentAudit audit = AssignmentAudit.builder()
						.assignmentId(assignment.getId())
						.serviceRequestId(assignment.getServiceRequestId())
						.technicianId(candidate.getUserId())
						.assignedBy(null) // system automatic reassignment
						.auditTimestamp(LocalDateTime.now())
						.previousStatus("REJECTED")
						.newStatus("ASSIGNED")
						.rejectionReason("Auto-reassigned due to rejection: " + rejectionReason)
						.build();
				assignmentAuditRepository.save(audit);

				// Publish Event
				eventPublisher.publishEvent(AssignmentEvent.builder()
						.userId(candidate.getUserId())
						.message("You have been auto-assigned a rejected service request ID: " + assignment.getServiceRequestId())
						.eventType("TECHNICIAN_ASSIGNED")
						.build());
			}
		} catch (Exception e) {
			System.err.println("Automatic reassignment failed: " + e.getMessage());
		}
	}

	private boolean isSkillMatching(String skill, String categoryName) {
		if (skill == null || categoryName == null) {
			return false;
		}
		String s = skill.trim().toLowerCase();
		String c = categoryName.trim().toLowerCase();

		if (s.equals(c)) {
			return true;
		}

		if (s.length() >= 5 && c.length() >= 5) {
			if (s.substring(0, 5).equals(c.substring(0, 5))) {
				return true;
			}
		}

		return s.contains(c) || c.contains(s);
	}
}
