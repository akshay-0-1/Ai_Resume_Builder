package com.project.resumeTracker.service;

import com.project.resumeTracker.dto.FeedbackDTO;

import java.util.List;


public interface FeedbackService {
        FeedbackDTO saveFeedback(FeedbackDTO feedbackDTO, Long userId);
    List<FeedbackDTO> getAllFeedback();
}
