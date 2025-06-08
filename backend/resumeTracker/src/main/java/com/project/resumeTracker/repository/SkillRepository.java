package com.project.resumeTracker.repository;

import com.project.resumeTracker.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SkillRepository extends JpaRepository<Skill, UUID> {
    // Add custom query methods if needed in the future
}
