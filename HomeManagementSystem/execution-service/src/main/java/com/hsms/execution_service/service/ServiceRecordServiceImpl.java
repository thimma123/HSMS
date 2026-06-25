package com.hsms.execution_service.service;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.hsms.execution_service.entity.PaymentMethod;
import com.hsms.execution_service.entity.ServiceRecord;
import com.hsms.execution_service.event.ServiceCompletedEvent;
import com.hsms.execution_service.event.ServiceStartedEvent;
import com.hsms.execution_service.exception.InvalidStateException;
import com.hsms.execution_service.exception.ResourceNotFoundException;
import com.hsms.execution_service.feignclient.AssignmentClient;
import com.hsms.execution_service.feignclient.BookingserviceClient;
import com.hsms.execution_service.feignclient.PaymentClient;
import com.hsms.execution_service.model.AssignmentResponseDTO;
import com.hsms.execution_service.model.BookingServiceResponseDTO;
import com.hsms.execution_service.model.PaymentDetailResponseDTO;
import com.hsms.execution_service.model.PaymentRequestDTO;
import com.hsms.execution_service.model.PaymentResponseDTO;
import com.hsms.execution_service.model.ServiceRecordDetailResponseDTO;
import com.hsms.execution_service.model.ServiceRecordRequestDTO;
import com.hsms.execution_service.model.ServiceRecordResponseDTO;
import com.hsms.execution_service.repository.ServiceRecordRepository;
import com.hsms.execution_service.security.CustomPrincipal;
import com.hsms.execution_service.security.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceRecordServiceImpl implements ServiceRecordService {

	private final ServiceRecordRepository repo;
	private final BookingserviceClient requestClient;
	private final PaymentClient paymentClient;
	private final AssignmentClient assignmentClient;
	private final ModelMapper mapper;
	private final ApplicationEventPublisher eventPublisher;

	@Override
	public ServiceRecordResponseDTO start(ServiceRecordRequestDTO dto) {
		requireRole("TECHNICIAN");

		// Validate service request
		BookingServiceResponseDTO sr = requestClient.getRequest(dto.getServiceRequestId());
		if (sr == null) {
			throw new InvalidStateException("Booking response is null");
		}

		if (!"ACCEPTED".equalsIgnoreCase(sr.getStatus())) {
			throw new InvalidStateException("Booking status is [" + sr.getStatus() + "], expected ACCEPTED");
		}

		// Validate assignment
		AssignmentResponseDTO assignment = assignmentClient.getByServiceRequestId(dto.getServiceRequestId());
		if (assignment == null || !"ACCEPTED".equalsIgnoreCase(assignment.getStatus())) {
			throw new InvalidStateException("Only ACCEPTED assignments can be started");
		}

		// Validate caller technician ID matches assigned technician ID
		CustomPrincipal currentUser = SecurityUtils.getCurrentUser();
		if (currentUser == null || !currentUser.getUserId().equals(assignment.getTechnicianId())) {
			throw new InvalidStateException("Access denied: You are not the technician assigned to this request");
		}

		ServiceRecord record = new ServiceRecord();
		record.setServiceRequestId(dto.getServiceRequestId());
		record.setStartTime(LocalDateTime.now());
		record.setStatus("IN_PROGRESS");
		record.setActualCost(0.0);
		record.setTechnicianId(assignment.getTechnicianId());

		ServiceRecord saved = repo.save(record);

		// Update Booking Service status
		requestClient.updateStatus(dto.getServiceRequestId(), "IN_PROGRESS");

		// Publish local ServiceStartedEvent
		eventPublisher.publishEvent(ServiceStartedEvent.builder()
				.serviceRequestId(saved.getServiceRequestId())
				.customerId(sr.getCustomerId())
				.technicianId(saved.getTechnicianId())
				.build());

		ServiceRecordResponseDTO response = new ServiceRecordResponseDTO();
		response.setRecordId(saved.getServiceId());
		response.setServiceRequestId(saved.getServiceRequestId());
		response.setStartTime(saved.getStartTime());
		response.setStatus(saved.getStatus());

		return response;
	}

	@Override
	public ServiceRecordDetailResponseDTO complete(Long id, ServiceRecordRequestDTO dto) {
		requireRole("TECHNICIAN");

		ServiceRecord record = repo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Service record not found"));

		if (!"IN_PROGRESS".equalsIgnoreCase(record.getStatus())) {
			throw new InvalidStateException("Only IN_PROGRESS services can be completed");
		}

		// Validate caller technician ID matches assigned technician ID
		CustomPrincipal currentUser = SecurityUtils.getCurrentUser();
		if (currentUser == null || !currentUser.getUserId().equals(record.getTechnicianId())) {
			throw new InvalidStateException("Access denied: You are not the technician assigned to this request");
		}

		// Update Booking Service status to COMPLETED first (so payment service allows it)
		requestClient.updateStatus(record.getServiceRequestId(), "COMPLETED");

		record.setEndTime(LocalDateTime.now());
		record.setRemarks(dto.getRemarks());
		record.setActualCost(dto.getActualCost());
		record.setTechnicianNotes(dto.getTechnicianNotes());
		record.setExecutionNotes(dto.getExecutionNotes());
		record.setStatus("COMPLETED");

		ServiceRecord saved = repo.save(record);

		// Fetch service request detail to retrieve customerId
		var sr = requestClient.getRequest(record.getServiceRequestId());

		// Trigger payment
		PaymentRequestDTO payment = new PaymentRequestDTO();
		payment.setServiceRequestId(record.getServiceRequestId());
		payment.setAmount(record.getActualCost());
		payment.setPaymentMethod(dto.getPaymentMethod());

		try {
			PaymentResponseDTO paymentResponse = paymentClient.createPayment(payment);
			log.info("Payment triggered successfully. Response status: {}", paymentResponse.getStatus());
		} catch (Exception e) {
			log.error("Failed to automatically trigger payment: {}", e.getMessage());
		}

		// Publish local ServiceCompletedEvent
		eventPublisher.publishEvent(ServiceCompletedEvent.builder()
				.serviceRequestId(saved.getServiceRequestId())
				.customerId(sr != null ? sr.getCustomerId() : null)
				.technicianId(saved.getTechnicianId())
				.actualCost(saved.getActualCost())
				.build());

		ServiceRecordDetailResponseDTO response = mapper.map(saved, ServiceRecordDetailResponseDTO.class);
		response.setRecordId(saved.getServiceId());
		response.setPaymentMethod(dto.getPaymentMethod() != null ? dto.getPaymentMethod().name() : null);
		response.setPaymentStatus("PENDING");

		return response;
	}

	@Override
	public ServiceRecordDetailResponseDTO get(Long id) {
		ServiceRecord record = repo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Service record not found"));
		ServiceRecordDetailResponseDTO response = mapper.map(record, ServiceRecordDetailResponseDTO.class);
		response.setRecordId(record.getServiceId());
		
		try {
			var paymentDetail = paymentClient.getPaymentByServiceRequestId(record.getServiceRequestId());
			if (paymentDetail != null) {
				response.setPaymentMethod(paymentDetail.getPaymentMethod());
				response.setPaymentStatus(paymentDetail.getStatus());
			}
		} catch (Exception e) {
			log.error("Failed to fetch payment details for service request {}: {}", record.getServiceRequestId(), e.getMessage());
		}
		
		return response;
	}

	@Override
	public ServiceRecordDetailResponseDTO updatePaymentStatus(Long id, String status, String method) {
		ServiceRecord record = repo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Service record not found"));

		// 1. Check if payment exists
		PaymentDetailResponseDTO paymentDetail = null;
		try {
			paymentDetail = paymentClient.getPaymentByServiceRequestId(record.getServiceRequestId());
		} catch (Exception e) {
			log.info("Payment record not found for service request {}, will attempt to create: {}", record.getServiceRequestId(), e.getMessage());
		}

		PaymentDetailResponseDTO updatedPayment = null;

		if (paymentDetail == null) {
			// Create new payment
			PaymentRequestDTO newPayment = new PaymentRequestDTO();
			newPayment.setServiceRequestId(record.getServiceRequestId());
			newPayment.setAmount(record.getActualCost());
			
			PaymentMethod pm = null;
			if (method != null) {
				try {
					pm = PaymentMethod.valueOf(method.toUpperCase());
				} catch (IllegalArgumentException e) {
					log.warn("Invalid payment method: {}", method);
				}
			}
			newPayment.setPaymentMethod(pm != null ? pm : PaymentMethod.UPI);

			try {
				PaymentResponseDTO paymentResponse = paymentClient.createPayment(newPayment);
				if (paymentResponse != null) {
					if (status != null && !status.equalsIgnoreCase(paymentResponse.getStatus())) {
						updatedPayment = paymentClient.updatePaymentStatus(paymentResponse.getPaymentId(), status, method);
					} else {
						updatedPayment = new PaymentDetailResponseDTO(
							paymentResponse.getPaymentId(),
							paymentResponse.getBookingId(),
							paymentResponse.getCustomerId(),
							paymentResponse.getAmount(),
							paymentResponse.getPaymentMethod(),
							paymentResponse.getStatus(),
							paymentResponse.getPaymentDate()
						);
					}
				}
			} catch (Exception ex) {
				log.error("Failed to create payment in updatePaymentStatus: {}", ex.getMessage());
				throw new InvalidStateException("Failed to create payment: " + ex.getMessage());
			}
		} else {
			// Update existing payment
			updatedPayment = paymentClient.updatePaymentStatus(paymentDetail.getPaymentId(), status, method);
		}

		ServiceRecordDetailResponseDTO response = mapper.map(record, ServiceRecordDetailResponseDTO.class);
		response.setRecordId(record.getServiceId());
		if (updatedPayment != null) {
			response.setPaymentMethod(updatedPayment.getPaymentMethod());
			response.setPaymentStatus(updatedPayment.getStatus());
		}
		return response;
	}

	private void requireRole(String... allowedRoles) {
		CustomPrincipal user = SecurityUtils.getCurrentUser();
		if (user == null) {
			throw new InvalidStateException("No authenticated user session");
		}
		String role = user.getRole();
		if (role == null || Arrays.stream(allowedRoles).noneMatch(r -> r.equalsIgnoreCase(role))) {
			throw new InvalidStateException("Access denied for role: " + role);
		}
	}
}