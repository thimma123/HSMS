package com.hsms.execution_service.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponseDTO {
	private Long id;
	private Long technicianId;
	private Long serviceRequestId;
	private LocalDateTime assignedDate;
	private String status;
}
