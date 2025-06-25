package com.project.resumeTracker.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * Payload accepted by PUT /api/resumes/{id} when the user saves edits.
 * Mirrors ResumeResponseDTO but excludes server-managed fields (id, dates, sizes, mimeType, etc.).
 */
@Data
public class ResumeUpdateDTO {

    @Valid
    @NotNull
    private PersonalDetailsDTO personalDetails;

    @Valid
    private List<WorkExperienceDTO> workExperiences;

    @Valid
    private List<EducationDTO> educations;

    @Valid
    private List<SkillDTO> skills;

    @Valid
    private List<Project> projects;

    @Valid
    private List<Certificate> certificates;
}
