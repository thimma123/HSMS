package com.hsms.assignmentservice.model;

import java.time.LocalDateTime;

import com.hsms.assignmentservice.entity.AssignmentStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentDetailResponseDTO {
    private Long id;
    private Long technician_Id;
    private Long serviceRequestId;
    private LocalDateTime assignedDate;
    private LocalDateTime startTime;
    private AssignmentStatus status;
    private TechnicianDetailResponseDTO technician; 
}