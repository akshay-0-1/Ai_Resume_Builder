import React from 'react';
import { motion } from 'framer-motion';
import { Eye } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const HistoryCard = ({ analysis }) => {
  const navigate = useNavigate();
  
  const handleViewResume = () => {
    navigate(`/resumes/${analysis.resumeId}`);
  };
    if (!analysis) {
        return null;
    }

    return (
        <motion.div 
            className="bg-white p-4 rounded-lg shadow-sm border border-gray-200 hover:shadow-md transition-shadow duration-200"
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.3 }}
        >
            <div className="flex justify-between items-center">
                <div className="flex flex-col">
                    <h3 className="text-lg font-semibold text-gray-800 truncate" title={analysis.resumeFilename}>
                      {analysis.resumeFilename}
                    </h3>
                    <div className="mt-2 text-sm text-gray-500">
                      Analyzed on: {new Date(analysis.createdAt).toLocaleString()}
                    </div>
                </div>
                <div className="flex items-center">
                    <button
                      onClick={handleViewResume}
                      className="p-2 rounded-lg hover:bg-gray-100 transition-colors"
                      title="View Resume"
                    >
                      <Eye className="w-5 h-5 text-gray-600 hover:text-gray-800" />
                    </button>
                    <span 
                      className={`px-3 py-1 text-sm font-semibold rounded-full ${analysis.jobScore > 75 ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'}`}>
                      Score: {analysis.jobScore}
                    </span>
                </div>
            </div>
        </motion.div>
    );
};

export default HistoryCard;
