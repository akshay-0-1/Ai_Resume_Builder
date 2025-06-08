package com.project.resumeTracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "resumes", indexes = {
        @Index(name = "idx_resumes_user_id", columnList = "user_id"),
        @Index(name = "idx_resumes_active", columnList = "user_id, is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String filename;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(name = "file_url", nullable = false, length = 1000)
    private String fileUrl;

    @Column(name = "upload_date")
    @CreationTimestamp
    private LocalDateTime uploadDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "parsing_status", length = 20)
    private String parsingStatus = "pending";

    @OneToOne(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PersonalDetails personalDetails;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkExperience> workExperiences = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Education> educations = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Skill> skills = new ArrayList<>();

    // Helper methods to manage bidirectional relationships (optional but good practice)
    public void setPersonalDetails(PersonalDetails personalDetails) {
        if (personalDetails == null) {
            if (this.personalDetails != null) {
                this.personalDetails.setResume(null);
            }
        } else {
            personalDetails.setResume(this);
        }
        this.personalDetails = personalDetails;
    }

    public void addWorkExperience(WorkExperience workExperience) {
        workExperiences.add(workExperience);
        workExperience.setResume(this);
    }

    public void removeWorkExperience(WorkExperience workExperience) {
        workExperiences.remove(workExperience);
        workExperience.setResume(null);
    }

    public void addEducation(Education education) {
        educations.add(education);
        education.setResume(this);
    }

    public void removeEducation(Education education) {
        educations.remove(education);
        education.setResume(null);
    }

    public void addSkill(Skill skill) {
        skills.add(skill);
        skill.setResume(this);
    }

    public void removeSkill(Skill skill) {
        skills.remove(skill);
        skill.setResume(null);
    }

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}