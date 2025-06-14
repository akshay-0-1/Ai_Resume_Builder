import axiosInstance from './axiosConfig';

export const feedbackService = {
  submitFeedback: async (feedbackData) => {
    try {
      const response = await axiosInstance.post('/feedback', feedbackData);
      return { success: true, data: response.data };
    } catch (error) {
      return { success: false, error: error.response?.data?.message || 'Failed to submit feedback' };
    }
  },

  getFeedback: async () => {
    try {
      const response = await axiosInstance.get('/feedback');
      return { success: true, data: response.data };
    } catch (error) {
      return { success: false, error: error.response?.data?.message || 'Failed to fetch feedback' };
    }
  },
};
