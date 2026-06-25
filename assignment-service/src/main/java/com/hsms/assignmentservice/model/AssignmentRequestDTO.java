package com.hsms.assignmentservice.model;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentRequestDTO {

    private Long technicianId;
    private Long serviceRequestId;
    private LocalDateTime startTime;

    private Long userId;
}