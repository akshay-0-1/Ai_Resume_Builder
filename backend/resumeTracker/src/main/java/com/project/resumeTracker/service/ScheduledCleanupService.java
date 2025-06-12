package com.project.resumeTracker.service;

import com.project.resumeTracker.entity.JobAnalysis;
import com.project.resumeTracker.entity.Resume;
import com.project.resumeTracker.repository.JobAnalysisRepository;
import com.project.resumeTracker.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledCleanupService {

    private final ResumeRepository resumeRepository;
    private final JobAnalysisRepository jobAnalysisRepository;

    /**
     * This scheduled task runs to clean up old resumes.
     * It runs 60 seconds after application startup and every hour thereafter.
     * It finds resumes older than 1 hour and performs a soft delete by setting their isActive flag to false.
     */
    @Scheduled(initialDelay = 60000, fixedRate = 3600000) // 1-minute initial delay, then runs every 1 hour
    public void cleanupOldResumes() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        log.info("Running scheduled cleanup for resumes older than: {}", oneHourAgo);

        List<Resume> oldResumes = resumeRepository.findAllByIsActiveTrueAndUploadDateBefore(oneHourAgo);

        if (oldResumes.isEmpty()) {
            log.info("No old resumes found to clean up.");
            return;
        }

        for (Resume resume : oldResumes) {
            resume.setIsActive(false);
        }

        resumeRepository.saveAll(oldResumes);
        log.info("Successfully soft-deleted {} old resumes.", oldResumes.size());
    }

    /**
     * This scheduled task runs to clean up old job analysis history.
     * It runs 120 seconds after application startup and every 2 hours thereafter.
     * It finds analysis records older than 7 days and performs a soft delete by setting their isActive flag to false.
     */
    @Scheduled(initialDelay = 120000, fixedRate = 7200000) // 2-minute initial delay, then runs every 2 hours
    public void cleanupOldAnalysisHistory() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        log.info("Running scheduled cleanup for analysis history older than: {}", sevenDaysAgo);

        List<JobAnalysis> oldAnalyses = jobAnalysisRepository.findByIsActiveTrueAndCreatedAtBefore(sevenDaysAgo);

        if (oldAnalyses.isEmpty()) {
            log.info("No old analysis history found to clean up.");
            return;
        }

        for (JobAnalysis analysis : oldAnalyses) {
            analysis.setIsActive(false);
        }

        jobAnalysisRepository.saveAll(oldAnalyses);
        log.info("Successfully soft-deleted {} old analysis history records.", oldAnalyses.size());
    }
}
