import React from 'react';
import { Briefcase } from 'lucide-react';
import Card from '../common/Card';

const JobDescriptionInput = ({ value, onChange }) => {
  return (
    <Card className="p-6">
      <div className="flex items-center space-x-2 mb-4">
        <Briefcase className="w-5 h-5 text-blue-600" />
        <h3 className="text-lg font-semibold text-gray-900">Job Description</h3>
      </div>
      
      <div className="space-y-4">
        <p className="text-sm text-gray-600">
          Paste the job description below to get personalized recommendations for your resume.
        </p>
        
        <textarea
          value={value}
          onChange={(e) => onChange(e.target.value)}
          placeholder="Paste the job description here...

Example:
We are looking for a Senior React Developer with 3+ years of experience in building modern web applications. Requirements include:
- Proficiency in React, TypeScript, and Node.js
- Experience with state management (Redux, Context API)
- Knowledge of modern CSS frameworks
- Understanding of REST APIs and GraphQL
- Experience with testing frameworks (Jest, React Testing Library)
- Familiarity with CI/CD pipelines"
          className="w-full h-64 p-4 border border-gray-300 rounded-lg resize-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
          rows={12}
        />
        
        <div className="flex justify-between items-center text-xs text-gray-500">
          <span>{value ? value.length : 0} characters</span>
          <span>Minimum 50 characters recommended</span>
        </div>
      </div>
    </Card>
  );
};

export default JobDescriptionInput;