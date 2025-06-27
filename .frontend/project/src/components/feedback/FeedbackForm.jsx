import React, { useState } from 'react';
import { feedbackService } from '../../api/feedbackService';
import StarRating from './StarRating';
import Button from '../common/Button';
import { Loader2, CheckCircle, AlertTriangle } from 'lucide-react';

const FeedbackForm = ({ onFeedbackSubmitted }) => {
    const [rating, setRating] = useState(0);
    const [feedbackText, setFeedbackText] = useState('');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        if (rating === 0) {
            setError('Please select a rating to continue.');
            return;
        }

        if (!feedbackText.trim()) {
            setError('Please share your thoughts before submitting.');
            return;
        }

        setIsLoading(true);

        try {
            const response = await feedbackService.submitFeedback({ rating, feedbackText });

            if (response.success) {
                setSuccess('Thank you for your feedback!');
                if (onFeedbackSubmitted) {
                    onFeedbackSubmitted(response.data);
                }
            } else {
                setError(response.error || 'Something went wrong. Please try again.');
            }
        } catch (err) {
            setError('An unexpected error occurred. Please try again later.');
        } finally {
            setIsLoading(false);
        }
    };

    if (success) {
        return (
            <div className="text-center py-8">
                <CheckCircle className="w-16 h-16 text-green-500 mx-auto mb-4" />
                <h3 className="text-xl font-semibold text-gray-800 mb-2">Thank You!</h3>
                <p className="text-gray-600">{success}</p>
            </div>
        );
    }

    return (
        <form onSubmit={handleSubmit} className="space-y-6">
            <div className="text-center">
                <p className="text-gray-600 mb-4">What would you rate your experience?</p>
                <div className="flex justify-center">
                    <StarRating rating={rating} onRatingChange={setRating} />
                </div>
            </div>

            <div>
                <label htmlFor="feedbackText" className="block text-sm font-medium text-gray-700 mb-2">
                    Share your thoughts
                </label>
                <textarea
                    id="feedbackText"
                    value={feedbackText}
                    onChange={(e) => setFeedbackText(e.target.value)}
                    placeholder="Tell us what you liked or what we can improve..."
                    rows="4"
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg shadow-sm focus:ring-primary-500 focus:border-primary-500 transition-colors"
                    disabled={isLoading}
                />
            </div>

            {error && (
                <div className="flex items-center p-3 text-sm text-red-700 bg-red-100 rounded-lg" role="alert">
                    <AlertTriangle className="w-5 h-5 mr-2 flex-shrink-0" />
                    <div>{error}</div>
                </div>
            )}

            <div className="flex justify-end pt-2">
                <Button type="submit" disabled={isLoading} className="w-full sm:w-auto">
                    {isLoading ? (
                        <>
                            <Loader2 className="w-5 h-5 mr-2 animate-spin" />
                            Submitting...
                        </>
                    ) : (
                        'Submit Feedback'
                    )}
                </Button>
            </div>
        </form>
    );
};

export default FeedbackForm;
