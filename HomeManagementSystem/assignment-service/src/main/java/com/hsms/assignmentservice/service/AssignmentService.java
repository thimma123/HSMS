package com.hsms.assignmentservice.service;

import java.time.LocalDateTime;
import java.util.List;

import com.hsms.assignmentservice.model.AssignmentDetailResponseDTO;
import com.hsms.assignmentservice.model.AssignmentRequestDTO;
import com.hsms.assignmentservice.model.AssignmentResponseDTO;

public interface AssignmentService {
    AssignmentResponseDTO assignTechnician(AssignmentRequestDTO requestDTO);
    List<AssignmentDetailResponseDTO> getAllAssignments();
    AssignmentResponseDTO reassignTechnician(Long assignmentId, Long technicianId,LocalDateTime startTime);
	void deleteAssignment(Long assignmentId);
	AssignmentResponseDTO acceptJob(Long assignmentId);
    AssignmentResponseDTO rejectJob(Long assignmentId, String rejectionReason);
    AssignmentResponseDTO getAssignmentByServiceRequestId(Long serviceRequestId);
    List<AssignmentResponseDTO> getAssignmentsByTechnician(Long technicianId);
}