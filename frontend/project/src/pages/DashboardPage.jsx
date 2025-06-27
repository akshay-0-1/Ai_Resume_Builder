import React from 'react';
import { useAnalysis } from '../context/AnalysisContext';

import ResumeUploader from '../components/dashboard/ResumeUploader';
import JobDescriptionInput from '../components/dashboard/JobDescriptionInput';
import AnalysisDisplay from '../components/dashboard/AnalysisDisplay';
import ResumeDisplay from '../components/dashboard/ResumeDisplay';
import Button from '../components/common/Button';
import { BarChart3 } from 'lucide-react';
import { toast } from 'react-toastify';
import { motion, AnimatePresence } from 'framer-motion';
import { SPACING } from '../styles/tokens';
import HistoryCard from '../components/history/HistoryCard';

const DashboardPage = () => {
  const { 
    selectedResume, 
    jobDescription, 
    setJobDescription,
    analyzeResume, 
    isAnalyzing, 
    clearAnalysis,
    resumeHistory,
    fetchResumeHistory
  } = useAnalysis();

  const canAnalyze = selectedResume && jobDescription.trim().length >= 50;
  const MotionButton = motion(Button);

  const handleAnalyze = async () => {
    try {
      await analyzeResume();
    } catch (error) {
      toast.error(error.message);
    }
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -20 }}
      transition={{ duration: 0.4 }}
      className="min-h-screen bg-gray-50 dark:bg-gray-900"
    >

      <div className="max-w-7xl mx-auto p-6">
        {/* Header */}
        <div className="mb-[${SPACING[12]}]">
          <h1 className="text-3xl font-bold text-gray-900 mb-[${SPACING[4]}] dark:text-white">
            Resume Analysis Dashboard
          </h1>
          <p className="text-gray-600 dark:text-gray-300">
            Upload your resume and compare it with job descriptions to get personalized improvement suggestions.
          </p>
        </div>

        {/* Main Content - Single Column Layout */}
        <div className="space-y-[${SPACING[6]}]">
          {/* Input Section */}
          <div className="space-y-[${SPACING[6]}]">
            <ResumeUploader />
            <JobDescriptionInput value={jobDescription} onChange={setJobDescription} />

            {/* Analyze Button */}
            <div className="flex flex-col sm:flex-row gap-4">
               <MotionButton
                 onClick={handleAnalyze}
                 disabled={!canAnalyze}
                 isLoading={isAnalyzing}
                 size="lg"
                 className="flex-1"
                 whileHover={{ scale: 1.02 }}
                 whileTap={{ scale: 0.97 }}
               >
                 <BarChart3 className="w-5 h-5 mr-2" />
                 Analyze Resume
               </MotionButton>

               <MotionButton
                 onClick={clearAnalysis}
                 variant="secondary"
                 size="lg"
                 className="sm:w-auto"
                 whileHover={{ scale: 1.02 }}
                 whileTap={{ scale: 0.97 }}
               >
                 Clear
               </MotionButton>
            </div>

            {!canAnalyze && (
              <div className="bg-amber-50 border border-amber-200 rounded-lg p-[${SPACING[4]}] dark:bg-amber-900 dark:border-amber-800">
                <p className="text-sm text-amber-800 dark:text-amber-400">
                  {!selectedResume && "Please select a resume. "}
                  {selectedResume && jobDescription.trim().length < 50 && "Please provide a job description (minimum 50 characters)."}
                </p>
              </div>
            )}
          </div>

          {/* Analysis Section */}
          <div className="space-y-[${SPACING[6]}]">
            <AnimatePresence mode="wait">
              {!isAnalyzing && (
                <motion.div
                  key="analysis"
                  initial={{ opacity: 0, x: 20 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{ opacity: 0, x: -20 }}
                  transition={{ duration: 0.3, delay: 0.1 }}
                >
                  <AnalysisDisplay />
                </motion.div>
              )}
            </AnimatePresence>
          </div>
        </div>
      </div>
    </motion.div>
  );
};

export default DashboardPage;