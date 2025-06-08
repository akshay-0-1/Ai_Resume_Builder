package com.project.resumeTracker.repository;

import com.project.resumeTracker.entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EducationRepository extends JpaRepository<Education, UUID> {
    // Add custom query methods if needed in the future
}
