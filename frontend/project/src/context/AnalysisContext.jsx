import React, { createContext, useContext, useState } from 'react';
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
  const [resumes, setResumes] = useState([]);
  const [selectedResume, setSelectedResume] = useState(null);
  const [jobDescription, setJobDescription] = useState('');
  const [analysisResult, setAnalysisResult] = useState(null);
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [isUploading, setIsUploading] = useState(false);

  const uploadResume = async (file) => {
    const formData = new FormData();
    formData.append('file', file);

    setIsUploading(true);
    try {
      const result = await resumeService.uploadResume(formData);
      if (result.success && result.data.success) {
        const newResume = result.data.data;
        setResumes((prev) => [...prev, newResume]);
        setSelectedResume(newResume);
        setAnalysisResult(null); // Clear old analysis on new upload
        return { success: true, data: newResume };
      } else {
        throw new Error(result.data.message || result.error || 'Upload failed');
      }
    } catch (error) {
      console.error('Upload failed:', error);
      throw error;
    } finally {
      setIsUploading(false);
    }
  };

  const selectResume = (resume) => {
    setSelectedResume(resume);
    setAnalysisResult(null); // Clear analysis when switching resumes
  };

  const analyzeResume = async () => {
    if (!selectedResume || !jobDescription.trim()) {
      throw new Error('Please select a resume and provide a job description');
    }

    setIsAnalyzing(true);
    setAnalysisResult(null);

    try {
      const result = await resumeService.analyzeResume(selectedResume.id, jobDescription);

      if (result.success) {
        console.log('Analysis result in context:', result.data);
        setAnalysisResult(result.data);
        return result.data;
      } else {
        throw new Error(result.error);
      }
    } catch (error) {
      console.error('Analysis failed:', error);
      throw new Error(error.message || 'Analysis failed. Please try again.');
    } finally {
      setIsAnalyzing(false);
    }
  };

  const clearAnalysis = () => {
    setAnalysisResult(null);
    setJobDescription('');
    setSelectedResume(null);
  };

  const value = {
    resumes,
    selectedResume,
    jobDescription,
    setJobDescription,
    analysisResult,
    isAnalyzing,
    isUploading,
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