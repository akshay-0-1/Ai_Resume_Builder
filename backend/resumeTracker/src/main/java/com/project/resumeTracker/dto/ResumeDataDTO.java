package com.project.resumeTracker.dto;

import com.project.resumeTracker.entity.Education;
import com.project.resumeTracker.entity.PersonalDetails;
import com.project.resumeTracker.entity.Skill;
import com.project.resumeTracker.entity.Project;
import com.project.resumeTracker.entity.Certificate;
import com.project.resumeTracker.entity.WorkExperience;
import lombok.Data;

import java.util.List;

@Data
public class ResumeDataDTO {
    private PersonalDetails personalDetails;
    private List<WorkExperience> workExperiences;
    private List<Education> educations;
    private List<Skill> skills;
    private List<Project> projects;
    private List<Certificate> certificates;
}
