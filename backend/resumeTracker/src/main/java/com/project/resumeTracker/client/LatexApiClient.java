package com.project.resumeTracker.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.client.RestClientException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class LatexApiClient {

    private static final String API_URL = "https://latex.ytotech.com/builds/sync";
    private static final Logger log = LoggerFactory.getLogger(LatexApiClient.class);

    

    private final RestTemplate restTemplate;

    public LatexApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public byte[] compileLatex(String latexContent) throws IOException {
        if (latexContent == null || latexContent.isEmpty()) {
            throw new IllegalArgumentException("LaTeX content cannot be null or empty");
        }

        log.info("Sending request to LaTeX compilation service at {}", API_URL);
        log.debug("Full LaTeX Content:\n{}", latexContent);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        
        Map<String, Object> body = new HashMap<>();
                // Use pdfLaTeX to ensure \pdfglyphtounicode macros work
        body.put("compiler", "pdflatex");
        Map<String, Object> mainResource = new HashMap<>();
        mainResource.put("main", true);
        mainResource.put("content", latexContent);
        body.put("resources", Collections.singletonList(mainResource));
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        
        int attempts = 0;
        int maxAttempts = 3;
        long backoff = 1000L; // 1 s initial

        while (true) {
            attempts++;
            try {
                ResponseEntity<byte[]> response = restTemplate.postForEntity(
                API_URL,
                entity,
                byte[].class
            );
            
            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                byte[] pdfBytes = response.getBody();
                if (pdfBytes == null || pdfBytes.length == 0) {
                    throw new RuntimeException("Empty PDF received from LaTeX compilation service");
                }
                return pdfBytes;
            } else {
                String errorMessage = new String(response.getBody(), StandardCharsets.UTF_8);
                log.error("LaTeX compilation failed with status: {}\nResponse: {}", 
                    response.getStatusCode(), errorMessage);
                throw new com.project.resumeTracker.exception.LatexCompilationException(
                        "LaTeX compilation failed: " + errorMessage);
            }
            } catch (RestClientException e) {
                if (attempts >= maxAttempts) {
                    log.error("LaTeX compilation failed after {} attempts: {}", attempts, e.getMessage());
                    throw new com.project.resumeTracker.exception.LatexCompilationException("Error compiling LaTeX: " + e.getMessage(), e);
                }
                log.warn("Attempt {} to compile LaTeX failed: {}. Retrying in {} ms", attempts, e.getMessage(), backoff);
                try { Thread.sleep(backoff); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                backoff *= 2; // exponential
            }
        }
    }
}
