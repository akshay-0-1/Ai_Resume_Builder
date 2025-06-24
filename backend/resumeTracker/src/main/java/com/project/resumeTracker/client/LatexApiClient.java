package com.project.resumeTracker.client;


import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class LatexApiClient {
    private static final Logger log = LoggerFactory.getLogger(LatexApiClient.class);
    private static final String API_URL = "https://latex.ytotech.com/builds/sync";
    private final RestTemplate restTemplate;

    public LatexApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public byte[] compileLatex(String latexContent) {
        log.info("Sending request to LaTeX compilation service at {}", API_URL);
        log.debug("Full LaTeX Content:\n{}", latexContent);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/pdf");
        
        Map<String, Object> request = new HashMap<>();
        request.put("compiler", "pdflatex");
        
        Map<String, Object> resource = new HashMap<>();
        resource.put("main", true);
        resource.put("content", latexContent);
        
        request.put("resources", new Object[]{resource});
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<byte[]> response = restTemplate.exchange(
            API_URL,
            HttpMethod.POST,
            entity,
            byte[].class
        );
        
        log.info("Received response with status code: {}", response.getStatusCode());
        return response.getBody();
    }
}
