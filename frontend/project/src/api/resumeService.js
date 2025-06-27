import axiosInstance from './axiosConfig';

const API_TIMEOUT = 90000; // 90 seconds

const handleApiError = (error, defaultMessage = 'API request failed') => {
  const message = error?.response?.data?.message || 
    error?.message || 
    defaultMessage;
  
  return {
    success: false,
    error: message
  };
};

const handleApiResponse = (response, successKey = 'success', dataKey = 'data') => {
  if (!response?.data) {
    return handleApiError(null, 'No response data received');
  }

  if (response.data[successKey]) {
    return { success: true, data: response.data[dataKey] };
  }

  return handleApiError(null, response.data?.message || 'Request failed');
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
      const response = await axiosInstance.get('/resumes/analysis/history');
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