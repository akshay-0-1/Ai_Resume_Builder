package com.project.resumeTracker.controller;

import com.project.resumeTracker.dto.FeedbackDTO;
import com.project.resumeTracker.entity.User;
import com.project.resumeTracker.repository.UserRepository;
import com.project.resumeTracker.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> submitFeedback(@RequestBody FeedbackDTO feedbackDTO, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return new ResponseEntity<>("User not authenticated", HttpStatus.UNAUTHORIZED);
        }

        String username = userDetails.getUsername();
        User user = userRepository.findByUsername(username)
                .orElse(null);

        if (user == null) {
            return new ResponseEntity<>("User with username '" + username + "' not found.", HttpStatus.NOT_FOUND);
        }

        Long userId = user.getId();
        FeedbackDTO savedFeedback = feedbackService.saveFeedback(feedbackDTO, userId);
        return ResponseEntity.ok(savedFeedback);
    }

    @GetMapping
    public ResponseEntity<List<FeedbackDTO>> getAllFeedback() {
        List<FeedbackDTO> feedbackList = feedbackService.getAllFeedback();
        return ResponseEntity.ok(feedbackList);
    }
}
