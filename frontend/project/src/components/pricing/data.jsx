import React from 'react';
import { Star, Zap, Crown } from 'lucide-react';

export const plans = [
  {
    name: 'Free',
    price: '$0',
    period: 'forever',
    description: 'Perfect for getting started with resume analysis',
    icon: <Star className="w-6 h-6" />,
    color: 'from-gray-500 to-gray-600',
    features: [
      '3 resume analyses per month',
      'Basic job match scoring',
      'General improvement suggestions',
      'PDF resume upload',
      'Email support'
    ],
    limitations: [
      'Limited to 3 analyses',
      'Basic suggestions only',
      'No priority support'
    ],
    cta: 'Get Started Free',
    popular: false
  },
  {
    name: 'Pro',
    price: '$19',
    period: 'per month',
    description: 'Ideal for active job seekers and career changers',
    icon: <Zap className="w-6 h-6" />,
    color: 'from-primary-500 to-primary-600',
    features: [
      'Unlimited resume analyses',
      'Advanced AI-powered insights',
      'Targeted improvement suggestions',
      'Industry-specific recommendations',
      'ATS optimization tips',
      'Resume history tracking',
      'Priority email support',
      'Export analysis reports'
    ],
    limitations: [],
    cta: 'Start Pro Trial',
    popular: true
  },
  {
    name: 'Enterprise',
    price: '$49',
    period: 'per month',
    description: 'For professionals and teams who need the best',
    icon: <Crown className="w-6 h-6" />,
    color: 'from-accent-500 to-accent-600',
    features: [
      'Everything in Pro',
      'Team collaboration features',
      'Custom branding options',
      'Advanced analytics dashboard',
      'API access for integrations',
      'Dedicated account manager',
      '24/7 phone & chat support',
      'Custom AI model training'
    ],
    limitations: [],
    cta: 'Contact Sales',
    popular: false
  }
];

export const faqs = [
  {
    question: 'How accurate is the AI analysis?',
    answer: 'Our AI engine has been trained on thousands of successful resumes and job descriptions, achieving a 95% accuracy rate in identifying improvement opportunities.'
  },
  {
    question: 'Can I cancel my subscription anytime?',
    answer: 'Yes, you can cancel your subscription at any time. There are no long-term contracts or cancellation fees.'
  },
  {
    question: 'Do you offer refunds?',
    answer: 'We offer a 30-day money-back guarantee for all paid plans. If you\'re not satisfied, we\'ll provide a full refund.'
  },
  {
    question: 'Is my resume data secure?',
    answer: 'Absolutely. We use enterprise-grade encryption and never share your personal information with third parties. Your data is completely secure.'
  },
  {
    question: 'What file formats do you support?',
    answer: 'We support PDF, DOC, and DOCX formats. We recommend PDF for the best analysis results.'
  },
  {
    question: 'Do you offer team discounts?',
    answer: 'Yes, we offer volume discounts for teams of 5 or more users. Contact our sales team for custom pricing.'
  }
];
