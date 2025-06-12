import React, { useEffect, useState } from 'react';
import { CircularProgressbar, buildStyles } from 'react-circular-progressbar';
import 'react-circular-progressbar/dist/styles.css';

const JobScoreCircle = ({ score, size = 200 }) => {
  const [animatedScore, setAnimatedScore] = useState(0);
  const numericScore = parseInt(score, 10) || 0;

  useEffect(() => {
    const timer = setTimeout(() => {
      setAnimatedScore(numericScore);
    }, 500);

    return () => clearTimeout(timer);
  }, [numericScore]);

  const getColor = (score) => {
    if (score >= 71) return '#10B981'; // Green
    if (score >= 41) return '#F59E0B'; // Yellow/Amber
    return '#EF4444'; // Red
  };

  const getScoreLabel = (score) => {
    if (score >= 90) return 'Excellent Match';
    if (score >= 80) return 'Great Match';
    if (score >= 70) return 'Good Match';
    if (score >= 60) return 'Fair Match';
    if (score >= 40) return 'Needs Work';
    return 'Poor Match';
  };

  const color = getColor(animatedScore);

  return (
    <div className="flex flex-col items-center space-y-4">
      <div style={{ width: size, height: size }}>
        <CircularProgressbar
          value={animatedScore}
          text={`${Math.round(animatedScore)}%`}
          styles={buildStyles({
            pathColor: color,
            textColor: color,
            trailColor: '#E5E7EB',
            backgroundColor: '#F3F4F6',
            textSize: '16px',
            pathTransitionDuration: 2,
          })}
        />
      </div>
      
      <div className="text-center">
        <h3 className="text-xl font-bold text-gray-900">
          Job Match Score
        </h3>
        <p 
          className="text-lg font-semibold mt-1"
          style={{ color }}
        >
          {getScoreLabel(animatedScore)}
        </p>
      </div>
    </div>
  );
};

export default JobScoreCircle;