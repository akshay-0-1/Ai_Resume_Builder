import React from 'react';
import { motion } from 'framer-motion';
import { useAnalysis } from '../../context/AnalysisContext';
import { CheckCircle, XCircle, Target, TrendingUp, Lightbulb } from 'lucide-react';
import JobScoreCircle from './JobScoreCircle';
import TargetedChangeCard from './TargetedChangeCard';
import Card from '../common/Card';
import Spinner from '../common/Spinner';
import Skeleton from '../common/Skeleton';

const AnalysisDisplay = () => {
  const { analysisResult, isAnalyzing } = useAnalysis();

  if (isAnalyzing) {
    return (
      <div className="space-y-6">
        {/* Job Score Skeleton */}
        <Card className="p-6">
          <div className="flex justify-center">
            <Skeleton className="w-48 h-48 rounded-full" />
          </div>
        </Card>

        {/* Keyword Match Skeleton */}
        <Card className="p-6 space-y-4">
          <Skeleton className="w-40 h-4" />
          <div className="grid grid-cols-2 gap-4">
            {[...Array(4)].map((_, idx) => (
              <Skeleton key={idx} className="h-3 w-full" />
            ))}
          </div>
        </Card>

        {/* Targeted Improvements Skeleton */}
        <Card className="p-6 space-y-3">
          <Skeleton className="w-48 h-4" />
          {[...Array(3)].map((_, idx) => (
            <Skeleton key={idx} className="h-3 w-full" />
          ))}
        </Card>

        {/* Overall Improvements Skeleton */}
        <Card className="p-6 space-y-3">
          <Skeleton className="w-48 h-4" />
          {[...Array(4)].map((_, idx) => (
            <Skeleton key={idx} className="h-3 w-full" />
          ))}
        </Card>
      </div>
    );
  }

  if (!analysisResult) {
    return (
      <div className="space-y-6">
        <Card className="p-8">
          <div className="text-center">
            <TrendingUp className="w-16 h-16 text-gray-300 mx-auto mb-4" />
            <h3 className="text-lg font-semibold text-gray-900 mb-2">
              Ready for Analysis
            </h3>
            <p className="text-gray-600">
              Upload a resume and provide a job description to get started with your personalized analysis.
            </p>
          </div>
        </Card>
      </div>
    );
  }

  const containerVariants = {
    hidden: {},
    show: {
      transition: { staggerChildren: 0.08 }
    }
  };

  const itemVariants = {
    hidden: { opacity: 0, y: 8 },
    show: { opacity: 1, y: 0 }
  };

  return (
    <motion.div className="space-y-6" initial="hidden" animate="show" variants={containerVariants}>
      {/* Job Score */}
      <motion.div variants={itemVariants}>
        <Card className="p-6">
          <div className="flex justify-center">
            <motion.div initial={{ scale: 0.9, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} transition={{ type: 'spring', stiffness: 260, damping: 20 }}>
              <motion.div animate={{ scale: [1, 1.05, 1] }} transition={{ duration: 2, repeat: Infinity, repeatDelay: 4 }}>
                <JobScoreCircle score={analysisResult?.jobScore} />
              </motion.div>
            </motion.div>
          </div>
        </Card>
      </motion.div>

      {/* Keyword Match */}
      {(analysisResult?.matchedKeywords?.length > 0 || analysisResult?.missingKeywords?.length > 0) && (
        <motion.div variants={itemVariants}>
          <Card className="p-6">
            <div className="flex items-center space-x-2 mb-4">
              <Target className="w-5 h-5 text-purple-600" />
              <h3 className="text-lg font-semibold text-gray-900">
                Keyword Match Analysis
              </h3>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {analysisResult.matchedKeywords?.length > 0 && (
                <div>
                  <h4 className="text-sm font-medium text-green-600 mb-2 flex items-center">
                    <CheckCircle className="w-4 h-4 mr-1" /> Matched Keywords
                  </h4>
                  <ul className="list-disc list-inside text-gray-700 space-y-1">
                    {analysisResult.matchedKeywords.map((kw, idx) => (
                      <motion.li key={idx} variants={itemVariants}>{kw}</motion.li>
                    ))}
                  </ul>
                </div>
              )}

              {analysisResult.missingKeywords?.length > 0 && (
                <div>
                  <h4 className="text-sm font-medium text-red-600 mb-2 flex items-center">
                    <XCircle className="w-4 h-4 mr-1" /> Missing Keywords
                  </h4>
                  <ul className="list-disc list-inside text-gray-700 space-y-1">
                    {analysisResult.missingKeywords.map((kw, idx) => (
                      <motion.li key={idx} variants={itemVariants}>{kw}</motion.li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          </Card>
        </motion.div>
      )}

      {/* Targeted Changes */}
      {analysisResult?.targetedChanges && analysisResult?.targetedChanges.length > 0 && (
        <motion.div variants={itemVariants}>
          <Card className="p-6">
            <div className="flex items-center space-x-2 mb-4">
              <Target className="w-5 h-5 text-blue-600" />
              <h3 className="text-lg font-semibold text-gray-900">
                Targeted Improvements
              </h3>
            </div>

            <p className="text-sm text-gray-600 mb-4">
              Specific changes to make your resume more aligned with this job:
            </p>

            <div className="space-y-3">
              {analysisResult.targetedChanges.map((change, index) => (
                <motion.div key={index} variants={itemVariants}>
                  <TargetedChangeCard
                    section={change.section}
                    suggestion={change.suggestion}
                  />
                </motion.div>
              ))}
            </div>
          </Card>
        </motion.div>
      )}

      {/* Overall Improvements */}
      {analysisResult?.overallImprovements && analysisResult?.overallImprovements.length > 0 && (
        <motion.div variants={itemVariants}>
          <Card className="p-6">
            <div className="flex items-center space-x-2 mb-4">
              <Lightbulb className="w-5 h-5 text-amber-600" />
              <h3 className="text-lg font-semibold text-gray-900">
                General Feedback
              </h3>
            </div>

            <p className="text-sm text-gray-600 mb-4">
              Overall observations and recommendations:
            </p>

            <div className="space-y-3">
              {analysisResult.overallImprovements.map((improvement, index) => (
                <motion.div key={index} className="flex items-start space-x-3" variants={itemVariants}>
                  <CheckCircle className="w-5 h-5 text-green-500 mt-0.5 flex-shrink-0" />
                  <p className="text-gray-700 leading-relaxed">
                    {improvement}
                  </p>
                </motion.div>
              ))}
            </div>
          </Card>
        </motion.div>
      )}
    </motion.div>
  );
};

export default AnalysisDisplay;