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

  getFeedback: async (page = 0, size = 5) => {
    try {
      const response = await axiosInstance.get(`/feedback?page=${page}&size=${size}`);
      return { success: true, data: response.data };
    } catch (error) {
      return { success: false, error: error.response?.data?.message || 'Failed to fetch feedback' };
    }
  },
};
