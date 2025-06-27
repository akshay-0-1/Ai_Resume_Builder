import React from 'react';
import { Zap, Target, TrendingUp, Shield, Clock, Users } from 'lucide-react';

export const features = [
  {
    icon: <Zap className="w-6 h-6" />,
    title: 'AI-Powered Analysis',
    description: 'Advanced AI algorithms analyze your resume against job requirements for precise matching.'
  },
  {
    icon: <Target className="w-6 h-6" />,
    title: 'Targeted Suggestions',
    description: 'Get specific, actionable recommendations to improve your resume for each job application.'
  },
  {
    icon: <TrendingUp className="w-6 h-6" />,
    title: 'Score Tracking',
    description: 'Visual job match scores help you understand how well your resume fits different positions.'
  },
  {
    icon: <Shield className="w-6 h-6" />,
    title: 'Secure & Private',
    description: 'Your resume data is encrypted and secure. We never share your information with third parties.'
  },
  {
    icon: <Clock className="w-6 h-6" />,
    title: 'Instant Results',
    description: 'Get comprehensive analysis and suggestions in seconds, not hours.'
  },
  {
    icon: <Users className="w-6 h-6" />,
    title: 'Trusted by Professionals',
    description: 'Join thousands of job seekers who have improved their success rate with ResumeAI.'
  }
];

export const stats = [
  { number: '100%', label: 'Free to Use' },
  { number: '85%', label: 'Success Rate' },
  { number: '2.5x', label: 'More Interviews' },
  { number: '24/7', label: 'Available' }
];
