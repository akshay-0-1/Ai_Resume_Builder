package com.project.resumeTracker.service;

import com.project.resumeTracker.entity.Resume;
import com.project.resumeTracker.repository.ResumeRepository;
import com.project.resumeTracker.service.ResumeDataService;
import com.project.resumeTracker.service.DocumentParsingFacade;
import com.project.resumeTracker.service.LatexTemplateService;
import com.project.resumeTracker.client.LatexApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.Map;
import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class ResumeProcessingTask {
    private final ResumeRepository resumeRepository;
    private final ResumeDataService resumeDataService;
    private final DocumentParsingFacade documentParsingFacade;
    private final LatexTemplateService latexTemplateService;
    private final LatexApiClient latexApiClient;

    @Async
    @Transactional
    public void processResume(UUID resumeId) {
        Resume resume = null;
        try {
            resume = resumeRepository.findById(resumeId)
                    .orElseThrow(() -> new RuntimeException("Resume not found with ID: " + resumeId));
            
            if (resume.getFileData() == null || resume.getFileData().length == 0) {
                throw new IllegalStateException("Resume file data is missing or empty");
            }
            
            log.info("[ASYNC] Processing resume {} for user {}", resumeId, resume.getUserId());
            
            // Recreate MultipartFile for existing parsers
            org.springframework.mock.web.MockMultipartFile mf = new org.springframework.mock.web.MockMultipartFile(
                    resume.getOriginalFilename(), resume.getOriginalFilename(), resume.getMimeType(), resume.getFileData());

            String rawText = documentParsingFacade.parseDocument(mf);
            if (rawText == null || rawText.isEmpty()) {
                throw new IllegalStateException("Failed to extract text from resume file");
            }
            
            resume.setRawText(rawText);
            resumeDataService.parseAndEnrichResume(rawText, resume);
            log.info("[ASYNC] Parsing completed for {}", resumeId);

            generateResumeContent(resume);
            resume.setParsingStatus("COMPLETED");
        } catch (Exception ex) {
            log.error("[ASYNC] Failed to process resume {}: {}", resumeId, ex.getMessage(), ex);
            if (resume != null) {
                resume.setParsingStatus("FAILED");
            }
            throw new RuntimeException("Failed to process resume: " + ex.getMessage(), ex);
        }
        
        try {
            if (resume != null) {
                resumeRepository.save(resume);
            }
        } catch (Exception ex) {
            log.error("[ASYNC] Failed to save resume {}: {}", resumeId, ex.getMessage(), ex);
            throw new RuntimeException("Failed to save resume: " + ex.getMessage(), ex);
        }
    }

    private void generateResumeContent(Resume resume) {
        try {
            log.info("[ASYNC] Starting LaTeX content generation for {}", resume.getId());
            // Generate LaTeX content
            Map<String, String> templateData = latexTemplateService.prepareDataForTemplate(resume);
            String template = latexTemplateService.getTemplateContent();
            String latexContent = latexTemplateService.replacePlaceholders(template, templateData);
            resume.setLatexContent(latexContent);
            resume.setParsingStatus("LATEX_GENERATED");
            resumeRepository.save(resume);

            log.info("[ASYNC] Starting PDF generation for {}", resume.getId());
            // Generate PDF
            byte[] pdfBytes = latexApiClient.compileLatex(latexContent);
            resume.setLatexPdf(pdfBytes);
            resume.setParsingStatus("PDF_GENERATED");
            resumeRepository.save(resume);

        } catch (com.project.resumeTracker.exception.LatexCompilationException | IOException e) {
            log.error("Error generating LaTeX content or PDF: {}", e.getMessage(), e);
            resume.setParsingStatus("FAILED");
            resume.setErrorMessage(e.getMessage());
            resumeRepository.save(resume);
        }
    }
}
