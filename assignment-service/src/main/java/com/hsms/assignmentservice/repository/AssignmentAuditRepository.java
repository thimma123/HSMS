package com.hsms.assignmentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hsms.assignmentservice.entity.AssignmentAudit;

public interface AssignmentAuditRepository extends JpaRepository<AssignmentAudit, Long> {
}
