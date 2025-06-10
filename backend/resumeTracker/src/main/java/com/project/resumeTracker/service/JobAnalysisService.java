package com.project.resumeTracker.service;

import com.project.resumeTracker.dto.JobAnalysisResponseDTO;
import org.springframework.web.multipart.MultipartFile;
// Import necessary Gemini SDK classes here later

public interface JobAnalysisService {
    JobAnalysisResponseDTO analyzeResumeAndJobDescription(MultipartFile resumeFile, String jobDescription);
}
