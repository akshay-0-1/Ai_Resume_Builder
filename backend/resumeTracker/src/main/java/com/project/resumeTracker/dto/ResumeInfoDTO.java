package com.project.resumeTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeInfoDTO {
    private UUID id;
    private String fileName;
    private long fileSize;
    private LocalDateTime uploadDate;
    private String mimeType;
}
