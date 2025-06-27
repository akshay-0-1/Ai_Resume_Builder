import React, { useState, useEffect } from 'react';
import { resumeService } from '../api/resumeService';
import HistoryCard from '../components/history/HistoryCard';
import { motion } from 'framer-motion';
import Navbar from '../components/layout/Navbar';

const History = () => {
    const [history, setHistory] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchHistory = async () => {
            try {
                const response = await resumeService.getAnalysisHistory();
                if (response.success) {
                    setHistory(response.data || []);
                } else {
                    setError(response.error);
                }
            } catch (err) {
                setError('An unexpected error occurred.');
            } finally {
                setLoading(false);
            }
        };

        fetchHistory();
    }, []);

    if (loading) {
        return <div className="text-center p-8">Loading history...</div>;
    }

    if (error) {
        return <div className="text-center p-8 text-red-500">Error: {error}</div>;
    }

    return (
        <div className="min-h-screen bg-gray-50">
            <Navbar />
            <motion.div 
                className="container mx-auto p-4 md:p-8"
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.5 }}
            >
                <h1 className="text-3xl font-bold mb-6 text-gray-800">Analysis History</h1>
                {history.length === 0 ? (
                    <p className="text-gray-500">No analysis history found.</p>
                ) : (
                    <div className="space-y-4">
                        {history.map((analysis) => (
                            <HistoryCard key={analysis.analysisId} analysis={analysis} />
                        ))}
                    </div>
                )}
            </motion.div>
        </div>
    );
};

export default History;
