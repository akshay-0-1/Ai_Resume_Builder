package com.project.resumeTracker.service;

import com.project.resumeTracker.dto.FeedbackDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;



public interface FeedbackService {
        FeedbackDTO saveFeedback(FeedbackDTO feedbackDTO, Long userId);
    Page<FeedbackDTO> getAllFeedback(Pageable pageable);
}
