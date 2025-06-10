package com.project.resumeTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobAnalysisResponseDTO {
    private String jobScore; // Or could be a more complex object if the score has details
    private String improvementHighlights; // Or List<String>
}
