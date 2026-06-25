package com.abc.paymentservice.service;

import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.abc.paymentservice.entity.Payment;
import com.abc.paymentservice.entity.PaymentAudit;
import com.abc.paymentservice.enums.PaymentStatus;
import com.abc.paymentservice.event.PaymentSuccessEvent;
import com.abc.paymentservice.exception.InvalidPaymentException;
import com.abc.paymentservice.exception.PaymentNotFoundException;
import com.abc.paymentservice.exception.ServiceUnavailableException;
import com.abc.paymentservice.feignclient.BookingFeignClient;
import com.abc.paymentservice.model.BookingResponseDTO;
import com.abc.paymentservice.model.PaymentRequestDTO;
import com.abc.paymentservice.model.PaymentResponseDTO;
import com.abc.paymentservice.repository.PaymentAuditRepository;
import com.abc.paymentservice.repository.PaymentRepository;
import com.abc.paymentservice.security.CustomPrincipal;
import com.abc.paymentservice.security.SecurityUtils;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

	@Autowired
	private PaymentRepository paymentRepo;

	@Autowired
	private PaymentAuditRepository paymentAuditRepository;

	@Autowired
	private BookingFeignClient bookingFeignClient;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	// Create Payment
	@Override
	@CircuitBreaker(name = "paymentService", fallbackMethod = "createPaymentFallback")
	public PaymentResponseDTO createPayment(PaymentRequestDTO dto) {
		// Validate Booking
		BookingResponseDTO booking = null;
		try {
			booking = bookingFeignClient.getBookingById(dto.getServiceRequestId());
		} catch (Exception e) {
			log.error("Error communicating with booking-service: {}", e.getMessage());
			auditFailedPaymentAttempt(dto, "Booking validation failed: " + e.getMessage());
			throw e;
		}

		if (booking == null) {
			auditFailedPaymentAttempt(dto, "Booking not found");
			throw new InvalidPaymentException("Booking not found");
		}

		CustomPrincipal user = SecurityUtils.getCurrentUser();
		if (user != null) {
			String role = user.getRole();
			Long currentUserId = user.getUserId();
			if ("CUSTOMER".equalsIgnoreCase(role)) {
				if (!currentUserId.equals(booking.getCustomerId())) {
					auditFailedPaymentAttempt(dto, "Access denied: customer ID mismatch");
					throw new InvalidPaymentException("You can only pay for your own service requests");
				}
			} else if (!"TECHNICIAN".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role) && !"SERVICE_MANAGER".equalsIgnoreCase(role)) {
				auditFailedPaymentAttempt(dto, "Access denied: invalid role: " + role);
				throw new InvalidPaymentException("Access denied for role: " + role);
			}
		}

		if (!"COMPLETED".equalsIgnoreCase(booking.getStatus()) && !"PAID".equalsIgnoreCase(booking.getStatus())) {
			auditFailedPaymentAttempt(dto, "Booking status not COMPLETED or PAID");
			throw new InvalidPaymentException("Payment is only allowed for COMPLETED requests");
		}

		// Prevent Duplicate Payment / Handle Retry
		var existingPaymentOpt = paymentRepo.findByServiceRequestId(dto.getServiceRequestId());
		if (existingPaymentOpt.isPresent()) {
			Payment existingPayment = existingPaymentOpt.get();
			if (existingPayment.getPaymentStatus() == PaymentStatus.SUCCESS) {
				auditFailedPaymentAttempt(dto, "Payment already completed successfully");
				throw new InvalidPaymentException("Payment already completed successfully for booking " + dto.getServiceRequestId());
			} else {
				// Retry failed/pending payment
				com.abc.paymentservice.enums.PaymentMethod method = null;
				if (dto.getPaymentMethod() != null) {
					try {
						method = com.abc.paymentservice.enums.PaymentMethod.valueOf(dto.getPaymentMethod().toUpperCase());
					} catch (IllegalArgumentException e) {
						// Keep method null
					}
				}
				if (method == null) {
					existingPayment.setPaymentStatus(PaymentStatus.FAILED);
					auditPayment(existingPayment, "Payment retry failed: Invalid method", null);
				} else {
					existingPayment.setPaymentMethod(method);
					existingPayment.setPaymentStatus(PaymentStatus.SUCCESS);
					bookingFeignClient.updateStatus(dto.getServiceRequestId(), "PAID");
					auditPayment(existingPayment, "Payment retry succeeded", null);
					publishSuccessEvent(existingPayment);
				}
				existingPayment.setPaymentDate(LocalDateTime.now());
				Payment saved = paymentRepo.save(existingPayment);
				return modelMapper.map(saved, PaymentResponseDTO.class);
			}
		}

		Payment payment = new Payment();
		payment.setServiceRequestId(dto.getServiceRequestId());
		payment.setAmount(java.math.BigDecimal.valueOf(dto.getAmount() != null ? dto.getAmount() : 0.0));
		payment.setCustomerId(dto.getCustomerId() != null ? dto.getCustomerId() : booking.getCustomerId());

		// Parse payment method
		com.abc.paymentservice.enums.PaymentMethod method = null;
		if (dto.getPaymentMethod() != null) {
			try {
				method = com.abc.paymentservice.enums.PaymentMethod.valueOf(dto.getPaymentMethod().toUpperCase());
			} catch (IllegalArgumentException e) {
				// Keep method null
			}
		}

		if (method == null) {
			payment.setPaymentMethod(com.abc.paymentservice.enums.PaymentMethod.UPI); // Default placeholder
			payment.setPaymentStatus(PaymentStatus.FAILED);
			Payment saved = paymentRepo.save(payment);
			auditPayment(saved, "Payment failed: Invalid method", "Method was not supported");
			return modelMapper.map(saved, PaymentResponseDTO.class);
		} else {
			payment.setPaymentMethod(method);
			payment.setPaymentStatus(PaymentStatus.SUCCESS);
			bookingFeignClient.updateStatus(dto.getServiceRequestId(), "PAID");
		}

		payment.setPaymentDate(LocalDateTime.now());
		Payment saved = paymentRepo.save(payment);

		if (saved.getPaymentStatus() == PaymentStatus.SUCCESS) {
			auditPayment(saved, "Payment succeeded", null);
			publishSuccessEvent(saved);
		}

		return modelMapper.map(saved, PaymentResponseDTO.class);
	}

	// Fallback Methods
	public PaymentResponseDTO createPaymentFallback(PaymentRequestDTO dto, InvalidPaymentException ex) {
		throw ex;
	}

	public PaymentResponseDTO createPaymentFallback(PaymentRequestDTO dto, Exception ex) {
		throw new ServiceUnavailableException("Booking Service is currently unavailable. Please try again later.");
	}

	// Get Payment By Id
	@Override
	public PaymentResponseDTO getPaymentById(Long paymentId) {
		Payment payment = paymentRepo.findById(paymentId)
				.orElseThrow(() -> new PaymentNotFoundException("Payment " + paymentId + " not found"));
		return modelMapper.map(payment, PaymentResponseDTO.class);
	}

	// Get Payments By Customer
	@Override
	public Page<PaymentResponseDTO> getPaymentsByCustomer(Long customerId, Pageable pageable) {
		return paymentRepo.findByCustomerId(customerId, pageable)
				.map(payment -> modelMapper.map(payment, PaymentResponseDTO.class));
	}

	// Update Payment Status
	@Override
	public PaymentResponseDTO updatePaymentStatus(Long paymentId, PaymentStatus status, String method) {
		Payment payment = paymentRepo.findById(paymentId)
				.orElseThrow(() -> new PaymentNotFoundException("Payment " + paymentId + " not found"));

		payment.setPaymentStatus(status);
		if (method != null) {
			try {
				payment.setPaymentMethod(com.abc.paymentservice.enums.PaymentMethod.valueOf(method.toUpperCase()));
			} catch (IllegalArgumentException e) {
				log.warn("Invalid payment method: {}", method);
			}
		}
		Payment updated = paymentRepo.save(payment);

		if (status == PaymentStatus.SUCCESS) {
			bookingFeignClient.updateStatus(payment.getServiceRequestId(), "PAID");
			auditPayment(updated, "Payment status updated to SUCCESS", null);
			publishSuccessEvent(updated);
		} else {
			auditPayment(updated, "Payment status updated to " + status, null);
		}

		return modelMapper.map(updated, PaymentResponseDTO.class);
	}

	// Show All Payments
	@Override
	public List<PaymentResponseDTO> showAllPayments() {
		return paymentRepo.findAll().stream().map(payment -> modelMapper.map(payment, PaymentResponseDTO.class))
				.toList();
	}

	@Override
	public void deletePayment(Long paymentId) {
		Payment payment = paymentRepo.findById(paymentId)
				.orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

		paymentRepo.delete(payment);
	}

	private void auditPayment(Payment payment, String message, String errorDetails) {
		PaymentAudit audit = PaymentAudit.builder()
				.paymentId(payment != null ? payment.getPaymentId() : null)
				.serviceRequestId(payment != null ? payment.getServiceRequestId() : null)
				.customerId(payment != null ? payment.getCustomerId() : null)
				.amount(payment != null ? payment.getAmount() : java.math.BigDecimal.ZERO)
				.paymentMethod(payment != null ? payment.getPaymentMethod() : null)
				.paymentStatus(payment != null ? payment.getPaymentStatus() : PaymentStatus.FAILED)
				.logTimestamp(LocalDateTime.now())
				.message(message)
				.errorDetails(errorDetails)
				.build();
		paymentAuditRepository.save(audit);
	}

	private void auditFailedPaymentAttempt(PaymentRequestDTO dto, String errorMessage) {
		PaymentAudit audit = PaymentAudit.builder()
				.serviceRequestId(dto.getServiceRequestId())
				.customerId(dto.getCustomerId())
				.amount(java.math.BigDecimal.valueOf(dto.getAmount() != null ? dto.getAmount() : 0.0))
				.paymentStatus(PaymentStatus.FAILED)
				.logTimestamp(LocalDateTime.now())
				.message("Payment attempt failed")
				.errorDetails(errorMessage)
				.build();
		paymentAuditRepository.save(audit);
	}

	private void publishSuccessEvent(Payment payment) {
		eventPublisher.publishEvent(PaymentSuccessEvent.builder()
				.paymentId(payment.getPaymentId())
				.serviceRequestId(payment.getServiceRequestId())
				.customerId(payment.getCustomerId())
				.amount(payment.getAmount())
				.build());
	}

	@Override
	public PaymentResponseDTO getPaymentByServiceRequestId(Long serviceRequestId) {
		Payment payment = paymentRepo.findByServiceRequestId(serviceRequestId)
				.orElseThrow(() -> new PaymentNotFoundException("Payment not found for service request ID: " + serviceRequestId));
		return modelMapper.map(payment, PaymentResponseDTO.class);
	}
}