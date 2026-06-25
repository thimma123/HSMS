package com.hsms.assignmentservice.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hsms.assignmentservice.entity.Assignment;
import com.hsms.assignmentservice.entity.AssignmentStatus;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

	Optional<Assignment> findByServiceRequestId(Long serviceRequestId);

	List<Assignment> findByTechnicianIdAndStatusIn(Long technician_Id, List<AssignmentStatus> of);

	List<Assignment> findByTechnicianId(Long technician_Id);

	boolean existsByTechnicianIdAndStartTimeAndStatusIn(Long technician_Id, LocalDateTime startTime,
			List<AssignmentStatus> of);


}