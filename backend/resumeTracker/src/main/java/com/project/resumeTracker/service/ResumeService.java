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
import org.hibernate.Hibernate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
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

    public ResumeResponseDTO uploadResume(MultipartFile file, Long userId) throws IOException {
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

        String html;
        // Prioritize the user's saved HTML content if it exists.
        if (resume.getHtmlContent() != null && !resume.getHtmlContent().trim().isEmpty()) {
            html = resume.getHtmlContent();
        } else {
            // Fallback to building from structured data if no edits have been saved.
            html = buildResumeHtmlFromData(resume);
        }

        try {
            byte[] pdfData = generatePdfFromHtml(html);
            resume.setFileData(pdfData);
            resume.setMimeType("application/pdf");
        } catch (IOException e) {
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

    @Transactional
    public ResumeResponseDTO updateResumeContent(UUID resumeId, Long userId, String htmlContent) throws Exception {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found."));

        if (!resume.getUserId().equals(userId)) {
            throw new SecurityException("Access denied.");
        }

        // Parse the incoming HTML to extract plain text for keyword searching, etc.
        Document jsoupDoc = Jsoup.parse(htmlContent);
        String updatedRawText = jsoupDoc.text();

        // Save the raw text and the full HTML content from the editor.
        resume.setRawText(updatedRawText);
        resume.setHtmlContent(htmlContent);

        // Save the updated resume. This is now a fast operation.
        resumeRepository.save(resume);

        // Return a lightweight response.
        return new ResumeResponseDTO(resumeId, resume.getOriginalFilename());
    }

    private String buildResumeHtmlFromData(Resume resume) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");

        sb.append("<div class='resume-container'>");

        // Header
        PersonalDetails details = resume.getPersonalDetails();
        if (details != null) {
            sb.append("<div class='header'>");
            String name = details.getName() != null ? details.getName().trim() : "";
            sb.append("<h1>").append(escapeHtml(name)).append("</h1>");
            sb.append("<div class='contact-info'>");

            String email = details.getEmail() != null ? details.getEmail().trim() : "";
            if (!email.isEmpty()) sb.append(escapeHtml(email));

            String phone = details.getPhone() != null ? details.getPhone().trim() : "";
            if (!phone.isEmpty()) sb.append("<span> | </span>").append(escapeHtml(phone));

            String linkedin = details.getLinkedinUrl() != null ? details.getLinkedinUrl().trim() : "";
            if (!linkedin.isEmpty()) sb.append("<span> | </span><a href='").append(escapeHtml(linkedin)).append("'>LinkedIn</a>");

            String github = details.getGithubUrl() != null ? details.getGithubUrl().trim() : "";
            if (!github.isEmpty()) sb.append("<span> | </span><a href='").append(escapeHtml(github)).append("'>GitHub</a>");

            sb.append("</div></div>");
        }

        // Work Experience
        if (resume.getWorkExperiences() != null && !resume.getWorkExperiences().isEmpty()) {
            sb.append("<div class='section'><h2 class='section-title'>Experience</h2>");
            resume.getWorkExperiences().stream()
                  .sorted(Comparator.comparing(WorkExperience::getStartDate, Comparator.nullsLast(Comparator.reverseOrder())))
                  .forEach(exp -> {
                      sb.append("<div class='entry'>");

                      String startDateStr = exp.getStartDate() != null ? exp.getStartDate().format(formatter) : "";
                      String endDateStr = exp.getEndDate() != null ? exp.getEndDate().format(formatter) : "Present";
                      sb.append("<span class='date'>").append(startDateStr).append(" - ").append(endDateStr).append("</span>");

                      String jobTitle = exp.getJobTitle() != null ? exp.getJobTitle().trim() : "";
                      sb.append("<div class='title'>").append(escapeHtml(jobTitle)).append("</div>");

                      String companyName = exp.getCompanyName() != null ? exp.getCompanyName().trim() : "";
                      sb.append("<div class='company'>").append(escapeHtml(companyName)).append("</div>");

                      String description = exp.getDescription();
                      if (description != null && !description.trim().isEmpty()) {
                          sb.append("<div class='description'><ul>");
                          for (String line : description.split("\\n")) {
                              if (!line.trim().isEmpty()) {
                                  sb.append("<li>").append(escapeHtml(line.trim())).append("</li>");
                              }
                          }
                          sb.append("</ul></div>");
                      }
                      sb.append("</div>");
                  });
            sb.append("</div>");
        }

        // Education
        if (resume.getEducations() != null && !resume.getEducations().isEmpty()) {
            sb.append("<div class='section'><h2 class='section-title'>Education</h2>");
            resume.getEducations().stream()
                .sorted(Comparator.comparing(Education::getEndDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .forEach(edu -> {
                    sb.append("<div class='entry'>");
                    String gradDateStr = edu.getEndDate() != null ? edu.getEndDate().format(formatter) : "";
                    sb.append("<span class='date'>").append(gradDateStr).append("</span>");

                    String degree = edu.getDegree() != null ? edu.getDegree().trim() : "";
                    sb.append("<div class='title'>").append(escapeHtml(degree)).append("</div>");

                    String institutionName = edu.getInstitutionName() != null ? edu.getInstitutionName().trim() : "";
                    sb.append("<div class='institution'>").append(escapeHtml(institutionName)).append("</div>");
                    sb.append("</div>");
                });
            sb.append("</div>");
        }

        // Skills
        if (resume.getSkills() != null && !resume.getSkills().isEmpty()) {
            sb.append("<div class='section'><h2 class='section-title'>Skills</h2>");
            sb.append("<ul class='skills-list'>");
            resume.getSkills().forEach(skill -> {
                String skillName = skill.getSkillName() != null ? skill.getSkillName().trim() : "";
                if (!skillName.isEmpty()) {
                    sb.append("<li>").append(escapeHtml(skillName)).append("</li>");
                }
            });
            sb.append("</ul></div>");
        }

        sb.append("</div>");
        return sb.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
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
        dto.setHtmlContent(resume.getHtmlContent());

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

    private byte[] generatePdfFromHtml(String html) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();

            // Load CSS
            org.springframework.core.io.Resource cssResource = new ClassPathResource("static/css/default-resume-style.css");
            String css = new String(Files.readAllBytes(Paths.get(cssResource.getURI())));

            // The HTML needs to be a full document for the renderer
            String fullHtml = "<!DOCTYPE html><html><head><style>" + css + "</style></head><body>" + html + "</body></html>";

            builder.withHtmlContent(fullHtml, null);
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        }
    }
}