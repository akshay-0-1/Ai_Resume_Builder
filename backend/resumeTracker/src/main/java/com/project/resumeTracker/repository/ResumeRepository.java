package com.project.resumeTracker.repository;

import com.project.resumeTracker.entity.Resume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, UUID> {

    List<Resume> findByUserIdAndIsActiveTrue(UUID userId);

    Page<Resume> findByUserIdAndIsActiveTrue(UUID userId, Pageable pageable);

    @Query("SELECT r FROM Resume r WHERE r.userId = :userId AND r.isActive = true ORDER BY r.uploadDate DESC")
    List<Resume> findActiveResumesByUserIdOrderByDate(UUID userId);

    long countByUserIdAndIsActiveTrue(UUID userId);
}
