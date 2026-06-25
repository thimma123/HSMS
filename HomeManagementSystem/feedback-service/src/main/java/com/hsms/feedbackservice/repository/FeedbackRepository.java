package com.hsms.feedbackservice.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hsms.feedbackservice.entity.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    Optional<Feedback> findByServiceRequestId(Long serviceRequestId);
    java.util.List<Feedback> findByTechnicianId(Long technicianId);
}