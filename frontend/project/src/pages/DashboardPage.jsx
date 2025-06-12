import React from 'react';
import { useAnalysis } from '../context/AnalysisContext';
import Navbar from '../components/layout/Navbar';
import ResumeUploader from '../components/dashboard/ResumeUploader';
import JobDescriptionInput from '../components/dashboard/JobDescriptionInput';
import AnalysisDisplay from '../components/dashboard/AnalysisDisplay';
import Button from '../components/common/Button';
import { BarChart3 } from 'lucide-react';
import toast from 'react-hot-toast';

const DashboardPage = () => {
  const { 
    selectedResume, 
    jobDescription, 
    setJobDescription,
    analyzeResume, 
    isAnalyzing, 
    clearAnalysis 
  } = useAnalysis();

  const canAnalyze = selectedResume && jobDescription.trim().length >= 50;

  const handleAnalyze = async () => {
    try {
      await analyzeResume();
    } catch (error) {
      toast.error(error.message);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      
      <div className="max-w-7xl mx-auto p-6">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            Resume Analysis Dashboard
          </h1>
          <p className="text-gray-600">
            Upload your resume and compare it with job descriptions to get personalized improvement suggestions.
          </p>
        </div>

        {/* Main Content - Split Layout */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Left Column - Input */}
          <div className="space-y-6">
            <ResumeUploader />
            <JobDescriptionInput value={jobDescription} onChange={setJobDescription} />
            
            {/* Analyze Button */}
            <div className="flex flex-col sm:flex-row gap-4">
              <Button
                onClick={handleAnalyze}
                disabled={!canAnalyze}
                isLoading={isAnalyzing}
                size="lg"
                className="flex-1"
              >
                <BarChart3 className="w-5 h-5 mr-2" />
                Analyze Resume
              </Button>
              
              <Button
                onClick={clearAnalysis}
                variant="secondary"
                size="lg"
                className="sm:w-auto"
              >
                Clear
              </Button>
            </div>
            
            {!canAnalyze && (
              <div className="bg-amber-50 border border-amber-200 rounded-lg p-4">
                <p className="text-sm text-amber-800">
                  {!selectedResume && "Please select a resume. "}
                  {selectedResume && jobDescription.trim().length < 50 && "Please provide a job description (minimum 50 characters)."}
                </p>
              </div>
            )}
          </div>

          {/* Right Column - Output */}
          <div className="space-y-6">
            <AnalysisDisplay />
          </div>
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;