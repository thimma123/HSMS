package com.abc.paymentservice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.abc.paymentservice.enums.PaymentMethod;
import com.abc.paymentservice.enums.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "payment_tbl")
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name = "PAYMENT_ID")
	private Long paymentId;

	@Column(name = "SERVICE_REQUEST_ID",
	        nullable = false,
	        unique = true)
	private Long serviceRequestId;

	@Column(name = "AMOUNT",
	        nullable = false)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	@Column(name = "PAYMENT_METHOD",
	        nullable = false)
	private PaymentMethod paymentMethod;

	@Enumerated(EnumType.STRING)
	@Column(name = "PAYMENT_STATUS",
	        nullable = false)
	private PaymentStatus paymentStatus;
	
	private Long customerId;
	
	private LocalDateTime paymentDate;
	
}
