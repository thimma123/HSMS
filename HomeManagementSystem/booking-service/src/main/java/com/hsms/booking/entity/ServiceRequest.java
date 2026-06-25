package com.hsms.booking.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.hsms.booking.enums.ServiceRequestStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "SERVICE_REQUESTS",
    indexes = {
        @Index(name = "IDX_CUSTOMER_ID", columnList = "CUSTOMER_ID"),
        @Index(name = "IDX_CATEGORY_ID", columnList = "CATEGORY_ID"),
        @Index(name = "IDX_STATUS", columnList = "STATUS"),
        @Index(name = "IDX_SCHEDULED_DATE_TIME", columnList = "SCHEDULED_DATE_TIME"),
        @Index(name = "IDX_CREATED_AT", columnList = "CREATED_AT"),
        @Index(name = "IDX_CITY", columnList = "CITY"),
        @Index(name = "IDX_PINCODE", columnList = "PINCODE"),
        @Index(name = "IDX_STATUS_CREATED_AT", columnList = "STATUS,CREATED_AT"),
        @Index(name = "IDX_CUSTOMER_STATUS", columnList = "CUSTOMER_ID,STATUS"),
        @Index(name = "IDX_CATEGORY_STATUS", columnList = "CATEGORY_ID,STATUS"),
        @Index(name = "IDX_TECHNICIAN_STATUS", columnList = "TECHNICIAN_ID,STATUS")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "service_request_seq")
    @SequenceGenerator(
        name = "service_request_seq",
        sequenceName = "SERVICE_REQUEST_SEQ",
        allocationSize = 1
    )
    @Column(name = "REQUEST_ID")
    private Long requestId;

    @Column(name = "CUSTOMER_ID", nullable = false)
    private Long customerId;

    @Column(name = "CATEGORY_ID", nullable = false)
    private Long categoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    @Builder.Default
    private ServiceRequestStatus status = ServiceRequestStatus.CREATED;

    @Column(name = "ADDRESS", nullable = false, length = 255)
    private String address;

    @Column(name = "CITY", length = 100)
    private String city;

    @Column(name = "PINCODE", length = 10)
    private String pincode;

    @Column(name = "SCHEDULED_DATE_TIME", nullable = false)
    private LocalDateTime scheduledDateTime;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Column(name = "PRIORITY", length = 20)
    private String priority;

    @Column(name = "TECHNICIAN_ID")
    private Long technicianId;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
}