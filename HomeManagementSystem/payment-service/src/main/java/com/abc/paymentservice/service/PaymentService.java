package com.abc.paymentservice.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.abc.paymentservice.enums.PaymentStatus;
import com.abc.paymentservice.model.PaymentRequestDTO;
import com.abc.paymentservice.model.PaymentResponseDTO;

public interface PaymentService {
	
	
	
	PaymentResponseDTO createPayment( PaymentRequestDTO dto);

	PaymentResponseDTO getPaymentById(Long paymentId);

	Page<PaymentResponseDTO> getPaymentsByCustomer( Long customerId, Pageable pageable);

	PaymentResponseDTO updatePaymentStatus(Long paymentId, PaymentStatus status, String method);

	List<PaymentResponseDTO> showAllPayments();

	void deletePayment(Long paymentId);

	PaymentResponseDTO getPaymentByServiceRequestId(Long serviceRequestId);
}