package com.project.resumeTracker.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class JobAnalysisRequestDTO {
    private UUID resumeId;
    private String jobDescription;
}
