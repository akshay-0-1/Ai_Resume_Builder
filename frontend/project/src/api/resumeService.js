import axiosInstance from './axiosConfig';

const handleApiError = (error) => {
  return {
    success: false,
    error: error.response?.data?.message || 'API request failed'
  };
};

const handleApiResponse = (response) => {
  if (response.data && response.data.success) {
    return { success: true, data: response.data.data };
  }
  return {
    success: false,
    error: response.data?.message || 'An unknown API error occurred'
  };
};

export const resumeService = {
  uploadResume: async (formData) => {
    try {
      const response = await axiosInstance.post('/resumes/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
        timeout: 90000, // 90-second timeout
      });
      return handleApiResponse(response);
    } catch (error) {
      return handleApiError(error);
    }
  },

  getResumes: async () => {
    try {
      const response = await axiosInstance.get('/resumes');
      return handleApiResponse(response);
    } catch (error) {
      return handleApiError(error);
    }
  },

  analyzeResume: async (resumeId, jobDescription) => {
    try {
      const response = await axiosInstance.post('/resumes/analyze', {
        resumeId,
        jobDescription,
      }, {
        timeout: 90000, // 90-second timeout
      });
      console.log('API Response in resumeService:', response.data);
      return handleApiResponse(response);
    } catch (error) {
      console.error('API Error in resumeService:', error);
      return handleApiError(error);
    }
  },

  getAnalysisHistory: async () => {
    try {
      const response = await axiosInstance.get('/resumes/history');
      return handleApiResponse(response);
    } catch (error) {
      return handleApiError(error);
    }
  },

  getResume: async (resumeId) => {
    try {
      const response = await axiosInstance.get(`/resumes/${resumeId}`);
      return handleApiResponse(response);
    } catch (error) {
      return handleApiError(error);
    }
  },

  updateResumeContent: async (resumeId, htmlContent) => {
    try {
      const response = await axiosInstance.put(`/resumes/${resumeId}/content`, { htmlContent });
      return handleApiResponse(response);
    } catch (error) {
      return handleApiError(error);
    }
  },
};