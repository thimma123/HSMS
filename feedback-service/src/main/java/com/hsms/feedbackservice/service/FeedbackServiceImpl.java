package com.hsms.feedbackservice.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.hsms.feedbackservice.dto.ServiceRequestResponseDTO;
import com.hsms.feedbackservice.entity.Feedback;
import com.hsms.feedbackservice.event.FeedbackSubmittedEvent;
import com.hsms.feedbackservice.feign.BookingFeignClient;
import com.hsms.feedbackservice.feign.UserFeignClient;
import com.hsms.feedbackservice.repository.FeedbackRepository;
import com.hsms.feedbackservice.security.CustomPrincipal;
import com.hsms.feedbackservice.security.SecurityUtils;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private BookingFeignClient bookingFeignClient;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public Feedback saveFeedback(Feedback feedback) {
        // 1. Rating must be between 1 and 5
        if (feedback.getRating() == null || feedback.getRating() < 1 || feedback.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        // 2. Only one feedback per request allowed
        feedbackRepository.findByServiceRequestId(feedback.getServiceRequestId()).ifPresent(f -> {
            throw new IllegalArgumentException("Feedback already submitted for service request ID: " + feedback.getServiceRequestId());
        });

        // 3. Retrieve caller customer ID from security context to validate ownership
        CustomPrincipal currentUser = SecurityUtils.getCurrentUser();
        Long customerId = currentUser != null ? currentUser.getUserId() : null;
        if (customerId == null) {
            throw new IllegalArgumentException("No authenticated customer session found");
        }

        // 4. Validate against booking-service using OpenFeign client
        ServiceRequestResponseDTO booking = null;
        try {
            booking = bookingFeignClient.getServiceRequestById(feedback.getServiceRequestId());
        } catch (Exception e) {
            throw new IllegalArgumentException("Validation failed against booking-service: " + e.getMessage());
        }

        if (booking == null) {
            throw new IllegalArgumentException("Service request not found with ID: " + feedback.getServiceRequestId());
        }

        String status = booking.getStatus();
        if (!"COMPLETED".equalsIgnoreCase(status) && !"PAID".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("Feedback is only allowed for COMPLETED or PAID service requests");
        }

        if (booking.getCustomerId() != null && !booking.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("You can only submit feedback for your own service requests");
        }

        // Set properties and save feedback
        feedback.setUserId(customerId);
        feedback.setTechnicianId(booking.getTechnicianId());
        Feedback savedFeedback = feedbackRepository.save(feedback);

        // 5. Update technician average rating in user-service
        if (savedFeedback.getTechnicianId() != null) {
            try {
                List<Feedback> technicianFeedbacks = feedbackRepository.findByTechnicianId(savedFeedback.getTechnicianId());
                double sum = 0.0;
                for (Feedback f : technicianFeedbacks) {
                    if (f.getRating() != null) {
                        sum += f.getRating();
                    }
                }
                double avgRating = technicianFeedbacks.isEmpty() ? 0.0 : sum / technicianFeedbacks.size();
                userFeignClient.updateTechnicianRating(savedFeedback.getTechnicianId(), avgRating);
            } catch (Exception e) {
                // Failures to update average rating should not fail the main transaction
                System.err.println("Failed to update technician average rating in user-service: " + e.getMessage());
            }
        }

        // 6. Publish async event
        eventPublisher.publishEvent(FeedbackSubmittedEvent.builder()
                .userId(savedFeedback.getUserId())
                .message("Thank you for your feedback. Your rating: " + savedFeedback.getRating())
                .build());

        return savedFeedback;
    }

    @Override
    public List<Feedback> getAllFeedbacks() {
        return feedbackRepository.findAll();
    }

    @Override
    public Feedback getFeedbackById(Long id) {
        return feedbackRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Feedback not found with id: " + id));
    }

    @Override
    public Feedback updateFeedback(Long id, Feedback feedbackDetails) {
        Feedback existingFeedback = feedbackRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Feedback not found with id: " + id));

        existingFeedback.setUserId(feedbackDetails.getUserId());
        existingFeedback.setServiceRequestId(
                feedbackDetails.getServiceRequestId());
        existingFeedback.setRating(feedbackDetails.getRating());
        existingFeedback.setComments(feedbackDetails.getComments());

        return feedbackRepository.save(existingFeedback);
    }

    @Override
    public void deleteFeedback(Long id) {
        if (!feedbackRepository.existsById(id)) {
            throw new RuntimeException(
                    "Feedback not found with id: " + id);
        }
        feedbackRepository.deleteById(id);
    }
}