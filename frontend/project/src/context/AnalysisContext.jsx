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
    isLoading: true
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
      loadResumes();
    } else {
      setState(prev => ({ ...prev, isLoading: false }));
    }
  }, []); // Empty dependency array means this runs once on mount

  const uploadResume = async (file) => {
    const formData = new FormData();
    formData.append('file', file);

    setState(prev => ({ ...prev, isUploading: true }));
    try {
      const result = await resumeService.uploadResume(formData);
      if (result.success && result.data.success) {
        const newResume = result.data.data;
        setState(prev => ({
          ...prev,
          resumes: Array.isArray(prev.resumes) ? [...prev.resumes, newResume] : [newResume],
          selectedResume: newResume,
          analysisResult: null,
          resumeContent: newResume.resumeContent,
          isUploading: false
        }));
        return { success: true, data: newResume };
      } else {
        throw new Error(result.data.message || result.error || 'Upload failed');
      }
    } catch (error) {
      console.error('Upload failed:', error);
      throw error;
    }
  };

  const selectResume = async (resume) => {
    console.log('Attempting to select resume:', resume);
    if (!resume || !resume.id) {
      console.error('selectResume called with invalid resume object');
      return;
    }

    if (selectedResume?.id === resume.id) return;

    // Update selected resume and clear analysis
    setState(prev => ({
      ...prev,
      selectedResume: resume,
      analysisResult: null
    }));

    // If resume already has content, use it immediately
    if (resume.resumeContent) {
      setState(prev => ({ ...prev, resumeContent: resume.resumeContent }));
    } else {
      // Otherwise fetch it from the backend
      try {
        console.log(`Fetching full details for resume ID: ${resume.id}`);
        const result = await resumeService.getResume(resume.id);
        if (result.success && result.data.success) {
          setState(prev => ({ ...prev, resumeContent: result.data.data.resumeContent }));
        } else {
          toast.error('Could not load resume content.');
          console.error('Failed to load resume content:', result.data?.message || result.error);
        }
      } catch (error) {
        toast.error('Failed to fetch resume details.');
        console.error('Error fetching resume details:', error);
      }
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