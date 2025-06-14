import React, { useState, useEffect, useRef } from 'react';
import { Plus, Star, MessageSquare, ChevronLeft, ChevronRight } from 'lucide-react';
import { feedbackService } from '../../api/feedbackService';
import { useAuth } from '../../context/AuthContext';
import Spinner from '../common/Spinner';
import Modal from '../common/Modal';
import FeedbackForm from '../feedback/FeedbackForm';
import Card from '../common/Card';

const SuccessStories = () => {
  const [feedbacks, setFeedbacks] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);
  const scrollContainerRef = useRef(null);
  const { user } = useAuth();

  const fetchFeedback = async () => {
    if (!hasMore || loading) return;
    setLoading(true);
    const response = await feedbackService.getFeedback(page, 5);
    if (response.success) {
      setFeedbacks(prev => [...prev, ...response.data.content]);
      setHasMore(!response.data.last);
      setPage(prev => prev + 1);
    }
    setLoading(false);
  };

  useEffect(() => {
    fetchFeedback();
  }, []);

  const handleFeedbackSubmitted = (newFeedback) => {
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
        </div>
        <div className="text-center mt-8">
          {loading && <Spinner />}
          {hasMore && !loading && (
            <button onClick={fetchFeedback} className="bg-blue-600 text-white font-bold py-3 px-8 rounded-lg hover:bg-blue-700 transition duration-300">
              Load More
            </button>
          )}
        </div>
      </div>

      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Share Your Feedback">
        <FeedbackForm onFeedbackSubmitted={handleFeedbackSubmitted} />
      </Modal>
    </section>
  );
};

export default SuccessStories;
