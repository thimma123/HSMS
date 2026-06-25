package com.hsms.execution_service.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRecordDetailResponseDTO {
    private Long recordId;
    private Long serviceRequestId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String remarks;
    private Double actualCost;
    private String paymentMethod;
    private String status;
    private String paymentStatus;
    private String technicianNotes;
    private String executionNotes;
}