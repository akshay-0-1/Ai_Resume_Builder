import React from 'react';
import { Heart, Lightbulb, Users, Globe, Award, Target } from 'lucide-react';

export const team = [
  {
    name: 'Alex Johnson',
    role: 'CEO & Co-Founder',
    image: 'https://images.pexels.com/photos/2379004/pexels-photo-2379004.jpeg?auto=compress&cs=tinysrgb&w=300&h=300&fit=crop',
    bio: 'Former Google engineer with 10+ years in AI and machine learning.'
  },
  {
    name: 'Sarah Chen',
    role: 'CTO & Co-Founder',
    image: 'https://images.pexels.com/photos/1239291/pexels-photo-1239291.jpeg?auto=compress&cs=tinysrgb&w=300&h=300&fit=crop',
    bio: 'AI researcher and former Microsoft principal engineer specializing in NLP.'
  },
  {
    name: 'Michael Rodriguez',
    role: 'Head of Product',
    image: 'https://images.pexels.com/photos/1222271/pexels-photo-1222271.jpeg?auto=compress&cs=tinysrgb&w=300&h=300&fit=crop',
    bio: 'Product leader with experience at LinkedIn and career development platforms.'
  },
  {
    name: 'Emily Davis',
    role: 'Head of Design',
    image: 'https://images.pexels.com/photos/774909/pexels-photo-774909.jpeg?auto=compress&cs=tinysrgb&w=300&h=300&fit=crop',
    bio: 'UX designer passionate about creating intuitive experiences for job seekers.'
  }
];

export const values = [
  {
    icon: <Heart className="w-8 h-8" />,
    title: 'Empowerment',
    description: 'We believe everyone deserves access to tools that help them succeed in their career journey.'
  },
  {
    icon: <Lightbulb className="w-8 h-8" />,
    title: 'Innovation',
    description: 'We continuously push the boundaries of AI technology to provide cutting-edge solutions.'
  },
  {
    icon: <Users className="w-8 h-8" />,
    title: 'Community',
    description: 'We foster a supportive community where job seekers can learn and grow together.'
  },
  {
    icon: <Globe className="w-8 h-8" />,
    title: 'Accessibility',
    description: 'We make professional career tools accessible to everyone, regardless of background.'
  }
];

export const milestones = [
  {
    year: '2022',
    title: 'Company Founded',
    description: 'ResumeAI was born from the vision to democratize career success through AI.'
  },
  {
    year: '2023',
    title: '10K Users',
    description: 'Reached our first major milestone with 10,000 active users.'
  },
  {
    year: '2023',
    title: 'AI Engine v2.0',
    description: 'Launched our advanced AI analysis engine with 95% accuracy.'
  },
  {
    year: '2024',
    title: '50K+ Resumes',
    description: 'Successfully analyzed over 50,000 resumes with 85% success rate.'
  }
];
