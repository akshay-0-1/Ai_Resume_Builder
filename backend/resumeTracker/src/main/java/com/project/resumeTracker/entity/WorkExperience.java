package com.project.resumeTracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.project.resumeTracker.helper.CustomLocalDateDeserializer;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "work_experience")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String jobTitle;
    private String companyName;
    private String location;
    private LocalDate startDate;
    @JsonDeserialize(using = CustomLocalDateDeserializer.class)
    private LocalDate endDate;
    private boolean isCurrentJob;
    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;
}
