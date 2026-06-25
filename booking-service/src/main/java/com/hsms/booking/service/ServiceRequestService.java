package com.hsms.booking.service;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hsms.booking.client.CatalogClient;
import com.hsms.booking.client.CategoryDTO;
import com.hsms.booking.client.CustomerDTO;
import com.hsms.booking.client.TechnicianClient;
import com.hsms.booking.client.TechnicianDTO;
import com.hsms.booking.client.UserClient;
import com.hsms.booking.dto.ServiceEvent;
import com.hsms.booking.dto.ServiceRequestDTO;
import com.hsms.booking.dto.ServiceRequestResponse;
import com.hsms.booking.entity.ServiceRequest;
import com.hsms.booking.enums.ServiceRequestStatus;
import com.hsms.booking.exception.InvalidOperationException;
import com.hsms.booking.exception.ResourceNotFoundException;
import com.hsms.booking.exception.ServiceRequestNotFoundException;
import com.hsms.booking.repository.ServiceRequestRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing service requests
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceRequestService {

	private final ServiceRequestRepository serviceRequestRepository;
	private final UserClient userClient;
	private final CatalogClient catalogClient;
	private final TechnicianClient technicianClient;
	private final ApplicationEventPublisher eventPublisher;

	/**
	 * Create a new service request
	 */
	@Transactional
	public ServiceRequestResponse createServiceRequest(ServiceRequestDTO request, Long customerId) {
		log.info("Creating new service request for customer ID: {}", customerId);

		// ✅ Mandatory validation:

		// 1. Validate customer exists
		CustomerDTO customer = userClient.getCustomerById(customerId);
		if (customer == null) {
			throw new ResourceNotFoundException("Customer not found with ID: " + customerId);
		}

		// 2. Validate category exists and is active
		CategoryDTO category = catalogClient.getCategoryById(request.getCategoryId());
		if (category == null) {
			throw new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId());
		}
		if (category.getIsActive() == null || !category.getIsActive()) {
			throw new InvalidOperationException("Service category " + request.getCategoryId() + " is not available");
		}

		// 3. Business Validations
		if (request.getScheduledDateTime() == null) {
			throw new InvalidOperationException("Scheduled date time is required");
		}

		if (request.getScheduledDateTime().isBefore(java.time.LocalDateTime.now())) {
			throw new InvalidOperationException("Cannot schedule service in the past");
		}

		if (request.getScheduledDateTime().isAfter(java.time.LocalDateTime.now().plusMonths(6))) {
			throw new InvalidOperationException("Cannot schedule service more than 6 months in advance");
		}

		// 4. Check for duplicate bookings (same customer, same category, similar time)
		java.util.List<ServiceRequest> recentRequests = serviceRequestRepository
				.findByCustomerIdAndCategoryIdAndScheduledDateTimeBetween(customerId, request.getCategoryId(),
						request.getScheduledDateTime().minusHours(2), request.getScheduledDateTime().plusHours(2));

		if (!recentRequests.isEmpty()) {
			throw new InvalidOperationException("You already have a similar booking around this time");
		}

		// Create service request
		ServiceRequest serviceRequest = ServiceRequest.builder().customerId(customerId)
				.categoryId(request.getCategoryId()).status(ServiceRequestStatus.CREATED).address(request.getAddress())
				.city(request.getCity()).pincode(request.getPincode()).scheduledDateTime(request.getScheduledDateTime())
				.description(request.getDescription()).build();

		ServiceRequest savedRequest = serviceRequestRepository.save(serviceRequest);
		log.info("Service request created successfully with ID: {}", savedRequest.getRequestId());

		// Publish event
		ServiceEvent event = ServiceEvent.builder()
				.eventType("REQUEST_CREATED")
				.requestId(savedRequest.getRequestId())
				.customerId(customerId)
				.message("Your service request for " + category.getCategoryName() + " has been created successfully.")
				.build();
		eventPublisher.publishEvent(event);
		log.info("Event published for request: {}", savedRequest.getRequestId());

		return convertToResponse(savedRequest, category, null);
	}

	/**
	 * Get all service requests for a customer with pagination
	 */
	@Transactional(readOnly = true)
	public Page<ServiceRequestResponse> getMyRequests(Long customerId, Pageable pageable) {
		log.debug("Fetching service requests for customer ID: {} with pagination", customerId);

		Page<ServiceRequest> requests = serviceRequestRepository.findByCustomerId(customerId, pageable);
		log.debug("Found {} service requests for customer {}", requests.getTotalElements(), customerId);

		return requests.map(this::convertToResponse);
	}

	/**
	 * Get all service requests with pagination
	 */
	@Transactional(readOnly = true)
	public Page<ServiceRequestResponse> getAllRequests(Pageable pageable) {
		log.debug("Fetching all service requests with pagination");

		Page<ServiceRequest> requests = serviceRequestRepository.findAll(pageable);
		log.debug("Found {} total service requests", requests.getTotalElements());

		return requests.map(this::convertToResponse);
	}

	/**
	 * Get service request by ID
	 */
	@Transactional(readOnly = true)
	public ServiceRequestResponse getRequestById(Long requestId) {
		log.debug("Fetching service request with ID: {}", requestId);

		ServiceRequest request = serviceRequestRepository.findById(requestId).orElseThrow(() -> {
			log.warn("Service request not found with ID: {}", requestId);
			return new ServiceRequestNotFoundException("Service request not found with ID: " + requestId);
		});

		return convertToResponse(request);
	}

	/**
	 * Cancel a service request
	 */
	@Transactional
	public ServiceRequestResponse cancelRequest(Long requestId, Long customerId) {
		log.info("Cancelling service request ID: {} for customer ID: {}", requestId, customerId);

		ServiceRequest request = serviceRequestRepository.findById(requestId).orElseThrow(() -> {
			log.warn("Service request not found with ID: {}", requestId);
			return new ServiceRequestNotFoundException("Service request not found with ID: " + requestId);
		});

		// Validate request belongs to customer
		if (!request.getCustomerId().equals(customerId)) {
			log.warn("Service request {} does not belong to customer {}", requestId, customerId);
			throw new InvalidOperationException("Service request does not belong to this customer");
		}

		// Validate status
		ServiceRequestStatus currentStatus = request.getStatus();
		if (currentStatus != ServiceRequestStatus.CREATED && currentStatus != ServiceRequestStatus.ASSIGNED) {
			log.warn("Cannot cancel service request {} with status: {}", requestId, currentStatus);
			throw new InvalidOperationException("Cannot cancel service request with status: " + currentStatus);
		}

		request.setStatus(ServiceRequestStatus.CANCELLED);
		ServiceRequest cancelledRequest = serviceRequestRepository.save(request);
		log.info("Service request {} cancelled successfully", requestId);

		// Publish cancel event
		ServiceEvent event = ServiceEvent.builder().eventType("REQUEST_CANCELLED")
				.requestId(cancelledRequest.getRequestId()).customerId(cancelledRequest.getCustomerId())
				.technicianId(cancelledRequest.getTechnicianId()).message("Service request cancelled").build();
		eventPublisher.publishEvent(event);
		log.info("Event published for request cancellation: {}", cancelledRequest.getRequestId());

		return convertToResponse(cancelledRequest);
	}

	/**
	 * Get service requests by status
	 */
	@Transactional(readOnly = true)
	public Page<ServiceRequestResponse> getRequestsByStatus(ServiceRequestStatus status, Pageable pageable) {
		log.debug("Fetching service requests with status: {}", status);

		Page<ServiceRequest> requests = serviceRequestRepository.findByStatus(status, pageable);
		log.debug("Found {} service requests with status {}", requests.getTotalElements(), status);

		return requests.map(this::convertToResponse);
	}

	/**
	 * Get service requests for a specific technician
	 */
	@Transactional(readOnly = true)
	public Page<ServiceRequestResponse> getRequestsByTechnician(Long technicianId, Pageable pageable) {
		log.debug("Fetching service requests assigned to technician ID: {}", technicianId);

		Page<ServiceRequest> requests = serviceRequestRepository.findByTechnicianId(technicianId, pageable);
		log.debug("Found {} service requests for technician {}", requests.getTotalElements(), technicianId);

		return requests.map(this::convertToResponse);
	}

	/**
	 * Get service requests by category
	 */
	@Transactional(readOnly = true)
	public Page<ServiceRequestResponse> getRequestsByCategory(Long categoryId, Pageable pageable) {
		log.debug("Fetching service requests for category ID: {}", categoryId);

		Page<ServiceRequest> requests = serviceRequestRepository.findByCategoryId(categoryId, pageable);
		log.debug("Found {} service requests for category {}", requests.getTotalElements(), categoryId);

		return requests.map(this::convertToResponse);
	}

	/**
	 * Convert ServiceRequest entity to ServiceRequestResponse DTO
	 */
	private ServiceRequestResponse convertToResponse(ServiceRequest serviceRequest) {
		CategoryDTO category = catalogClient.getCategoryById(serviceRequest.getCategoryId());
		CustomerDTO customer = userClient.getCustomerById(serviceRequest.getCustomerId());

		ServiceRequestResponse response = new ServiceRequestResponse();

		return convertToResponse(serviceRequest, category, customer);
	}

	/**
	 * Convert ServiceRequest entity to ServiceRequestResponse DTO with provided
	 * data
	 */
	private ServiceRequestResponse convertToResponse(ServiceRequest serviceRequest, CategoryDTO category,
			CustomerDTO customer) {
		ServiceRequestResponse.ServiceRequestResponseBuilder builder = ServiceRequestResponse.builder()
				.requestId(serviceRequest.getRequestId()).status(serviceRequest.getStatus())
				.categoryId(serviceRequest.getCategoryId())
				.address(serviceRequest.getAddress()).city(serviceRequest.getCity())
				.pincode(serviceRequest.getPincode()).scheduledDateTime(serviceRequest.getScheduledDateTime())
				.description(serviceRequest.getDescription()).createdAt(serviceRequest.getCreatedAt())
				.updatedAt(serviceRequest.getUpdatedAt()).customerId(serviceRequest.getCustomerId())
				.technicianId(serviceRequest.getTechnicianId());

		if (category != null) {
			builder.categoryName(category.getCategoryName());
			builder.basePrice(category.getBasePrice());
		}

		if (customer != null) {
			builder.customerName(customer.getName());
		}

		if (serviceRequest.getTechnicianId() != null) {
			TechnicianDTO technician = technicianClient.getTechnicianById(serviceRequest.getTechnicianId());

			builder.technicianName(technician.getName());

			builder.technicianRating(technician.getRating());
		}

		return builder.build();
	}

	/**
	 * Update service request status (for Assignment & Execution services)
	 */
	@Transactional
	public ServiceRequestResponse updateStatus(Long requestId, String statusStr) {
		return updateStatus(requestId, statusStr, null);
	}

	@Transactional
	public ServiceRequestResponse updateStatus(Long requestId, String statusStr, Long technicianId) {
		log.info("Updating service request {} to status: {} with technician ID: {}", requestId, statusStr,
				technicianId);

		ServiceRequest request = serviceRequestRepository.findById(requestId).orElseThrow(() -> {
			log.warn("Service request not found with ID: {}", requestId);
			return new ServiceRequestNotFoundException("Service request not found with ID: " + requestId);
		});

		// Validate and convert status
		ServiceRequestStatus newStatus;
		try {
			newStatus = ServiceRequestStatus.valueOf(statusStr);
		} catch (IllegalArgumentException e) {
			throw new InvalidOperationException("Invalid status: " + statusStr);
		}

		request.setStatus(newStatus);
		if (technicianId != null) {
			request.setTechnicianId(technicianId);
		} else if (newStatus == ServiceRequestStatus.REJECTED || newStatus == ServiceRequestStatus.CANCELLED) {
			request.setTechnicianId(null);
		}
		ServiceRequest updated = serviceRequestRepository.save(request);
		log.info("Service request {} status updated to {}", requestId, newStatus);

		return convertToResponse(updated);
	}

	@Transactional
	public ServiceRequestResponse assignTechnician(Long requestId, Long technicianId) {

		ServiceRequest request = serviceRequestRepository.findById(requestId)
				.orElseThrow(() -> new ServiceRequestNotFoundException("Request Not Found"));

		if (request.getStatus() != ServiceRequestStatus.CREATED) {

			throw new InvalidOperationException("Only CREATED requests can be assigned");
		}

		technicianClient.getTechnicianById(technicianId);

		request.setTechnicianId(technicianId);

		request.setStatus(ServiceRequestStatus.ASSIGNED);

		ServiceRequest saved = serviceRequestRepository.save(request);

		return convertToResponse(saved);
	}

	/**
	 * Get all service requests summary (for Report service)
	 */
	@Transactional(readOnly = true)
	public List<com.hsms.booking.dto.ServiceRequestSummary> getAllSummary() {
		log.debug("Fetching all service requests summary");

		List<ServiceRequest> requests = serviceRequestRepository.findAll();

		return requests.stream().map(request -> {
			CategoryDTO category = null;
			try {
				category = catalogClient.getCategoryById(request.getCategoryId());
			} catch (Exception e) {
				log.warn("Could not fetch category for summary: {}", e.getMessage());
			}

			return com.hsms.booking.dto.ServiceRequestSummary.builder().requestId(request.getRequestId())
					.customerId(request.getCustomerId()).categoryId(request.getCategoryId())
					.technicianId(request.getTechnicianId()).status(request.getStatus().name())
					.createdAt(request.getCreatedAt()).basePrice(category != null ? category.getBasePrice() : null)
					.categoryName(category != null ? category.getCategoryName() : null)
					.scheduledDateTime(request.getScheduledDateTime())
					.city(request.getCity())
					.build();
		}).collect(java.util.stream.Collectors.toList());
	}

	@Transactional
	public ServiceRequestResponse updateServiceRequest(Long requestId, ServiceRequestDTO request, Long customerId) {
		log.info("Updating service request ID: {} for customer ID: {}", requestId, customerId);

		ServiceRequest serviceRequest = serviceRequestRepository.findById(requestId).orElseThrow(() -> {
			log.warn("Service request not found with ID: {}", requestId);
			return new ServiceRequestNotFoundException("Service request not found with ID: " + requestId);
		});

		// 1. Validate request belongs to customer
		if (!serviceRequest.getCustomerId().equals(customerId)) {
			throw new InvalidOperationException("Service request does not belong to this customer");
		}

		// 2. Validate status: can only update when status is in CREATED or ASSIGNED
		if (serviceRequest.getStatus() != ServiceRequestStatus.CREATED
				&& serviceRequest.getStatus() != ServiceRequestStatus.ASSIGNED) {
			throw new InvalidOperationException(
					"Cannot update service request with status: " + serviceRequest.getStatus());
		}

		// 3. Validation: Scheduled date time
		if (request.getScheduledDateTime() == null) {
			throw new InvalidOperationException("Scheduled date time is required");
		}

		if (request.getScheduledDateTime().isBefore(java.time.LocalDateTime.now())) {
			throw new InvalidOperationException("Cannot schedule service in the past");
		}

		if (request.getScheduledDateTime().isAfter(java.time.LocalDateTime.now().plusMonths(6))) {
			throw new InvalidOperationException("Cannot schedule service more than 6 months in advance");
		}

		// 4. Update fields
		serviceRequest.setAddress(request.getAddress());
		serviceRequest.setCity(request.getCity());
		serviceRequest.setPincode(request.getPincode());
		serviceRequest.setScheduledDateTime(request.getScheduledDateTime());
		serviceRequest.setDescription(request.getDescription());

		// Category update if changed
		if (!serviceRequest.getCategoryId().equals(request.getCategoryId())) {
			// Validate category exists and is active
			CategoryDTO category = catalogClient.getCategoryById(request.getCategoryId());
			if (category == null) {
				throw new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId());
			}
			if (category.getIsActive() == null || !category.getIsActive()) {
				throw new InvalidOperationException(
						"Service category " + request.getCategoryId() + " is not available");
			}
			serviceRequest.setCategoryId(request.getCategoryId());
		}

		ServiceRequest saved = serviceRequestRepository.save(serviceRequest);
		log.info("Service request {} updated successfully", requestId);

		return convertToResponse(saved);
	}

	@Transactional(readOnly = true)
	public boolean isOwnerOrAssigned(Long requestId, Long userId) {
		if (requestId == null || userId == null) {
			return false;
		}
		return serviceRequestRepository.findById(requestId)
				.map(req -> userId.equals(req.getCustomerId()) || userId.equals(req.getTechnicianId()))
				.orElse(false);
	}
}
