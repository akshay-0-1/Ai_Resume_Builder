package com.project.resumeTracker.repository;

import com.project.resumeTracker.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {
    @Query(value = "SELECT f FROM Feedback f JOIN FETCH f.user",
           countQuery = "SELECT count(f) FROM Feedback f")
    Page<Feedback> findAllWithUser(Pageable pageable);
}
