package com.project.resumeTracker.service;

import com.project.resumeTracker.dto.JobAnalysisResponseDTO;
import com.project.resumeTracker.entity.Resume;
import com.project.resumeTracker.repository.ResumeRepository;
import com.project.resumeTracker.service.DocumentParsingFacade;
import com.project.resumeTracker.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobAnalysisServiceImpl implements JobAnalysisService {

    private final DocumentParsingFacade documentParsingFacade;
    private final ResumeRepository resumeRepository;
    private final GeminiService geminiService;

    @Override
    public JobAnalysisResponseDTO analyzeResumeAndJobDescription(UUID resumeId, String jobDescription, UUID userId) {
        log.info("Starting analysis for resume ID: {} for user ID: {}", resumeId, userId);

        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new RuntimeException("Resume not found or access denied for the user."));

        String resumeText;
        try {
            if (resume.getFileData() == null || resume.getFileData().length == 0) {
                throw new IllegalArgumentException("Resume file data is empty.");
            }
            resumeText = documentParsingFacade.parseDocument(new ByteArrayInputStream(resume.getFileData()), resume.getMimeType());
            log.info("Resume parsed successfully using DocumentParsingFacade.");
        } catch (IOException e) {
            log.error("Error parsing resume data for resume ID: {}", resumeId, e);
            throw new RuntimeException("Failed to parse resume file data.", e);
        }

        String promptString = buildAnalysisPrompt(resumeText, jobDescription);

        try {
            String fullResponseText = geminiService.getCompletion(promptString);
            log.info("Received response from Gemini API.");
            log.debug("Gemini raw response: {}", fullResponseText);

            String jobScore = parseValue(fullResponseText, "Job Score:");
            String improvementHighlights = parseValue(fullResponseText, "Improvement Highlights:");

            if (jobScore.isEmpty() && improvementHighlights.isEmpty() && !fullResponseText.isEmpty()) {
                log.warn("Could not parse specific fields from Gemini response. Returning full text as highlights.");
                return new JobAnalysisResponseDTO("See highlights for score", fullResponseText);
            }

            return new JobAnalysisResponseDTO(jobScore.isEmpty() ? "Score not found" : jobScore,
                                            improvementHighlights.isEmpty() ? "Highlights not found" : improvementHighlights);

        } catch (Exception e) {
            log.error("Error during Gemini API call: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to analyze resume and job description with Gemini API.", e);
        }
    }

    private String buildAnalysisPrompt(String resumeText, String jobDescription) {
        return String.format(
            "Analyze the following resume against the provided job description.\n"
            + "Provide a job score as a percentage (e.g., '85%%') indicating the resume's alignment with the job description. Briefly explain the score.\n"
            + "Also, provide 3-5 specific, actionable bullet points for resume improvement highlights, tailored to this job description. Format the highlights as a bulleted list.\n\n"
            + "Resume Text:\n%s\n\n"
            + "Job Description:\n%s",
            resumeText, jobDescription
        );
    }

    private String parseValue(String responseText, String key) {
        Pattern keyPattern = Pattern.compile("(?i)" + Pattern.quote(key));
        Matcher matcher = keyPattern.matcher(responseText);

        if (!matcher.find()) {
            log.warn("Key '{}' not found in response.", key);
            return "";
        }

        int startIndex = matcher.end();

        if (startIndex >= responseText.length()) {
            log.warn("Key '{}' found, but no content after it.", key);
            return "";
        }

        int endIndex = responseText.indexOf("\n\n", startIndex);
        if (endIndex == -1) {
            endIndex = responseText.indexOf("\n", startIndex);
            if (endIndex == -1) {
                endIndex = responseText.length();
            }
        }

        return responseText.substring(startIndex, endIndex).trim();
    }
}
