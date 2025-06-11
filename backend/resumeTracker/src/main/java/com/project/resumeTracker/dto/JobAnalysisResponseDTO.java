package com.project.resumeTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobAnalysisResponseDTO {
    private int jobScore;
    private List<TargetedChangeDTO> targetedChanges;
    private List<String> overallImprovements;
}
