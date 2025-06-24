package com.project.resumeTracker.dto;

import lombok.Data;
import java.util.List;


@Data
public class ResumeParsedData {
    private PersonalDetails personalDetails;
    private List<Education> education;
    private List<WorkExperience> workExperience;
    private List<Project> projects;
    private List<Skills> skills;
    private List<Certificate> certificates;
}
