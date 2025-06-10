package com.project.resumeTracker.service;

import com.project.resumeTracker.dto.JobAnalysisResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface JobAnalysisService {
    JobAnalysisResponseDTO analyzeResumeAndJobDescription(UUID resumeId, String jobDescription, UUID userId);
}
