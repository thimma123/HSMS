package com.hsms.feedbackservice.service;

import java.util.List;

import com.hsms.feedbackservice.entity.Feedback;

public interface FeedbackService {

    Feedback saveFeedback(Feedback feedback);

    List<Feedback> getAllFeedbacks();

    Feedback getFeedbackById(Long id);

    Feedback updateFeedback(Long id, Feedback feedback);

    void deleteFeedback(Long id);
}