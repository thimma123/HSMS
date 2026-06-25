package com.hsms.execution_service.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.hsms.execution_service.entity.ServiceRecord;

public interface ServiceRecordRepository extends JpaRepository<ServiceRecord, Long> {
    Optional<ServiceRecord> findByServiceRequestId(Long serviceRequestId);
}