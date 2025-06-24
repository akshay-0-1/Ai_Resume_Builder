package com.project.resumeTracker.service;

import com.project.resumeTracker.dto.FeedbackDTO;
import com.project.resumeTracker.entity.Feedback;
import com.project.resumeTracker.entity.User;
import com.project.resumeTracker.repository.FeedbackRepository;
import com.project.resumeTracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;



@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    @Override
    public FeedbackDTO saveFeedback(FeedbackDTO feedbackDTO, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Feedback feedback = Feedback.builder()
                .user(user)
                .rating(feedbackDTO.getRating())
                .feedbackText(feedbackDTO.getFeedbackText())
                .build();

        Feedback savedFeedback = feedbackRepository.save(feedback);
        return toDTO(savedFeedback);
    }

    @Override
    public Page<FeedbackDTO> getAllFeedback(Pageable pageable) {
        return feedbackRepository.findAllWithUser(pageable).map(this::toDTO);
    }

    private FeedbackDTO toDTO(Feedback feedback) {
        return new FeedbackDTO(
                feedback.getId(),
                feedback.getUser().getUsername(),
                feedback.getRating(),
                feedback.getFeedbackText(),
                feedback.getCreatedAt()
        );
    }
}
