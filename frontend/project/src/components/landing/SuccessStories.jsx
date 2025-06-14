import React, { useState, useEffect, useRef } from 'react';
import { Plus, Star, MessageSquare, ChevronLeft, ChevronRight } from 'lucide-react';
import { feedbackService } from '../../api/feedbackService';
import Card from '../common/Card';
import Modal from '../common/Modal';
import FeedbackForm from '../feedback/FeedbackForm';

const SuccessStories = () => {
  const [feedbacks, setFeedbacks] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const scrollContainerRef = useRef(null);

  useEffect(() => {
    fetchFeedbacks();
  }, []);

  const fetchFeedbacks = async () => {
    try {
      const response = await feedbackService.getFeedback();
      if (response.success) {
        // Sort by creation date to show the latest feedback first
        const sortedFeedbacks = response.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
        setFeedbacks(sortedFeedbacks);
      } else {
        console.error('Error fetching feedback:', response.error);
      }
    } catch (error) {
      console.error('Error fetching feedback:', error);
    }
  };

  const handleFeedbackSubmitted = (newFeedback) => {
    // Add the new feedback to the state. It's received from the server response,
    // so it includes the ID and is the single source of truth.
    setFeedbacks(prevFeedbacks => [newFeedback, ...prevFeedbacks]);
    setIsModalOpen(false);
  };

  const scroll = (direction) => {
    if (scrollContainerRef.current) {
      scrollContainerRef.current.scrollBy({ left: direction * 350, behavior: 'smooth' });
    }
  };

  return (
    <section className="py-20 bg-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center mb-12">
          <h2 className="text-4xl md:text-5xl font-display font-bold text-gray-900 mb-4">
            What Our Users Say
          </h2>
          <p className="mt-2 text-lg text-gray-600">Real stories from professionals who have leveled-up their careers with ResumeAI.</p>
        </div>

        <div className="relative">
          <div ref={scrollContainerRef} className="flex overflow-x-auto space-x-8 pb-8 scrollbar-hide">
            <div className="flex-shrink-0 w-80 flex items-stretch">
              <Card 
                className="p-8 w-full text-center flex flex-col justify-center items-center cursor-pointer border-2 border-dashed border-gray-300 hover:border-primary-400 hover:bg-primary-50 transition-all duration-300 group"
                onClick={() => setIsModalOpen(true)}
              >
                <div className="w-16 h-16 bg-gray-200 rounded-full flex items-center justify-center mb-4 group-hover:bg-primary-100 transition-colors duration-300">
                  <Plus className="w-8 h-8 text-gray-500 group-hover:text-primary-500 transition-colors duration-300" />
                </div>
                <h3 className="text-lg font-semibold text-gray-700 group-hover:text-primary-600 transition-colors duration-300">Add Your Story</h3>
              </Card>
            </div>

            {feedbacks.map((feedback) => (
              <div key={feedback.id} className="flex-shrink-0 w-80">
                <Card className="p-8 h-full flex flex-col bg-gray-50/50 border border-gray-200/80 shadow-sm hover:shadow-lg transition-shadow duration-300">
                  <MessageSquare className="w-8 h-8 text-primary-400 mb-4" />
                  <p className="text-gray-600 mb-6 flex-grow">
                    {feedback.feedbackText}
                  </p>
                  <div className="flex items-center justify-between pt-4 border-t border-gray-200/80">
                    <div className="font-semibold text-gray-800">{feedback.username}</div>
                    <div className="flex items-center">
                      {[...Array(5)].map((_, i) => (
                        <Star
                          key={i}
                          className={`w-5 h-5 ${i < feedback.rating ? 'text-yellow-400 fill-current' : 'text-gray-300'}`}
                        />
                      ))}
                    </div>
                  </div>
                </Card>
              </div>
            ))}
          </div>
          
          <button onClick={() => scroll(-1)} className="absolute top-1/2 -translate-y-1/2 -left-4 w-12 h-12 bg-white/80 backdrop-blur-sm rounded-full shadow-md flex items-center justify-center hover:bg-white transition-colors z-10">
            <ChevronLeft className="w-6 h-6 text-gray-700" />
          </button>
          <button onClick={() => scroll(1)} className="absolute top-1/2 -translate-y-1/2 -right-4 w-12 h-12 bg-white/80 backdrop-blur-sm rounded-full shadow-md flex items-center justify-center hover:bg-white transition-colors z-10">
            <ChevronRight className="w-6 h-6 text-gray-700" />
          </button>
        </div>
      </div>

      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Share Your Feedback">
        <FeedbackForm onFeedbackSubmitted={handleFeedbackSubmitted} />
      </Modal>
    </section>
  );
};

export default SuccessStories;
