package com.project.resumeTracker.repository;

import com.project.resumeTracker.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.UUID;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {
    @Query("SELECT f FROM Feedback f JOIN FETCH f.user ORDER BY f.createdAt DESC")
    List<Feedback> findAllWithUser();
}
