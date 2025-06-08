package com.project.resumeTracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "personal_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonalDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;
    private String email;
    private String phone;
    private String address;
    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;
    @Column(columnDefinition = "TEXT")
    private String summary;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;
}
