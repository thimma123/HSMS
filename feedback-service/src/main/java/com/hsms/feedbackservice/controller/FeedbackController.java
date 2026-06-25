package com.hsms.feedbackservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hsms.feedbackservice.dto.FeedbackDTO;
import com.hsms.feedbackservice.entity.Feedback;
import com.hsms.feedbackservice.service.FeedbackService;
import com.hsms.feedbackservice.security.CustomPrincipal;
import com.hsms.feedbackservice.security.SecurityUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @PostMapping
    public Feedback saveFeedback(
            @Valid @RequestBody FeedbackDTO feedbackDTO) {

        Feedback feedback = new Feedback();
        feedback.setServiceRequestId(feedbackDTO.getServiceRequestId());
        feedback.setRating(feedbackDTO.getRating());
        feedback.setComments(feedbackDTO.getComments());

        // Note: Customer ID is set inside the service using SecurityUtils context
        return feedbackService.saveFeedback(feedback);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','SERVICE_MANAGER')")
    @GetMapping
    public List<Feedback> getAllFeedbacks() {
        return feedbackService.getAllFeedbacks();
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','SERVICE_MANAGER','CUSTOMER')")
    @GetMapping("/{id}")
    public Feedback getFeedbackById(@PathVariable Long id) {
        return feedbackService.getFeedbackById(id);
    }

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @PutMapping("/{id}")
    public Feedback updateFeedback(
            @PathVariable Long id,
            @Valid @RequestBody FeedbackDTO feedbackDTO) {

        CustomPrincipal currentUser = SecurityUtils.getCurrentUser();
        Long userId = currentUser != null ? currentUser.getUserId() : null;

        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setServiceRequestId(feedbackDTO.getServiceRequestId());
        feedback.setRating(feedbackDTO.getRating());
        feedback.setComments(feedbackDTO.getComments());

        return feedbackService.updateFeedback(id, feedback);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public String deleteFeedback(@PathVariable Long id) {
        feedbackService.deleteFeedback(id);
        return "Feedback deleted successfully";
    }
}