package com.project.resumeTracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumeResponseDTO {
    private UUID id;
    private String originalFilename;
    private Long fileSize;
    private String mimeType;
    private String resumeContent;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime uploadDate;
    private String parsingStatus;

    // Parsed data
    private PersonalDetailsDTO personalDetails;
    private List<WorkExperienceDTO> workExperiences;
    private List<EducationDTO> educations;
    private List<SkillDTO> skills;

    // Constructor for basic info (before parsing)
    public ResumeResponseDTO(UUID id, String originalFilename, Long fileSize, String mimeType, LocalDateTime uploadDate, String parsingStatus) {
        this.id = id;
        this.originalFilename = originalFilename;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.uploadDate = uploadDate;
        this.parsingStatus = parsingStatus;
    }
}