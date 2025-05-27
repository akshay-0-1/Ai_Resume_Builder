package com.project.resumeTracker.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String emailOrUsername;
    private String password;
}