package com.project.resumeTracker.controller;

import com.project.resumeTracker.dto.ApiResponse;
import com.project.resumeTracker.dto.JobAnalysisRequestDTO;
import com.project.resumeTracker.dto.JobAnalysisResponseDTO;
import com.project.resumeTracker.dto.ResumeResponseDTO;
import com.project.resumeTracker.dto.ResumeUpdateDTO;
import com.project.resumeTracker.dto.ResumeStatusDTO;
import jakarta.validation.Valid;
import com.project.resumeTracker.dto.ResumeInfoDTO;
import com.project.resumeTracker.dto.JobAnalysisHistoryDTO;
import com.project.resumeTracker.entity.User;

import com.project.resumeTracker.repository.UserRepository;
import com.project.resumeTracker.service.JobAnalysisService;
import com.project.resumeTracker.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("api/resumes")
@RequiredArgsConstructor
@Slf4j
public class ResumeController {

    private final ResumeService resumeService;
    private final UserRepository userRepository;
    private final JobAnalysisService jobAnalysisService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<ResumeResponseDTO>> uploadResume(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        try {
            log.info("Received request to upload resume for user: {}", authentication.getName());
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Long userId = user.getId();
            
            ResumeResponseDTO resume = resumeService.enqueueUpload(file, userId);

            log.info("Successfully uploaded resume for user: {}", username);
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(ApiResponse.success("Resume uploaded successfully", resume));

        } catch (IOException e) {
            log.error("File processing error during resume upload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("File processing failed", "Error processing the uploaded file."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid request", e.getMessage()));
        } catch (Exception e) {
            log.error("Error uploading resume: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Upload failed", "Internal server error"));
        }
    }

    @GetMapping("/{resumeId}/status")
    public ResponseEntity<ApiResponse<ResumeStatusDTO>> getResumeStatus(@PathVariable UUID resumeId,
                                                                        Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Long userId = user.getId();
        ResumeStatusDTO dto = resumeService.getResumeStatus(resumeId, userId);

        switch (dto.getParsingStatus().toUpperCase()) {
            case "FAILED":
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error("PDF generation failed", dto.getFailureMessage()));
            case "PDF_GENERATED":
            case "COMPLETED":
                return ResponseEntity.ok(ApiResponse.success("PDF ready", dto));
            default:
                return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .header(HttpHeaders.RETRY_AFTER, "3")
                        .body(ApiResponse.success("Processing", dto));
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
            Long userId = user.getId();

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
            
            Long userId = user.getId();

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
            
            Long userId = user.getId();
            
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

    @GetMapping("/{resumeId}/download")
    public ResponseEntity<?> downloadResume(
            @PathVariable UUID resumeId,
            Authentication authentication) {
        try {
            log.info("Received request to download resume with ID: {} for user: {}", resumeId, authentication.getName());
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Long userId = user.getId();

            com.project.resumeTracker.entity.Resume resume = resumeService.getResumeFileById(resumeId, userId);

            // If PDF generation failed, inform client
            if ("FAILED".equalsIgnoreCase(resume.getParsingStatus())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error("PDF generation failed", "LaTeX compilation failed for this resume. Check logs."));
            }

            // If PDF not yet ready, ask client to retry later
            if (resume.getLatexPdf() == null || resume.getLatexPdf().length == 0) {
                return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .header(HttpHeaders.RETRY_AFTER, "3")
                        .body(ApiResponse.success("PDF is not ready yet", null));
            }

            // PDF is ready, return it
            if (resume.getLatexPdf() != null && resume.getLatexPdf().length > 0) {
                ByteArrayResource resource = new ByteArrayResource(resume.getLatexPdf());
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resume.getOriginalFilename() + ".pdf\"")
                        .contentType(MediaType.APPLICATION_PDF)
                        .contentLength(resume.getLatexPdf().length)
                        .body(resource);
            } else {
                // Fallback: original file (should rarely hit when PDF expected)
                ByteArrayResource resource = new ByteArrayResource(resume.getFileData());
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resume.getOriginalFilename() + "\"")
                        .contentType(MediaType.parseMediaType(resume.getMimeType()))
                        .contentLength(resume.getFileData().length)
                        .body(resource);
            }

        } catch (com.project.resumeTracker.exception.LatexCompilationException e) {
            // LaTeX compilation failed – let the client know so it can display error
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("PDF generation failed", e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Error downloading resume file: {}", e.getMessage());
            if (e instanceof SecurityException) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/analysis/history")
    public ResponseEntity<ApiResponse<List<JobAnalysisHistoryDTO>>> getAnalysisHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Long userId = user.getId();

            Pageable pageable = PageRequest.of(page, size);
            List<JobAnalysisHistoryDTO> history = jobAnalysisService.getJobAnalysisHistory(userId, pageable);

            return ResponseEntity.ok(ApiResponse.success("Analysis history retrieved successfully", history));

        } catch (Exception e) {
            log.error("Error fetching analysis history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch history", "Internal server error"));
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
            
            Long userId = user.getId();
            
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
    @GetMapping("/{resumeId}/editable")
    public ResponseEntity<ApiResponse<ResumeResponseDTO>> getEditableResume(
            @PathVariable UUID resumeId,
            Authentication authentication) {
        log.info("Fetching editable resume with ID: {}", resumeId);
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Long userId = user.getId();

            ResumeResponseDTO resume = resumeService.getResumeById(resumeId, userId);
            return ResponseEntity.ok(ApiResponse.success("Editable resume retrieved", resume));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Resume not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching editable resume: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch editable resume", "Internal server error"));
        }
    }

    @PutMapping({"/{resumeId}", "/{resumeId}/content"})
    public ResponseEntity<ApiResponse<ResumeResponseDTO>> updateResume(
            @PathVariable UUID resumeId,
            @Valid @RequestBody ResumeUpdateDTO updateDTO,
            Authentication authentication) {
        log.info("Received resume update request for id: {}", resumeId);
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Long userId = user.getId();

            ResumeResponseDTO updated = resumeService.updateResume(resumeId, userId, updateDTO);
            return ResponseEntity.ok(ApiResponse.success("Resume updated successfully", updated));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Resume not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating resume: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update resume", "Internal server error"));
        }
    }
}