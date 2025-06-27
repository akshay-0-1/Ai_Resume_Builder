import React, { createContext, useContext, useState, useEffect } from 'react';
import { resumeService } from '../api/resumeService';

const AnalysisContext = createContext();

export const useAnalysis = () => {
  const context = useContext(AnalysisContext);
  if (!context) {
    throw new Error('useAnalysis must be used within an AnalysisProvider');
  }
  return context;
};

export const AnalysisProvider = ({ children }) => {
  const initialState = {
    resumes: [],
    selectedResume: null,
    jobDescription: '',
    analysisResult: null,
    isAnalyzing: false,
    isUploading: false,
    resumeContent: '',
    isLoading: true,
    error: null
  };

  const [state, setState] = useState(initialState);

  // Destructure state and create setter functions
  const {
    resumes,
    selectedResume,
    jobDescription,
    analysisResult,
    isAnalyzing,
    isUploading,
    resumeContent,
    isLoading
  } = state;

  const setJobDescription = (newDescription) => {
    setState(prev => ({ ...prev, jobDescription: newDescription }));
  };

  // Load resumes on mount
  useEffect(() => {
    const loadResumes = async () => {
      try {
        const result = await resumeService.getResumes();
        if (result.success) {
          const newResumes = Array.isArray(result.data) ? result.data : [];
          setState(prev => ({
            ...prev,
            resumes: newResumes,
            isLoading: false
          }));
        }
      } catch (error) {
        console.error('Failed to load resumes:', error);
        setState(prev => ({ ...prev, isLoading: false }));
      }
    };

    // Only load if we have an auth token
    const token = localStorage.getItem('authToken');
    if (token) {
      // loadResumes(); // Commented out to prevent loading previous resumes
      setState(prev => ({ ...prev, isLoading: false, resumes: [] })); // Ensure list is empty and loading is false
    } else {
      setState(prev => ({ ...prev, isLoading: false }));
    }
  }, []); // Empty dependency array means this runs once on mount

  const uploadResume = async (file) => {
    try {
      if (!file) throw new Error('No file provided');

      const formData = new FormData();
      formData.append('file', file);

      setState(prev => ({ ...prev, isUploading: true }));
      
      const result = await resumeService.uploadResume(formData);
      if (!result.success) {
        throw new Error(result.error || 'Upload failed');
      }

      const newResume = result.data;
      setState(prev => ({
        ...prev,
        resumes: [...prev.resumes, newResume],
        selectedResume: newResume,
        analysisResult: null,
        resumeContent: newResume.rawText || '',
        error: null
      }));
      
      return { success: true, data: newResume };
    } catch (error) {
      console.error('Upload failed:', error);
      setState(prev => ({ ...prev, error: error.message, isUploading: false }));
      throw error;
    } finally {
      if (!state.isUploading) {
        setState(prev => ({ ...prev, isUploading: false }));
      }
    }
  };

  const selectResume = async (resume) => {
    if (!resume?.id) return;

    if (selectedResume?.id === resume.id) return;

    try {
      setState(prev => ({
        ...prev,
        selectedResume: resume,
        analysisResult: null,
        error: null
      }));

      if (!resume.resumeContent) {
        const result = await resumeService.getResume(resume.id);
        if (!result.success) {
          throw new Error(result.error || 'Failed to load resume content');
        }
        setState(prev => ({ ...prev, resumeContent: result.data.resumeContent }));
      }
    } catch (error) {
      console.error('Failed to load resume:', error);
      setState(prev => ({ ...prev, error: error.message }));
    }
  };

  const analyzeResume = async () => {
    if (!selectedResume || !jobDescription.trim()) {
      throw new Error('Please select a resume and provide a job description');
    }

    setState(prev => ({ ...prev, isAnalyzing: true, analysisResult: null }));

    try {
      const result = await resumeService.analyzeResume(selectedResume.id, jobDescription);

      if (result.success) {
        console.log('Analysis result in context:', result.data);
        setState(prev => ({ ...prev, analysisResult: result.data }));
        return result.data;
      } else {
        throw new Error(result.error);
      }
    } catch (error) {
      console.error('Analysis failed:', error);
      throw new Error(error.message || 'Analysis failed. Please try again.');
    } finally {
      setState(prev => ({ ...prev, isAnalyzing: false }));
    }
  };

  const clearAnalysis = () => {
    setState(prev => ({
      ...prev,
      analysisResult: null,
      jobDescription: '',
      selectedResume: null
    }));
  };

  const value = {
    resumes,
    selectedResume,
    jobDescription,
    setJobDescription,
    analysisResult,
    isAnalyzing,
    isUploading,
    isLoading,
    resumeContent,
    uploadResume,
    selectResume,
    analyzeResume,
    clearAnalysis,
  };

  return (
    <AnalysisContext.Provider value={value}>
      {children}
    </AnalysisContext.Provider>
  );
};