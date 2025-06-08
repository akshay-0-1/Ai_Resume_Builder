package com.project.resumeTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillDTO {
    private String skillName;
    private String proficiencyLevel; // e.g., Beginner, Intermediate, Advanced, or a numerical scale
}
