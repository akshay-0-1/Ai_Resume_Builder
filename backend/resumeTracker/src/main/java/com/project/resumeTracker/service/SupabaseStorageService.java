package com.project.resumeTracker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import jakarta.annotation.PostConstruct;


@Service
@Slf4j
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.storage.bucket}")
    private String defaultBucket;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void init() {
        // Create the default bucket if it doesn't exist
        createBucketIfNotExists(defaultBucket);
    }

    private void createBucketIfNotExists(String bucketName) {
        try {
            // First check if bucket exists
            String checkUrl = supabaseUrl + "/storage/v1/bucket/" + bucketName;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(supabaseKey);
            
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            
            try {
                restTemplate.exchange(
                    checkUrl,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
                );
                log.info("Bucket '{}' already exists", bucketName);
                return; // Bucket exists, no need to create
            } catch (Exception e) {
                log.info("Bucket '{}' not found, will create it", bucketName);
                // Continue to create bucket
            }
            
            // Create bucket
            String createUrl = supabaseUrl + "/storage/v1/bucket";
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            String requestBody = "{\"name\":\"" + bucketName + "\", \"public\": true}";
            HttpEntity<String> createRequestEntity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                createUrl,
                HttpMethod.POST,
                createRequestEntity,
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully created bucket '{}'", bucketName);
            } else {
                log.error("Failed to create bucket '{}': {}", bucketName, response.getBody());
            }
        } catch (Exception e) {
            log.error("Error creating bucket '{}': {}", bucketName, e.getMessage());
        }
    }

    public String uploadFile(String bucket, String filename, MultipartFile file) throws IOException {
        // Ensure bucket exists before uploading
        createBucketIfNotExists(bucket);
        
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + filename;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(supabaseKey);
        headers.setContentType(MediaType.parseMediaType(file.getContentType()));

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    uploadUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return getPublicUrl(bucket, filename);
            } else {
                throw new RuntimeException("Failed to upload file to Supabase Storage");
            }
        } catch (Exception e) {
            log.error("Error uploading file to Supabase: {}", e.getMessage());
            throw new RuntimeException("Upload failed", e);
        }
    }

    private String getPublicUrl(String bucket, String filename) {
        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + filename;
    }
}