package com.abc.paymentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.abc.paymentservice.entity.PaymentAudit;

public interface PaymentAuditRepository extends JpaRepository<PaymentAudit, Long> {
}
