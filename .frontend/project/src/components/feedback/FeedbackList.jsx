import React, { useEffect, useState } from 'react';
import { feedbackService } from '../../api/feedbackService';
import StarRating from './StarRating';

const FeedbackList = ({ newFeedback }) => {
    const [feedbackList, setFeedbackList] = useState([]);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchFeedback = async () => {
            const response = await feedbackService.getFeedback();
            if (response.success) {
                setFeedbackList(response.data);
            } else {
                setError(response.error);
            }
        };

        fetchFeedback();
    }, []);

    useEffect(() => {
        if (newFeedback) {
            setFeedbackList([newFeedback, ...feedbackList]);
        }
    }, [newFeedback]);

    return (
        <div className="feedback-list-container">
            <h3>User Feedback</h3>
            {error && <p className="error-message">{error}</p>}
            <div className="feedback-list">
                {feedbackList.map((feedback, index) => (
                    <div key={index} className="feedback-item">
                        <div className="feedback-header">
                            <strong>{feedback.username}</strong>
                            <StarRating rating={feedback.rating} onRatingChange={() => {}} />
                        </div>
                        <p>{feedback.feedbackText}</p>
                        <small>{new Date(feedback.createdAt).toLocaleString()}</small>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default FeedbackList;
