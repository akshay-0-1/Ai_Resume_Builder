package com.project.resumeTracker.service;

import com.project.resumeTracker.dto.EducationDTO;
import com.project.resumeTracker.dto.PersonalDetailsDTO;
import com.project.resumeTracker.dto.ResumeInfoDTO;
import com.project.resumeTracker.dto.ResumeResponseDTO;
import com.project.resumeTracker.dto.SkillDTO;
import com.project.resumeTracker.dto.WorkExperienceDTO;
import com.project.resumeTracker.entity.Education;
import com.project.resumeTracker.entity.PersonalDetails;
import com.project.resumeTracker.entity.Resume;
import com.project.resumeTracker.entity.Skill;
import com.project.resumeTracker.entity.WorkExperience;
import com.project.resumeTracker.entity.Certificate;
import com.project.resumeTracker.entity.Project;
import com.project.resumeTracker.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import com.project.resumeTracker.client.LatexApiClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeDataService resumeDataService;
    private final DocumentParsingFacade documentParsingFacade;
    private final LatexTemplateService latexTemplateService;
    private final LatexApiClient latexApiClient;

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public ResumeResponseDTO uploadResume(MultipartFile file, Long userId) throws IOException {
        log.info("Starting resume upload process for user {}", userId);
        validateFile(file);

        Resume savedResume = null;
        try {
            String fileExtension = getFileExtension(file.getOriginalFilename());
            String uniqueFilename = userId + "/" + UUID.randomUUID() + fileExtension;

            Resume resume = new Resume();
            resume.setUserId(userId);
            resume.setFilename(uniqueFilename);
            resume.setOriginalFilename(file.getOriginalFilename());
            resume.setFileSize(file.getSize());
            resume.setMimeType(file.getContentType());
            resume.setFileData(file.getBytes());
            resume.setIsActive(true);

            savedResume = resumeRepository.save(resume);
            log.info("Resume metadata saved: {}. Attempting to parse.", savedResume.getId());

            String rawText = documentParsingFacade.parseDocument(file);
            savedResume.setRawText(rawText);
            savedResume = resumeDataService.parseAndEnrichResume(rawText, savedResume);
            log.info("Resume parsing completed for: {}. Status: {}", savedResume.getId(), savedResume.getParsingStatus());

            // Generate both HTML and LaTeX versions after parsing
            savedResume = generateResumeContent(savedResume);

            return convertToResponseDTO(savedResume);

        } catch (IOException e) {
            log.error("IO Error during resume processing for user {}: {}", userId, e.getMessage(), e);
            if (savedResume != null && "PENDING".equals(savedResume.getParsingStatus())) {
                savedResume.setParsingStatus("FAILED_UPLOAD_OR_INITIAL_SAVE");
                resumeRepository.save(savedResume);
            }
            throw e;
        } catch (Exception e) {
            log.error("General Error uploading/parsing resume for user {}: {}", userId, e.getMessage(), e);
            if (savedResume != null) {
                if ("PENDING".equals(savedResume.getParsingStatus())) {
                    savedResume.setParsingStatus("FAILED");
                    resumeRepository.save(savedResume);
                }
                // Even on failure, return the DTO so the frontend gets the ID
                return convertToResponseDTO(savedResume);
            } else {
                // If resume was never saved, we can't return a DTO. We must throw.
                throw new RuntimeException("Failed to save resume before parsing: " + e.getMessage(), e);
            }
        }
    }

    public List<ResumeInfoDTO> getUserResumes(Long userId) {
        return resumeRepository.findResumeInfoByUserId(userId);
    }

    public Page<ResumeInfoDTO> getUserResumes(Long userId, Pageable pageable) {
        return resumeRepository.findResumeInfoByUserId(userId, pageable);
    }

    public ResumeResponseDTO getResumeById(UUID resumeId, Long userId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found."));

        if (!resume.getUserId().equals(userId)) {
            throw new SecurityException("Access denied.");
        }
        return convertToResponseDTO(resume);
    }

    public Resume getResumeFileById(UUID resumeId, Long userId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found."));

        if (!resume.getUserId().equals(userId)) {
            throw new SecurityException("Access denied.");
        }
        Hibernate.initialize(resume.getWorkExperiences());
        Hibernate.initialize(resume.getEducations());
        Hibernate.initialize(resume.getSkills());

        try {
            // Generate LaTeX content and PDF
            String latexContent = generateLatexResumeContent(resume);
            byte[] pdfData = generatePdfFromLatex(latexContent);
            resume.setFileData(pdfData);
            resume.setLatexContent(latexContent);
            resume.setMimeType("application/pdf");
        } catch (Exception e) {
            log.error("Failed to generate PDF for resumeId: {}", resumeId, e);
            throw new RuntimeException("Failed to generate PDF file.", e);
        }

        return resume;
    }

    public void deleteResume(UUID resumeId, Long userId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found."));

        if (!resume.getUserId().equals(userId)) {
            throw new SecurityException("Access denied.");
        }

        resume.setIsActive(false);
        resumeRepository.save(resume);
        log.info("Resume deleted: {}", resumeId);
    }

    /**
     * Generate LaTeX resume content from resume data
     */
    private String generateLatexResumeContent(Resume resume) throws IOException {
        log.info("Generating LaTeX content for resumeId: {} using LatexTemplateService", resume.getId());

        // 1. Get the template content
        String template = latexTemplateService.getTemplateContent();

        // 2. Prepare the data map using the service
        java.util.Map<String, String> data = latexTemplateService.prepareDataForTemplate(resume);

        // 3. Replace placeholders and return the final content
        return latexTemplateService.replacePlaceholders(template, data);
    }

    /**
     * Generate PDF from LaTeX content
     */
    private byte[] generatePdfFromLatex(String latexContent) {
        try {
            log.info("Sending LaTeX content to compiler service.");
            byte[] pdfBytes = latexApiClient.compileLatex(latexContent);
            log.info("Successfully compiled LaTeX to PDF.");
            return pdfBytes;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF from LaTeX", e);
        }
    }

    /**
     * Generate LaTeX content and PDF from resume data
     */
    private Resume generateResumeContent(Resume resume) throws IOException {
        log.info("Generating all resume content (LaTeX and PDF) for resumeId: {}", resume.getId());
        // Generate LaTeX content
        String latexContent = generateLatexResumeContent(resume);
        byte[] latexPdf = generatePdfFromLatex(latexContent);
        
        // Store LaTeX version
        resume.setLatexContent(latexContent);
        resume.setFileData(latexPdf);
        resume.setMimeType("application/pdf");
        
        return resume;
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

    private ResumeResponseDTO convertToResponseDTO(Resume resume) {
        if (resume == null) return null;
        ResumeResponseDTO dto = new ResumeResponseDTO();
        dto.setId(resume.getId());
        dto.setOriginalFilename(resume.getOriginalFilename());
        dto.setFileSize(resume.getFileSize());
        dto.setMimeType(resume.getMimeType());
        dto.setUploadDate(resume.getUploadDate());
        dto.setParsingStatus(resume.getParsingStatus());
        dto.setResumeContent(resume.getRawText());

        if (resume.getPersonalDetails() != null) {
            dto.setPersonalDetails(mapPersonalDetailsToDTO(resume.getPersonalDetails()));
        }
        if (resume.getWorkExperiences() != null) {
            dto.setWorkExperiences(resume.getWorkExperiences().stream().map(this::mapWorkExperienceToDTO).collect(Collectors.toList()));
        }
        if (resume.getEducations() != null) {
            dto.setEducations(resume.getEducations().stream().map(this::mapEducationToDTO).collect(Collectors.toList()));
        }
        if (resume.getSkills() != null) {
            dto.setSkills(resume.getSkills().stream().map(this::mapSkillToDTO).collect(Collectors.toList()));
        }
        return dto;
    }

    private PersonalDetailsDTO mapPersonalDetailsToDTO(PersonalDetails personalDetails) {
        if (personalDetails == null) return null;
        return new PersonalDetailsDTO(personalDetails.getName(), personalDetails.getEmail(), personalDetails.getPhone(), personalDetails.getAddress(), personalDetails.getLinkedinUrl(), personalDetails.getGithubUrl(), personalDetails.getPortfolioUrl(), personalDetails.getSummary());
    }

    private WorkExperienceDTO mapWorkExperienceToDTO(WorkExperience workExperience) {
        if (workExperience == null) return null;
        return new WorkExperienceDTO(workExperience.getJobTitle(), workExperience.getCompanyName(), workExperience.getLocation(), workExperience.getStartDate(), workExperience.getEndDate(), workExperience.isCurrentJob(), workExperience.getDescription());
    }

    private EducationDTO mapEducationToDTO(Education education) {
        if (education == null) return null;
        return new EducationDTO(education.getInstitutionName(), education.getDegree(), education.getFieldOfStudy(), education.getStartDate(), education.getEndDate(), education.getGrade(), education.getDescription());
    }

    private SkillDTO mapSkillToDTO(Skill skill) {
        if (skill == null) return null;
        return new SkillDTO(skill.getSkillName(), skill.getProficiencyLevel());
    }


}