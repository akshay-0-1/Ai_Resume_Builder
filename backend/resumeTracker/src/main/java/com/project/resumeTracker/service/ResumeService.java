package com.project.resumeTracker.service;

import com.project.resumeTracker.dto.EducationDTO;
import com.project.resumeTracker.dto.PersonalDetailsDTO;
import com.project.resumeTracker.dto.ResumeInfoDTO;
import com.project.resumeTracker.dto.ResumeResponseDTO;
import com.project.resumeTracker.dto.SkillDTO;
import com.project.resumeTracker.dto.WorkExperienceDTO;
import com.project.resumeTracker.dto.ResumeUpdateDTO;
import com.project.resumeTracker.dto.Project;
import com.project.resumeTracker.dto.Certificate;
import com.project.resumeTracker.entity.Education;
import com.project.resumeTracker.entity.PersonalDetails;
import com.project.resumeTracker.entity.Resume;
import com.project.resumeTracker.entity.Skill;
import com.project.resumeTracker.entity.WorkExperience;
import com.project.resumeTracker.repository.ResumeRepository;
import com.project.resumeTracker.util.Constants;
import com.project.resumeTracker.util.ResumeUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.project.resumeTracker.client.LatexApiClient;
import com.project.resumeTracker.dto.ResumeStatusDTO;
import org.springframework.mock.web.MockMultipartFile;
import com.project.resumeTracker.service.ResumeProcessingTask;
import org.springframework.context.annotation.Lazy;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeDataService resumeDataService;
    private final DocumentParsingFacade documentParsingFacade;
    private final LatexTemplateService latexTemplateService;
    private final LatexApiClient latexApiClient;
    private final ResumeProcessingTask processingTask;

    private static final Set<String> ALLOWED_MIME_TYPES = Constants.ALLOWED_MIME_TYPES;
    private static final long MAX_FILE_SIZE = Constants.MAX_FILE_SIZE;

    /**
     * New async upload entry point. Saves the resume metadata immediately and returns, while heavy
     * parsing/latex work is executed in a background task.
     */
    public ResumeResponseDTO enqueueUpload(MultipartFile file, Long userId) throws IOException {
        log.info("Queueing resume upload for async processing user {}", userId);
        validateFile(file);

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
        resume.setParsingStatus("QUEUED");

        Resume saved = resumeRepository.save(resume);
        processingTask.processResume(saved.getId());
        return convertToResponseDTO(saved);
    }

    /**
     * This method is invoked asynchronously by {@link ResumeAsyncProcessor}.
     */
    @Transactional
    public void processResume(UUID resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException(Constants.ERROR_RESUME_NOT_FOUND));
        log.info("[ASYNC] Processing resume {} for user {}", resumeId, resume.getUserId());
        try {
            // Recreate MultipartFile for existing parsers
            org.springframework.mock.web.MockMultipartFile mf = new org.springframework.mock.web.MockMultipartFile(
                    resume.getOriginalFilename(), resume.getOriginalFilename(), resume.getMimeType(), resume.getFileData());

            String rawText = documentParsingFacade.parseDocument(mf);
            resume.setRawText(rawText);
            resumeDataService.parseAndEnrichResume(rawText, resume);
            log.info("[ASYNC] Parsing completed for {}", resumeId);

            generateResumeContent(resume);
            resume.setParsingStatus("COMPLETED");
        } catch (Exception ex) {
            log.error("[ASYNC] Failed to fully process resume {}: {}", resumeId, ex.getMessage(), ex);
            resume.setParsingStatus("FAILED");
        }
        resumeRepository.save(resume);
    }

    public ResumeStatusDTO getResumeStatus(UUID resumeId, Long userId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException(Constants.ERROR_RESUME_NOT_FOUND));
        if (!resume.getUserId().equals(userId)) {
            throw new SecurityException(Constants.ERROR_ACCESS_DENIED);
        }
        return new ResumeStatusDTO(resume.getId(), resume.getParsingStatus(), resume.getErrorMessage());
    }

    // Existing synchronous method retained for internal use
    public ResumeResponseDTO uploadResumeSync(MultipartFile file, Long userId) throws IOException {
        log.info("[SYNC] Starting resume upload process for user {}", userId);
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
                .orElseThrow(() -> new RuntimeException(Constants.ERROR_RESUME_NOT_FOUND));

        if (!resume.getUserId().equals(userId)) {
            throw new SecurityException(Constants.ERROR_ACCESS_DENIED);
        }
        Hibernate.initialize(resume.getProjects());
        Hibernate.initialize(resume.getCertificates());
        return convertToResponseDTO(resume);
    }

    public Resume getResumeFileById(UUID resumeId, Long userId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException(Constants.ERROR_RESUME_NOT_FOUND));

        if (!resume.getUserId().equals(userId)) {
            throw new SecurityException(Constants.ERROR_ACCESS_DENIED);
        }

        // If previous attempt failed, propagate the failure message
        if ("FAILED".equalsIgnoreCase(resume.getParsingStatus())) {
            throw new com.project.resumeTracker.exception.LatexCompilationException(
                    resume.getErrorMessage() != null ? resume.getErrorMessage() : "LaTeX compilation failed.");
        }

        Hibernate.initialize(resume.getWorkExperiences());
        Hibernate.initialize(resume.getEducations());
        Hibernate.initialize(resume.getSkills());
        Hibernate.initialize(resume.getProjects());
        Hibernate.initialize(resume.getCertificates());

        // If PDF already exists, return it
        if (resume.getLatexPdf() != null && resume.getLatexPdf().length > 0) {
            return resume;
        }

        // PDF not ready yet – let controller decide how to respond (usually 202 Accepted).
        return resume;
    }

    public void deleteResume(UUID resumeId, Long userId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException(Constants.ERROR_RESUME_NOT_FOUND));

        if (!resume.getUserId().equals(userId)) {
            throw new SecurityException(Constants.ERROR_ACCESS_DENIED);
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
    private byte[] generatePdfFromLatex(String latexContent) throws IOException {
        log.info("Sending LaTeX content to compiler service.");
        byte[] pdfBytes = latexApiClient.compileLatex(latexContent);
        log.info("Successfully compiled LaTeX to PDF.");
        return pdfBytes;
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
        resume.setLatexPdf(latexPdf);
        resume.setMimeType("application/pdf");
        resume.setParsingStatus("PDF_GENERATED");
        
        return resume;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException(Constants.ERROR_FILE_EMPTY);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(Constants.ERROR_FILE_TOO_LARGE);
        }
        if (!ALLOWED_MIME_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException(Constants.ERROR_INVALID_FILE_TYPE);
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
        if (resume.getProjects() != null) {
            dto.setProjects(resume.getProjects().stream().map(this::mapProjectToDTO).collect(Collectors.toList()));
        }
        if (resume.getCertificates() != null) {
            dto.setCertificates(resume.getCertificates().stream().map(this::mapCertificateToDTO).collect(Collectors.toList()));
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

    private Project mapProjectToDTO(com.project.resumeTracker.entity.Project projectEntity) {
        if (projectEntity == null) return null;
        Project dto = new Project();
        dto.setName(projectEntity.getName());
        dto.setTechStack(projectEntity.getTechStack());
        dto.setDate(projectEntity.getDate());
        dto.setAchievements(projectEntity.getAchievements());
        return dto;
    }

    @Transactional
    public ResumeResponseDTO updateResume(UUID resumeId, Long userId, ResumeUpdateDTO updateDTO) {
        return updateResumeData(resumeId, userId, updateDTO);
    }

    private ResumeResponseDTO updateResumeData(UUID resumeId, Long userId, ResumeUpdateDTO updateDTO) {
        log.info("Starting resume update for id: {} by user {}", resumeId, userId);
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException(Constants.ERROR_RESUME_NOT_FOUND));
        if (!resume.getUserId().equals(userId)) {
            throw new SecurityException(Constants.ERROR_ACCESS_DENIED);
        }

        // --- Personal details ---
        if (updateDTO.getPersonalDetails() != null) {
            PersonalDetailsDTO pdDto = updateDTO.getPersonalDetails();
            com.project.resumeTracker.entity.PersonalDetails entity = resume.getPersonalDetails();
            if (entity == null) {
                entity = new com.project.resumeTracker.entity.PersonalDetails();
            }
            entity.setName(pdDto.getName());
            entity.setEmail(pdDto.getEmail());
            entity.setPhone(pdDto.getPhone());
            entity.setAddress(pdDto.getAddress());
            entity.setLinkedinUrl(pdDto.getLinkedinUrl());
            entity.setGithubUrl(pdDto.getGithubUrl());
            entity.setPortfolioUrl(pdDto.getPortfolioUrl());
            entity.setSummary(pdDto.getSummary());
            resume.setPersonalDetails(entity);
        }

        // --- Collections ---
        List<WorkExperienceDTO> weDtos = updateDTO.getWorkExperiences();
        if (weDtos != null && !weDtos.isEmpty()) {
            resume.getWorkExperiences().clear();
            for (WorkExperienceDTO weDto : weDtos) {
                WorkExperience we = new WorkExperience();
                we.setJobTitle(weDto.getJobTitle());
                we.setCompanyName(weDto.getCompanyName());
                we.setLocation(weDto.getLocation());
                we.setStartDate(weDto.getStartDate());
                we.setEndDate(weDto.getEndDate());
                we.setCurrentJob(Boolean.TRUE.equals(weDto.getCurrentJob()));
                we.setDescription(weDto.getDescription());
                resume.addWorkExperience(we);
            }
        }

        List<EducationDTO> edDtos = updateDTO.getEducations();
        if (edDtos != null && !edDtos.isEmpty()) {
            resume.getEducations().clear();
            for (EducationDTO edDto : edDtos) {
                Education ed = new Education();
                ed.setInstitutionName(edDto.getInstitutionName());
                ed.setDegree(edDto.getDegree());
                ed.setFieldOfStudy(edDto.getFieldOfStudy());
                ed.setStartDate(edDto.getStartDate());
                ed.setEndDate(edDto.getEndDate());
                ed.setGrade(edDto.getGrade());
                ed.setDescription(edDto.getDescription());
                resume.addEducation(ed);
            }
        }

        List<SkillDTO> skillDtos = updateDTO.getSkills();
        if (skillDtos != null && !skillDtos.isEmpty()) {
            resume.getSkills().clear();
            for (SkillDTO sDto : skillDtos) {
                Skill skill = new Skill();
                skill.setSkillName(sDto.getSkillName());
                skill.setProficiencyLevel(sDto.getProficiencyLevel());
                resume.addSkill(skill);
            }
        }

        List<Project> projDtos = updateDTO.getProjects();
        if (projDtos != null && !projDtos.isEmpty()) {
            resume.getProjects().clear();
            for (Project pDto : projDtos) {
                com.project.resumeTracker.entity.Project p = new com.project.resumeTracker.entity.Project();
                p.setName(pDto.getName());
                p.setTechStack(pDto.getTechStack());
                p.setDate(pDto.getDate());
                p.setAchievements(pDto.getAchievements());
                resume.addProject(p);
            }
        }

        List<Certificate> certDtos = updateDTO.getCertificates();
        if (certDtos != null && !certDtos.isEmpty()) {
            resume.getCertificates().clear();
            for (Certificate cDto : certDtos) {
                com.project.resumeTracker.entity.Certificate c = new com.project.resumeTracker.entity.Certificate();
                c.setName(cDto.getName());
                if (cDto.getDate() != null && !cDto.getDate().isEmpty()) {
                    c.setDate(java.time.LocalDate.parse(cDto.getDate()));
                }
                c.setInstitution(cDto.getInstitution());
                c.setUrl(cDto.getUrl());
                resume.addCertificate(c);
            }
        }

        resumeRepository.save(resume);
        log.info("Saved updated resume {}. Regenerating LaTeX/PDF", resumeId);
        try {
            generateResumeContent(resume);
        } catch (IOException e) {
            log.error("Failed to regenerate content after update", e);
            throw new RuntimeException(Constants.ERROR_PDF_GENERATION_FAILED, e);
        }
        return convertToResponseDTO(resume);
    }

    private SkillDTO mapSkillToDTO(Skill skill) {
        if (skill == null) return null;
        return new SkillDTO(skill.getSkillName(), skill.getProficiencyLevel());
    }

    private Certificate mapCertificateToDTO(com.project.resumeTracker.entity.Certificate certEntity) {
        if (certEntity == null) return null;
        Certificate dto = new Certificate();
        dto.setName(certEntity.getName());
        dto.setDate(certEntity.getDate() != null ? certEntity.getDate().toString() : null);
        dto.setInstitution(certEntity.getInstitution());
        dto.setUrl(certEntity.getUrl());
        return dto;
    }


}