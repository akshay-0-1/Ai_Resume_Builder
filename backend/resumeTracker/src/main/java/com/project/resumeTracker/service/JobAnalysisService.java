package com.project.resumeTracker.service;

import com.project.resumeTracker.dto.JobAnalysisHistoryDTO;
import com.project.resumeTracker.dto.JobAnalysisResponseDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface JobAnalysisService {
    JobAnalysisResponseDTO analyzeResumeAndJobDescription(UUID resumeId, String jobDescription, Long userId);

    List<JobAnalysisHistoryDTO> getJobAnalysisHistory(Long userId, Pageable pageable);
}
