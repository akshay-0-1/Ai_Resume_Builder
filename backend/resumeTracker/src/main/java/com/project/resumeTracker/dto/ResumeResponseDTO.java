package com.project.resumeTracker.dto;


import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.project.resumeTracker.dto.Project;
import com.project.resumeTracker.dto.Certificate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumeResponseDTO {
    private UUID id;
    private String originalFilename;
    private Long fileSize;
    private String mimeType;
    private LocalDateTime uploadDate;
    private String parsingStatus;
    private String resumeContent; // This holds the raw text
    private String latexContent; // This will hold the LaTeX content

    // Parsed data
    private PersonalDetailsDTO personalDetails;
    private List<WorkExperienceDTO> workExperiences;
    private List<EducationDTO> educations;
    private List<SkillDTO> skills;
    private List<Project> projects; // New for edit functionality
    private List<Certificate> certificates; // New for edit functionality

    // Constructor for basic info (before parsing)
    public ResumeResponseDTO(UUID id, String originalFilename, Long fileSize, String mimeType, LocalDateTime uploadDate, String parsingStatus) {
        this.id = id;
        this.originalFilename = originalFilename;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.uploadDate = uploadDate;
        this.parsingStatus = parsingStatus;
    }

    // Constructor for lightweight update response
    public ResumeResponseDTO(UUID id, String originalFilename) {
        this.id = id;
        this.originalFilename = originalFilename;
    }
}