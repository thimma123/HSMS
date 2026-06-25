package com.hsms.assignmentservice.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponseDTO {
    private Long id;
    private Long technicianId;
    private Long serviceRequestId;
    private LocalDateTime assignedDate;
    private LocalDateTime startTime;
    private String status;
}