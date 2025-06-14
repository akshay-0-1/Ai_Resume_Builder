import React, { useState } from 'react';
import FeedbackForm from '../components/feedback/FeedbackForm';
import FeedbackList from '../components/feedback/FeedbackList';

const FeedbackPage = () => {
    const [newFeedback, setNewFeedback] = useState(null);

    const handleFeedbackSubmitted = (feedback) => {
        setNewFeedback(feedback);
    };

    return (
        <div className="page-container">
            <h2>User Feedback</h2>
            <p>See what others are saying about our platform and share your own experience.</p>
            <div className="feedback-section">
                <FeedbackForm onFeedbackSubmitted={handleFeedbackSubmitted} />
                <FeedbackList newFeedback={newFeedback} />
            </div>
        </div>
    );
};

export default FeedbackPage;
