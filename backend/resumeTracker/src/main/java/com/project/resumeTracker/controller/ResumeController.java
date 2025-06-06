package com.project.resumeTracker.controller;

import com.project.resumeTracker.dto.ApiResponse;
import com.project.resumeTracker.dto.ResumeResponseDTO;
import com.project.resumeTracker.entity.User;
import com.project.resumeTracker.repository.UserRepository;
import com.project.resumeTracker.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/resumes")
@RequiredArgsConstructor
@Slf4j
public class ResumeController {

    private final ResumeService resumeService;
    private final UserRepository userRepository;

    private UUID getUserUUID(Long userId) {
        // Create a deterministic UUID based on the user's ID
        return UUID.nameUUIDFromBytes(("user-" + userId).getBytes());
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<ResumeResponseDTO>> uploadResume(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Generate a deterministic UUID based on the user's ID
            UUID userId = getUserUUID(user.getId());
            
            ResumeResponseDTO resume = resumeService.uploadResume(file, userId);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Resume uploaded successfully", resume));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid request", e.getMessage()));
        } catch (Exception e) {
            log.error("Error uploading resume: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Upload failed", "Internal server error"));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ResumeResponseDTO>>> getUserResumes(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Generate a deterministic UUID based on the user's ID
            UUID userId = getUserUUID(user.getId());

            if (page >= 0 && size > 0) {
                Page<ResumeResponseDTO> resumes = resumeService.getUserResumes(
                        userId, PageRequest.of(page, size));
                return ResponseEntity.ok(ApiResponse.success("Resumes retrieved", resumes.getContent()));
            } else {
                List<ResumeResponseDTO> resumes = resumeService.getUserResumes(userId);
                return ResponseEntity.ok(ApiResponse.success("Resumes retrieved", resumes));
            }

        } catch (Exception e) {
            log.error("Error retrieving resumes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve resumes", "Internal server error"));
        }
    }

    @GetMapping("/{resumeId}")
    public ResponseEntity<ApiResponse<ResumeResponseDTO>> getResume(
            @PathVariable UUID resumeId,
            Authentication authentication) {

        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Generate a deterministic UUID based on the user's ID
            UUID userId = getUserUUID(user.getId());
            
            ResumeResponseDTO resume = resumeService.getResumeById(resumeId, userId);

            return ResponseEntity.ok(ApiResponse.success("Resume retrieved", resume));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Resume not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving resume: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve resume", "Internal server error"));
        }
    }

    @DeleteMapping("/{resumeId}")
    public ResponseEntity<ApiResponse<Void>> deleteResume(
            @PathVariable UUID resumeId,
            Authentication authentication) {

        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Generate a deterministic UUID based on the user's ID
            UUID userId = getUserUUID(user.getId());
            
            resumeService.deleteResume(resumeId, userId);

            return ResponseEntity.ok(ApiResponse.success("Resume deleted successfully", null));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Resume not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting resume: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete resume", "Internal server error"));
        }
    }
}