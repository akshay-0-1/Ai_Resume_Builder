package com.project.resumeTracker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.resumeTracker.dto.JobAnalysisResponseDTO;
import com.project.resumeTracker.dto.TargetedChangeDTO;
import com.project.resumeTracker.entity.JobAnalysis;
import com.project.resumeTracker.entity.Resume;
import com.project.resumeTracker.repository.JobAnalysisRepository;
import com.project.resumeTracker.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobAnalysisServiceImpl implements JobAnalysisService {

    private final DocumentParsingFacade documentParsingFacade;
    private final ResumeRepository resumeRepository;
    private final JobAnalysisRepository jobAnalysisRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    @Override
    public JobAnalysisResponseDTO analyzeResumeAndJobDescription(UUID resumeId, String jobDescription, UUID userId) {
        log.info("Starting analysis for resume ID: {} for user ID: {}", resumeId, userId);

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found."));

        // Verify that the resume belongs to the authenticated user
        if (!resume.getUserId().equals(userId)) {
            throw new SecurityException("Access denied. You do not own this resume.");
        }

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

            JobAnalysisResponseDTO responseDto = parseJsonResponse(fullResponseText);

            // Save the successful analysis to the history
            saveAnalysisToHistory(userId, resume, jobDescription, responseDto);

            return responseDto;

        } catch (Exception e) {
            log.error("Error during Gemini API call or JSON parsing: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to analyze resume and job description with Gemini API.", e);
        }
    }

    private String buildAnalysisPrompt(String resumeText, String jobDescription) {
        return String.format(
            "Analyze the following resume against the provided job description and return your analysis in a strict JSON format. "
            + "The JSON object must have three keys: 'jobScore' (an integer from 0 to 100), 'targetedChanges' (a JSON array of objects, where each object has 'section' and 'suggestion' keys for specific, actionable changes), and 'overallImprovements' (a JSON array of strings for general feedback)."
            + "'section' should indicate the part of the resume to change (e.g., 'Summary', 'Skills', 'Project Experience')."
            + "'suggestion' should be a concrete instruction, like 'Change project description to...' or 'Add the skill...'."
            + "Do not include any text or formatting outside of the JSON object itself.\n\n"
            + "Resume Text:\n%s\n\n"
            + "Job Description:\n%s",
            resumeText, jobDescription
        );
    }

    private JobAnalysisResponseDTO parseJsonResponse(String jsonResponse) throws JsonProcessingException {
        String cleanedJson = jsonResponse.trim().replace("`", "");
        if (cleanedJson.startsWith("json")) {
            cleanedJson = cleanedJson.substring(4).trim();
        }

        JsonNode rootNode = objectMapper.readTree(cleanedJson);

        int score = rootNode.path("jobScore").asInt(0);

        List<TargetedChangeDTO> targetedChanges = new ArrayList<>();
        JsonNode targetedChangesNode = rootNode.path("targetedChanges");
        if (targetedChangesNode.isArray()) {
            for (JsonNode changeNode : targetedChangesNode) {
                String section = changeNode.path("section").asText();
                String suggestion = changeNode.path("suggestion").asText();
                targetedChanges.add(new TargetedChangeDTO(section, suggestion));
            }
        }

        List<String> overallImprovements = new ArrayList<>();
        JsonNode improvementsNode = rootNode.path("overallImprovements");
        if (improvementsNode.isArray()) {
            for (JsonNode improvement : improvementsNode) {
                overallImprovements.add(improvement.asText());
            }
        }

        return new JobAnalysisResponseDTO(score, targetedChanges, overallImprovements);
    }

    private void saveAnalysisToHistory(UUID userId, Resume resume, String jobDescription, JobAnalysisResponseDTO responseDto) {
        try {
            JobAnalysis analysis = JobAnalysis.builder()
                    .userId(userId)
                    .resume(resume)
                    .jobDescription(jobDescription)
                    .jobScore(responseDto.getJobScore())
                    .targetedChanges(objectMapper.writeValueAsString(responseDto.getTargetedChanges()))
                    .overallImprovements(objectMapper.writeValueAsString(responseDto.getOverallImprovements()))
                    .build();

            jobAnalysisRepository.save(analysis);
            log.info("Successfully saved job analysis history for user ID: {}", userId);
        } catch (Exception e) {
            log.error("Could not save job analysis history for user ID: {}. Error: {}", userId, e.getMessage());
        }
    }
}
