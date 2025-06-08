package com.project.resumeTracker.service;

import com.project.resumeTracker.dto.ResumeResponseDTO;
import com.project.resumeTracker.entity.Resume;
import com.project.resumeTracker.repository.ResumeRepository;
import com.project.resumeTracker.dto.*;
import com.project.resumeTracker.entity.*;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
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
    private final ResumeDataService resumeDataService;

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

        Resume savedResume = null;
        try {
            // Generate unique filename
            String fileExtension = getFileExtension(file.getOriginalFilename());
            String uniqueFilename = userId + "/" + UUID.randomUUID() + fileExtension;

            // Upload to Supabase Storage
            String fileUrl = storageService.uploadFile(bucketName, uniqueFilename, file);

            // Save initial metadata to database
            Resume resume = new Resume();
            resume.setUserId(userId);
            resume.setFilename(uniqueFilename);
            resume.setOriginalFilename(file.getOriginalFilename());
            resume.setFileSize(file.getSize());
            resume.setMimeType(file.getContentType());
            resume.setFileUrl(fileUrl);
            resume.setParsingStatus("PENDING"); // Initial status
            resume.setIsActive(true);

            savedResume = resumeRepository.save(resume);
            log.info("Resume metadata saved: {}. Attempting to parse.", savedResume.getId());

            // Call ResumeDataService to parse and enrich the resume
            savedResume = resumeDataService.parseAndEnrichResume(file, savedResume);
            log.info("Resume parsing completed for: {}. Status: {}", savedResume.getId(), savedResume.getParsingStatus());

            return mapToResponseDTO(savedResume);

        } catch (IOException e) {
            log.error("IO Error during resume processing for user {}: {}", userId, e.getMessage(), e);
            if (savedResume != null && "PENDING".equals(savedResume.getParsingStatus())) {
                savedResume.setParsingStatus("FAILED_UPLOAD_OR_INITIAL_SAVE");
                resumeRepository.save(savedResume);
            }
            throw e;
        } catch (Exception e) {
            log.error("General Error uploading/parsing resume for user {}: {}", userId, e.getMessage(), e);
            if (savedResume != null && ("PENDING".equals(savedResume.getParsingStatus()) || savedResume.getParsingStatus() == null)) {
                savedResume.setParsingStatus("ERROR");
                resumeRepository.save(savedResume);
            }
            throw new RuntimeException("Failed to upload and parse resume: " + e.getMessage(), e);
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

    private PersonalDetailsDTO mapPersonalDetailsToDTO(PersonalDetails personalDetails) {
        if (personalDetails == null) return null;
        return new PersonalDetailsDTO(
                personalDetails.getName(),
                personalDetails.getEmail(),
                personalDetails.getPhone(),
                personalDetails.getAddress(),
                personalDetails.getLinkedinUrl(),
                personalDetails.getGithubUrl(),
                personalDetails.getPortfolioUrl(),
                personalDetails.getSummary()
        );
    }

    private WorkExperienceDTO mapWorkExperienceToDTO(WorkExperience workExperience) {
        if (workExperience == null) return null;
        return new WorkExperienceDTO(
                workExperience.getJobTitle(),
                workExperience.getCompanyName(),
                workExperience.getLocation(),
                workExperience.getStartDate(),
                workExperience.getEndDate(),
                workExperience  .isCurrentJob(),
                workExperience.getDescription()
        );
    }

    private EducationDTO mapEducationToDTO(Education education) {
        if (education == null) return null;
        return new EducationDTO(
                education.getInstitutionName(),
                education.getDegree(),
                education.getFieldOfStudy(),
                education.getStartDate(),
                education.getEndDate(),
                education.getGrade(),
                education.getDescription()
        );
    }

    private SkillDTO mapSkillToDTO(Skill skill) {
        if (skill == null) return null;
        return new SkillDTO(
                skill.getSkillName(),
                skill.getProficiencyLevel()
        );
    }

    private ResumeResponseDTO mapToResponseDTO(Resume resume) {
        if (resume == null) return null;

        PersonalDetailsDTO personalDetailsDTO = mapPersonalDetailsToDTO(resume.getPersonalDetails());

        List<WorkExperienceDTO> workExperienceDTOs =
                resume.getWorkExperiences() != null ? resume.getWorkExperiences().stream()
                        .map(this::mapWorkExperienceToDTO)
                        .collect(Collectors.toList()) : Collections.emptyList();

        List<EducationDTO> educationDTOs =
                resume.getEducations() != null ? resume.getEducations().stream()
                        .map(this::mapEducationToDTO)
                        .collect(Collectors.toList()) : Collections.emptyList();

        List<SkillDTO> skillDTOs =
                resume.getSkills() != null ? resume.getSkills().stream()
                        .map(this::mapSkillToDTO)
                        .collect(Collectors.toList()) : Collections.emptyList();

        return new ResumeResponseDTO(
                resume.getId(),
                resume.getOriginalFilename(),
                resume.getFileSize(),
                resume.getMimeType(),
                resume.getUploadDate(),
                resume.getParsingStatus(),
                resume.getFileUrl(),
                personalDetailsDTO,
                workExperienceDTOs,
                educationDTOs,
                skillDTOs
        );
    }
}