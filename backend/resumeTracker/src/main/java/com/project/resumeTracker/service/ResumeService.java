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
import com.project.resumeTracker.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeDataService resumeDataService;
    private final DocumentParsingFacade documentParsingFacade;

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

    public List<ResumeInfoDTO> getUserResumes(UUID userId) {
        return resumeRepository.findResumeInfoByUserId(userId);
    }

    public Page<ResumeInfoDTO> getUserResumes(UUID userId, Pageable pageable) {
        return resumeRepository.findResumeInfoByUserId(userId, pageable);
    }

    public ResumeResponseDTO getResumeById(UUID resumeId, UUID userId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found."));

        if (!resume.getUserId().equals(userId)) {
            throw new SecurityException("Access denied.");
        }
        return convertToResponseDTO(resume);
    }

    public Resume getResumeFileById(UUID resumeId, UUID userId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found."));

        if (!resume.getUserId().equals(userId)) {
            throw new SecurityException("Access denied.");
        }
        return resume;
    }

    public void deleteResume(UUID resumeId, UUID userId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found."));

        if (!resume.getUserId().equals(userId)) {
            throw new SecurityException("Access denied.");
        }

        resume.setIsActive(false);
        resumeRepository.save(resume);
        log.info("Resume deleted: {}", resumeId);
    }

    @Transactional
    public ResumeResponseDTO updateResumeContent(UUID resumeId, UUID userId, String htmlContent) throws Exception {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found."));

        if (!resume.getUserId().equals(userId)) {
            throw new SecurityException("Access denied.");
        }

        // Clean and parse the HTML to ensure it's well-formed
        Document jsoupDoc = Jsoup.parse(htmlContent);
        jsoupDoc.outputSettings().syntax(OutputSettings.Syntax.xml);
        String cleanHtml = jsoupDoc.html();

        byte[] updatedFileData;
        String mimeType = resume.getMimeType();

        if ("application/pdf".equals(mimeType)) {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(cleanHtml, null);
                builder.toStream(os);
                builder.run();
                updatedFileData = os.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException("Failed to generate PDF from HTML", e);
            }
        } else if ("application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(mimeType)) {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
                MainDocumentPart mainDocumentPart = wordMLPackage.getMainDocumentPart();
                XHTMLImporterImpl xhtmlImporter = new XHTMLImporterImpl(wordMLPackage);
                mainDocumentPart.getContent().addAll(xhtmlImporter.convert(cleanHtml, null));
                wordMLPackage.save(os);
                updatedFileData = os.toByteArray();
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate DOCX from HTML", e);
            }
        } else {
            throw new IllegalArgumentException("Unsupported file type for editing: " + mimeType);
        }

        resume.setFileData(updatedFileData);
        // Optionally, re-parse the text content if needed elsewhere
        // resume.setResumeContent(parseTextFromHtml(htmlContent));
        resume.setUploadDate(LocalDateTime.now()); // Update timestamp

        Resume updatedResume = resumeRepository.save(resume);
        return convertToResponseDTO(updatedResume);
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