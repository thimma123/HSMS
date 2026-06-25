package com.hsms.assignmentservice.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "assignment_audits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long assignmentId;

    private Long serviceRequestId;

    private Long technicianId;

    private Long assignedBy;

    private LocalDateTime auditTimestamp;

    private String previousStatus;

    private String newStatus;

    private String rejectionReason;
}
