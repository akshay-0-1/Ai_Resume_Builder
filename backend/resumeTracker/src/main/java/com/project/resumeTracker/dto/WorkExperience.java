package com.project.resumeTracker.dto;

import lombok.Data;
import java.util.List;

@Data
public class WorkExperience {
    private String company;
    private String dates;
    private String role;
    private String location;
    private List<String> achievements;
}
