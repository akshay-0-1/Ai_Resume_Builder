package com.project.resumeTracker.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

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
    private String fileUrl;
}