package com.project.resumeTracker.service;

import com.project.resumeTracker.dto.JobAnalysisResponseDTO;
import com.project.resumeTracker.service.DocumentParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

// Imports for new Gemini API SDK (com.google.genai)
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

import java.io.IOException; // Keep for general IO
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class JobAnalysisServiceImpl implements JobAnalysisService {

    // Inject DocumentParserService (which PdfParserService implements)
    private final DocumentParserService documentParserService; 

    // Keeping these as they are used by the new Client builder for Vertex AI
    @Value("${google.cloud.project.id}")
    private String gcpProjectId;

    @Value("${google.cloud.location}")
    private String gcpLocation;

    // geminiApiKey is not directly used by the new SDK when vertexAI(true) is set,
    // as it relies on ADC (Application Default Credentials) or environment variables for Vertex.
    // @Value("${gemini.api.key}")
    // private String geminiApiKey; 

    @Autowired
    public JobAnalysisServiceImpl(@Qualifier("pdfParserService") DocumentParserService documentParserService) {
        this.documentParserService = documentParserService;
    }

    @Override
    public JobAnalysisResponseDTO analyzeResumeAndJobDescription(MultipartFile resumeFile, String jobDescription) {
        log.info("Starting analysis for resume: {} with job description.", resumeFile.getOriginalFilename());

        String resumeText;
        try {
            if (resumeFile.isEmpty()) {
                throw new IllegalArgumentException("Resume file is empty.");
            }
            resumeText = documentParserService.extractText(resumeFile);
            log.info("Resume parsed successfully using DocumentParserService.");
        } catch (IOException e) {
            log.error("Error parsing resume file: {}", resumeFile.getOriginalFilename(), e);
            throw new RuntimeException("Failed to parse resume file.", e);
        }

        String promptString = String.format(
            "Analyze the following resume against the provided job description.\n"
            + "Provide a job score as a percentage (e.g., '85%%') indicating the resume's alignment with the job description. Briefly explain the score.\n"
            + "Also, provide 3-5 specific, actionable bullet points for resume improvement highlights, tailored to this job description. Format the highlights as a bulleted list.\n\n"
            + "Resume Text:\n%s\n\n"
            + "Job Description:\n%s",
            resumeText, jobDescription
        );

        try {
            // Instantiate the client using Vertex AI backend
            Client genAIClient = Client.builder()
                                     .project(gcpProjectId)
                                     .location(gcpLocation)
                                     .vertexAI(true) // Use Vertex AI backend
                                     .build();
            
            // Construct content for the new SDK
            Content genAIContent = Content.fromParts(Part.fromText(promptString));
            
            // Generate content using the new SDK
            // Assuming "gemini-1.0-pro" is a valid model name for this SDK version and configuration.
            // The new SDK's generateContent might not need a separate GenerativeModel object.
            GenerateContentResponse response = genAIClient.models.generateContent("gemini-1.0-pro", genAIContent, null);
            
            String fullResponseText = response.text(); // New way to get text
            log.info("Received response from Gemini API via new SDK.");
            log.debug("Gemini raw response: {}", fullResponseText);

            String jobScore = parseValue(fullResponseText, "Job Score:");
            String improvementHighlights = parseValue(fullResponseText, "Improvement Highlights:");

            if (jobScore.isEmpty() && improvementHighlights.isEmpty() && !fullResponseText.isEmpty()) {
                log.warn("Could not parse specific fields from Gemini response. Returning full text as highlights.");
                return new JobAnalysisResponseDTO("See highlights for score", fullResponseText);
            }

            return new JobAnalysisResponseDTO(jobScore.isEmpty() ? "Score not found" : jobScore, 
                                            improvementHighlights.isEmpty() ? "Highlights not found" : improvementHighlights);

        } catch (Exception e) { // Catching general Exception as the new SDK might throw different types
            log.error("Error calling Gemini API via new SDK: {}", e.getMessage(), e);
            // Consider a more specific exception type if known from SDK docs
            throw new RuntimeException("Failed to analyze resume and job description with Gemini API.", e);
        }
    }

    private String parseValue(String responseText, String key) {
        // Create a case-insensitive pattern for the key
        // (?i) for case-insensitive, Pattern.quote to treat key as literal string
        Pattern keyPattern = Pattern.compile("(?i)" + Pattern.quote(key));
        Matcher matcher = keyPattern.matcher(responseText);

        if (!matcher.find()) {
            log.warn("Key '{}' not found in response.", key);
            return ""; // Key not found
        }

        int startIndex = matcher.end(); // Get end of matched key, this is start of our value

        if (startIndex >= responseText.length()) {
            log.warn("Key '{}' found, but no content after it.", key);
            return ""; // Key found, but no content after it
        }

        // Search for delimiters in the original responseText starting from startIndex
        int endIndex = responseText.indexOf("\n\n", startIndex);
        if (endIndex == -1) { // Double newline not found
            endIndex = responseText.indexOf("\n", startIndex);
            if (endIndex == -1) { // Single newline not found
                endIndex = responseText.length(); // Take till the end of the string
            }
        }
        
        return responseText.substring(startIndex, endIndex).trim();
    }
}
