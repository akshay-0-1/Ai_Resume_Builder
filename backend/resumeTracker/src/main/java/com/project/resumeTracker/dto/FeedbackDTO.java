package com.project.resumeTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackDTO {
    private UUID id;
    private String username;
    private int rating;
    private String feedbackText;
    private LocalDateTime createdAt;
}
