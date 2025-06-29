package com.project.resumeTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonalDetailsDTO {
    private String name;
    private String email;
    private String phone;
    private String address;
    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;
    private String summary;
}
