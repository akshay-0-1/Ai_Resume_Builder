import React from 'react';
import { motion } from 'framer-motion';

const HistoryCard = ({ analysis }) => {
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
                <h3 className="text-lg font-semibold text-gray-800 truncate" title={analysis.resumeFilename}>
                    {analysis.resumeFilename}
                </h3>
                <span 
                    className={`px-3 py-1 text-sm font-semibold rounded-full ${analysis.jobScore > 75 ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'}`}>
                    Score: {analysis.jobScore}
                </span>
            </div>
            <div className="mt-2 text-sm text-gray-500">
                Analyzed on: {new Date(analysis.createdAt).toLocaleString()}
            </div>
        </motion.div>
    );
};

export default HistoryCard;
