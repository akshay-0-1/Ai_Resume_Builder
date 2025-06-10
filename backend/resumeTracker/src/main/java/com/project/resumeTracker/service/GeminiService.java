package com.project.resumeTracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
@Slf4j
public class GeminiService {

    @Value("${google.gemini.api.key}")
    private String apiKey;

    @Value("${google.gemini.model.name}")
    private String modelName;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getCompletion(String prompt) throws IOException {
        if (apiKey == null || apiKey.equals("YOUR_GEMINI_API_KEY_HERE") || apiKey.trim().isEmpty()) {
            log.error("Gemini API key is not configured. Please set 'google.gemini.api.key' in application.properties.");
            throw new IOException("API key for Gemini is not set.");
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":generateContent?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Construct the request body
        ObjectNode requestBody = objectMapper.createObjectNode();
        ArrayNode contents = requestBody.putArray("contents");
        ObjectNode content = contents.addObject();
        ArrayNode parts = content.putArray("parts");
        ObjectNode part = parts.addObject();
        part.put("text", prompt);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

        log.info("Sending prompt to Gemini model: {}", modelName);

        try {
            String response = restTemplate.postForObject(url, entity, String.class);

            // Parse the response to extract the text
            JsonNode responseNode = objectMapper.readTree(response);
            JsonNode candidates = responseNode.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode contentNode = firstCandidate.path("content").path("parts").get(0);
                return contentNode.path("text").asText();
            } else {
                log.error("Failed to get a valid response from Gemini API. Response: {}", response);
                throw new IOException("Invalid response structure from Gemini API.");
            }
        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage());
            throw new IOException("Failed to get completion from Gemini API.", e);
        }
    }
}
