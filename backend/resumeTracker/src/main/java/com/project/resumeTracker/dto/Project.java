package com.project.resumeTracker.dto;

import lombok.Data;
import java.util.List;

@Data
public class Project {
    private String name;
    private String techStack;
    private String date;
    private List<String> achievements;
}
