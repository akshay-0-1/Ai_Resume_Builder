package com.project.resumeTracker.repository;

import com.project.resumeTracker.entity.PersonalDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PersonalDetailsRepository extends JpaRepository<PersonalDetails, UUID> {
    // Add custom query methods if needed in the future
}
