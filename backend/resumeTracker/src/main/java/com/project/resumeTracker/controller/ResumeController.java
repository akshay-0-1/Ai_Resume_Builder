package com.project.resumeTracker.controller;

import com.project.resumeTracker.dto.ApiResponse;
import com.project.resumeTracker.dto.JobAnalysisRequestDTO;
import com.project.resumeTracker.dto.JobAnalysisResponseDTO;
import com.project.resumeTracker.dto.ResumeResponseDTO;
import com.project.resumeTracker.dto.ResumeInfoDTO;
import com.project.resumeTracker.dto.JobAnalysisHistoryDTO;
import com.project.resumeTracker.entity.JobAnalysis;
import com.project.resumeTracker.entity.User;
import com.project.resumeTracker.repository.JobAnalysisRepository;
import com.project.resumeTracker.repository.UserRepository;
import com.project.resumeTracker.service.JobAnalysisService;
import com.project.resumeTracker.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("api/resumes")
@RequiredArgsConstructor
@Slf4j
public class ResumeController {

    private final ResumeService resumeService;
    private final UserRepository userRepository;
    private final JobAnalysisService jobAnalysisService;
    private final JobAnalysisRepository jobAnalysisRepository;

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

    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<JobAnalysisResponseDTO>> analyzeResumeForJob(
            @RequestBody JobAnalysisRequestDTO request,
            Authentication authentication) {

        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            UUID userId = getUserUUID(user.getId());

            if (request.getResumeId() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid request", "Resume ID is missing."));
            }
            if (request.getJobDescription() == null || request.getJobDescription().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid request", "Job description is empty."));
            }

            JobAnalysisResponseDTO analysisResult = jobAnalysisService.analyzeResumeAndJobDescription(request.getResumeId(), request.getJobDescription(), userId);

            // Check if the analysis was successful. A failure is assumed if both targeted and overall improvements are missing.
            if ((analysisResult.getTargetedChanges() == null || analysisResult.getTargetedChanges().isEmpty()) &&
                (analysisResult.getOverallImprovements() == null || analysisResult.getOverallImprovements().isEmpty())) {
                 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Analysis failed", "Could not generate improvement suggestions."));
            }

            return ResponseEntity.ok(ApiResponse.success("Analysis successful", analysisResult));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid arguments for analysis: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid request", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Resource not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error during resume analysis: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Analysis failed", "Internal server error: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ResumeInfoDTO>>> getUserResumes(
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
                Page<ResumeInfoDTO> resumes = resumeService.getUserResumes(
                        userId, PageRequest.of(page, size));
                return ResponseEntity.ok(ApiResponse.success("Resumes retrieved", resumes.getContent()));
            } else {
                List<ResumeInfoDTO> resumes = resumeService.getUserResumes(userId);
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

        log.info("Attempting to fetch resume with ID: {}", resumeId);
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

    @PutMapping("/{resumeId}/content")
    public ResponseEntity<ApiResponse<ResumeResponseDTO>> updateResumeContent(
            @PathVariable UUID resumeId,
            @RequestBody Map<String, String> payload,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            UUID userId = getUserUUID(user.getId());

            String htmlContent = payload.get("htmlContent");
            if (htmlContent == null || htmlContent.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Content cannot be empty", null));
            }

            ResumeResponseDTO updatedDto = resumeService.updateResumeContent(resumeId, userId, htmlContent);

            return ResponseEntity.ok(ApiResponse.success("Resume updated successfully", updatedDto));

        } catch (SecurityException e) {
            log.warn("Access denied while updating resume content: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access denied", null));
        } catch (RuntimeException e) {
            log.error("Error updating resume content: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage(), null));
        } catch (Exception e) {
            log.error("Internal server error while updating resume content: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Failed to update resume", "Internal server error"));
        }
    }

    @GetMapping("/{resumeId}/download")
    public ResponseEntity<ByteArrayResource> downloadResume(
            @PathVariable UUID resumeId,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            UUID userId = getUserUUID(user.getId());

            com.project.resumeTracker.entity.Resume resume = resumeService.getResumeFileById(resumeId, userId);

            ByteArrayResource resource = new ByteArrayResource(resume.getFileData());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + resume.getOriginalFilename() + "\"")
                    .contentType(MediaType.parseMediaType(resume.getMimeType()))
                    .contentLength(resume.getFileData().length)
                    .body(resource);

        } catch (RuntimeException e) {
            log.error("Error downloading resume file: {}", e.getMessage());
            if (e instanceof SecurityException) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<JobAnalysisHistoryDTO>>> getAnalysisHistory(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            UUID userId = getUserUUID(user.getId());

            List<JobAnalysisHistoryDTO> history = jobAnalysisRepository.findJobAnalysisHistoryByUserId(userId, PageRequest.of(0, 5));

            return ResponseEntity.ok(ApiResponse.success("History fetched successfully", history));

        } catch (Exception e) {
            log.error("Error fetching analysis history: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch history", "Internal server error"));
        }
    }

    @GetMapping("/analysis/history")
    public ResponseEntity<ApiResponse<List<JobAnalysisHistoryDTO>>> getAnalysisHistoryOld(
            Authentication authentication) {

        log.info("Attempting to retrieve analysis history.");
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            UUID userId = getUserUUID(user.getId());
            log.info("Fetching history for user ID: {}", userId);

            List<JobAnalysisHistoryDTO> history = jobAnalysisRepository.findJobAnalysisHistoryByUserId(userId, PageRequest.of(0, 5));
            log.info("Found {} history records for user ID: {}.", history.size(), userId);

            if (!history.isEmpty()) {
                log.debug("History data for user ID {}: {}", userId, history);
            }

            return ResponseEntity.ok(ApiResponse.success("Analysis history retrieved", history));

        } catch (Exception e) {
            log.error("Error retrieving analysis history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve history", "Internal server error"));
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