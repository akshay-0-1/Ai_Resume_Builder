package com.project.resumeTracker.service;


import com.project.resumeTracker.dto.ResumeResponseDTO;
import com.project.resumeTracker.entity.Resume;
import com.project.resumeTracker.repository.ResumeRepository;
import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final SupabaseStorageService storageService;

    @Value("${supabase.storage.bucket}")
    private String bucketName;

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public ResumeResponseDTO uploadResume(MultipartFile file, UUID userId) throws IOException {
        validateFile(file);

        try {
            // Generate unique filename
            String fileExtension = getFileExtension(file.getOriginalFilename());
            String uniqueFilename = userId + "/" + UUID.randomUUID() + fileExtension;

            // Upload to Supabase Storage
            String fileUrl = storageService.uploadFile(bucketName, uniqueFilename, file);

            // Save metadata to database
            Resume resume = new Resume();
            resume.setUserId(userId);
            resume.setFilename(uniqueFilename);
            resume.setOriginalFilename(file.getOriginalFilename());
            resume.setFileSize(file.getSize());
            resume.setMimeType(file.getContentType());
            resume.setFileUrl(fileUrl);
            resume.setParsingStatus("pending");
            resume.setIsActive(true);
            resume.setParsedData(null); // Explicitly set to null for now

            Resume savedResume = resumeRepository.save(resume);

            log.info("Resume uploaded successfully: {}", savedResume.getId());
            return mapToResponseDTO(savedResume);

        } catch (Exception e) {
            log.error("Error uploading resume for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to upload resume: " + e.getMessage());
        }
    }

    public List<ResumeResponseDTO> getUserResumes(UUID userId) {
        List<Resume> resumes = resumeRepository.findActiveResumesByUserIdOrderByDate(userId);
        return resumes.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public Page<ResumeResponseDTO> getUserResumes(UUID userId, Pageable pageable) {
        Page<Resume> resumes = resumeRepository.findByUserIdAndIsActiveTrue(userId, pageable);
        return resumes.map(this::mapToResponseDTO);
    }

    public ResumeResponseDTO getResumeById(UUID resumeId, UUID userId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));

        if (!resume.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        return mapToResponseDTO(resume);
    }

    public void deleteResume(UUID resumeId, UUID userId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));

        if (!resume.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        // Soft delete
        resume.setIsActive(false);
        resumeRepository.save(resume);

        // TODO: Delete from storage (optional)
        // storageService.deleteFile(bucketName, resume.getFilename());

        log.info("Resume deleted: {}", resumeId);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 10MB");
        }

        if (!ALLOWED_MIME_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Invalid file type. Only PDF, DOC, and DOCX files are allowed");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        return lastDotIndex >= 0 ? filename.substring(lastDotIndex) : "";
    }

    private ResumeResponseDTO mapToResponseDTO(Resume resume) {
        return new ResumeResponseDTO(
                resume.getId(),
                resume.getOriginalFilename(),
                resume.getFileSize(),
                resume.getMimeType(),
                resume.getUploadDate(),
                resume.getParsingStatus(),
                resume.getFileUrl()
        );
    }
}