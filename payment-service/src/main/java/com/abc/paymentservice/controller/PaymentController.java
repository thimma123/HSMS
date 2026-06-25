package com.abc.paymentservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

import com.abc.paymentservice.enums.PaymentStatus;
import com.abc.paymentservice.model.PaymentRequestDTO;
import com.abc.paymentservice.model.PaymentResponseDTO;
import com.abc.paymentservice.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

	@Autowired
	private PaymentService paymentService;

	// Create Payment (callable by Customer, or by Technician as part of auto-triggering on service completion)
	@PreAuthorize("hasAnyAuthority('CUSTOMER','ADMIN','SERVICE_MANAGER','TECHNICIAN')")
	@PostMapping("/save")
	public ResponseEntity<PaymentResponseDTO> createPayment(
	        @Valid @RequestBody PaymentRequestDTO dto) {

	    return ResponseEntity.status(HttpStatus.CREATED)
	            .body(paymentService.createPayment(dto));
	}

	// Get Payment By Id
	@PreAuthorize("hasAnyAuthority('ADMIN','SERVICE_MANAGER','CUSTOMER','TECHNICIAN')")
	@GetMapping("/{paymentId}")
	public ResponseEntity<PaymentResponseDTO> getPaymentById(
	        @PathVariable Long paymentId) {

	    return ResponseEntity.status(HttpStatus.OK)
	            .body(paymentService.getPaymentById(paymentId));
	}

	// Get Payments By Customer
	@PreAuthorize("hasAnyAuthority('ADMIN','SERVICE_MANAGER','CUSTOMER')")
	@GetMapping("/customer/{customerId}")
	public ResponseEntity<Page<PaymentResponseDTO>>
	        getPaymentsByCustomer(
	                @PathVariable Long customerId,
	                Pageable pageable) {

	    return ResponseEntity.status(HttpStatus.OK)
	            .body(paymentService.getPaymentsByCustomer(
	                    customerId,
	                    pageable));
	}

	// Update Payment Status
	@PreAuthorize("hasAnyAuthority('ADMIN','SERVICE_MANAGER','TECHNICIAN','CUSTOMER')")
	@PutMapping("/status/{paymentId}")
	public ResponseEntity<PaymentResponseDTO>
	        updatePaymentStatus(
	                @PathVariable Long paymentId,
	                @RequestParam PaymentStatus status,
	                @RequestParam(required = false) String method) {

	    return ResponseEntity.status(HttpStatus.OK)
	            .body(paymentService.updatePaymentStatus(
	                    paymentId,
	                    status,
	                    method));
	}

	// Show All Payments
	@PreAuthorize("hasAnyAuthority('ADMIN','SERVICE_MANAGER')")
	@GetMapping("/all")
	public ResponseEntity<List<PaymentResponseDTO>>
	        showAllPayments() {

	    return ResponseEntity.status(HttpStatus.OK)
	            .body(paymentService.showAllPayments());
	}

	@PreAuthorize("hasAuthority('ADMIN')")
	@DeleteMapping("/{paymentId}")
	public ResponseEntity<String> deletePayment(
	        @PathVariable Long paymentId) {

	    paymentService.deletePayment(paymentId);

	    return ResponseEntity.ok(
	            "Payment deleted successfully");
	}

	@PreAuthorize("hasAnyAuthority('ADMIN','SERVICE_MANAGER','CUSTOMER','TECHNICIAN')")
	@GetMapping("/service-request/{serviceRequestId}")
	public ResponseEntity<PaymentResponseDTO> getPaymentByServiceRequestId(
	        @PathVariable Long serviceRequestId) {

	    return ResponseEntity.status(HttpStatus.OK)
	            .body(paymentService.getPaymentByServiceRequestId(serviceRequestId));
	}
}