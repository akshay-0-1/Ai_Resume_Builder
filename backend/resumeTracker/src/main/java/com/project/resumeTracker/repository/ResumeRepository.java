package com.project.resumeTracker.repository;

import com.project.resumeTracker.dto.ResumeInfoDTO;
import com.project.resumeTracker.entity.Resume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, UUID> {

    List<Resume> findByUserIdAndIsActiveTrueOrderByUploadDateDesc(Long userId);

    Page<Resume> findByUserIdAndIsActiveTrueOrderByUploadDateDesc(Long userId, Pageable pageable);

    @Query("SELECT new com.project.resumeTracker.dto.ResumeInfoDTO(r.id, r.originalFilename, r.fileSize, r.uploadDate, r.mimeType) FROM Resume r WHERE r.userId = :userId AND r.isActive = true ORDER BY r.uploadDate DESC")
    List<ResumeInfoDTO> findResumeInfoByUserId(Long userId);

    @Query("SELECT new com.project.resumeTracker.dto.ResumeInfoDTO(r.id, r.originalFilename, r.fileSize, r.uploadDate, r.mimeType) FROM Resume r WHERE r.userId = :userId AND r.isActive = true ORDER BY r.uploadDate DESC")
    Page<ResumeInfoDTO> findResumeInfoByUserId(Long userId, Pageable pageable);

    Optional<Resume> findByIdAndUserId(UUID id, Long userId);

    boolean existsByIdAndUserId(UUID id, Long userId);

    List<Resume> findAllByIsActiveTrueAndUploadDateBefore(LocalDateTime timestamp);

}
