package com.project.resumeTracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.resumeTracker.dto.ResumeDataDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class InformationExtractorService {

    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    public ResumeDataDTO extractInformationFromText(String rawText) throws IOException {
        String prompt = buildPrompt(rawText);
        log.info("Sending prompt to Gemini for information extraction.");
        String jsonResponse = geminiService.getCompletion(prompt);
        log.info("Received JSON response from Gemini.");

        String cleanedJson = cleanGeminiResponse(jsonResponse);

        try {
            return objectMapper.readValue(cleanedJson, ResumeDataDTO.class);
        } catch (Exception e) {
            log.error("Failed to parse JSON response from Gemini: {}. Raw response: {}", e.getMessage(), jsonResponse);
            throw new IOException("Failed to parse structured data from resume text.", e);
        }
    }

    private String buildPrompt(String rawText) {
        String schema = """
            {
              \"personalDetails\": {
                \"name\": \"string\",
                \"email\": \"string\",
                \"phone\": \"string\",
                \"address\": \"string\",
                \"linkedinUrl\": \"string\",
                \"githubUrl\": \"string\",
                \"portfolioUrl\": \"string\",
                \"summary\": \"string\"
              },
              \"workExperiences\": [
                {
                  \"jobTitle\": \"string\",
                  \"companyName\": \"string\",
                  \"location\": \"string\",
                  \"startDate\": \"YYYY-MM-DD\",
                  \"endDate\": \"YYYY-MM-DD or Present\",
                  \"currentJob\": true,
                  \"description\": \"string\"
                }
              ],
              \"educations\": [
                {
                  \"institutionName\": \"string\",
                  \"degree\": \"string\",
                  \"fieldOfStudy\": \"string\",
                  \"startDate\": \"YYYY-MM-DD\",
                  \"endDate\": \"YYYY-MM-DD\",
                  \"grade\": \"string\",
                  \"description\": \"string\"
                }
              ],
              \"skills\": [
                {
                  \"skillName\": \"string\",
                  \"proficiencyLevel\": \"string (e.g., Beginner, Intermediate, Advanced, Expert)\"
                }
              ],
              \"projects\": [
                {
                  \"name\": \"string\",
                  \"techStack\": \"string\",
                  \"date\": \"YYYY-MM-DD\",
                  \"achievements\": [\"string\"]
                }
              ],
              \"certificates\": [
                {
                  \"name\": \"string\",
                  \"date\": \"YYYY-MM-DD\",
                  \"institution\": \"string\",
                  \"url\": \"string\"
                }
              ]
            }
            """;
        return "You are an expert resume parser. Analyze the following resume text and extract the information into a structured JSON object. " +
                "The JSON object must follow this exact schema: " + schema +
                "Ensure all date fields are in YYYY-MM-DD format. If a month and year are given, use the first day of the month (e.g., 'June 2020' becomes '2020-06-01'). If only a year is given, use January 1st of that year. If an end date is 'Present' or 'Current', use the literal string 'Present'. " +
                "If a piece of information is not available, set its value to null. Do not invent information. " +
                "The entire output must be a single, valid JSON object and nothing else. Do not include any introductory text, backticks, or explanations. " +
                "\n\nHere is the resume text:\n\n" + rawText;
    }

    private String cleanGeminiResponse(String response) {
        if (response == null) {
            return "{}";
        }
        int firstBrace = response.indexOf('{');
        int lastBrace = response.lastIndexOf('}');

        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            return response.substring(firstBrace, lastBrace + 1).trim();
        }

        log.warn("Could not find a valid JSON object in the Gemini response. Response was: {}", response);
        return "{}";
    }
}
