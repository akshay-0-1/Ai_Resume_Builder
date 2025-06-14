package com.project.resumeTracker.repository;

import com.project.resumeTracker.entity.JobAnalysis;
import com.project.resumeTracker.dto.JobAnalysisHistoryDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface JobAnalysisRepository extends JpaRepository<JobAnalysis, UUID> {

    /**
     * Finds the top 5 most recent, active job analyses for a specific user.
     *
     * @param userId The UUID of the user.
     * @return A list of the top 5 job analyses, ordered by creation date descending.
     */
    List<JobAnalysis> findTop5ByUserIdAndIsActiveTrueOrderByCreatedAtDesc(UUID userId);

    @Query("SELECT new com.project.resumeTracker.dto.JobAnalysisHistoryDTO(ja.id, ja.resume.id, r.originalFilename, ja.jobDescription, ja.jobScore, ja.createdAt) " +
           "FROM JobAnalysis ja JOIN ja.resume r " +
           "WHERE ja.userId = :userId AND ja.isActive = true " +
           "ORDER BY ja.createdAt DESC")
    List<JobAnalysisHistoryDTO> findJobAnalysisHistoryByUserId(UUID userId, Pageable pageable);

    /**
     * Finds all job analyses that were created before a certain timestamp and are still active.
     *
     * @param timestamp The cutoff time.
     * @return A list of old, active job analyses.
     */
    List<JobAnalysis> findByIsActiveTrueAndCreatedAtBefore(LocalDateTime timestamp);

    /**
     * Finds all active job analyses for a specific user, ordered by creation date ascending.
     *
     * @param userId The UUID of the user.
     * @return A list of job analyses for the user, sorted from oldest to newest.
     */
    List<JobAnalysis> findByUserIdAndIsActiveTrueOrderByCreatedAtAsc(UUID userId);
}
