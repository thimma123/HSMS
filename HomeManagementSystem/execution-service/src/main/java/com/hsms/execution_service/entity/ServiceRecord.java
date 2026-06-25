package com.hsms.execution_service.entity;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "SERVICE_RECORDS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long serviceId;

    @Column(name = "SERVICE_REQUEST_ID", nullable = false)
    private Long serviceRequestId;

    @Column(name = "TECHNICIAN_ID", nullable = false)
    private Long technicianId;

    @Column(name = "START_TIME", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "END_TIME")
    private LocalDateTime endTime;

    @Column(name = "REMARKS")
    private String remarks;

    @Column(name = "ACTUAL_COST", nullable = false)
    private Double actualCost;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "TECHNICIAN_NOTES")
    private String technicianNotes;

    @Column(name = "EXECUTION_NOTES")
    private String executionNotes;
}