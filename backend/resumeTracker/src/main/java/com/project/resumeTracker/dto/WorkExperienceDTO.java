package com.project.resumeTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkExperienceDTO {
    private String jobTitle;
    private String companyName;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCurrentJob;
    private String description;

    public Boolean getCurrentJob() {
        return isCurrentJob;
    }

    public void setCurrentJob(Boolean currentJob) {
        isCurrentJob = currentJob;
    }
}
