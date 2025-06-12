import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { 
  Check, 
  Star, 
  Zap, 
  Crown, 
  Rocket,
  ArrowRight,
  Sparkles
} from 'lucide-react';
import Navbar from '../components/layout/Navbar';
import Card from '../components/common/Card';
import Button from '../components/common/Button';

const PricingPage = () => {
  const { isAuthenticated } = useAuth();

  const plans = [
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

  const faqs = [
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

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 via-white to-blue-50">
      <Navbar />
      
      {/* Hero Section */}
      <section className="relative overflow-hidden py-20">
        <div className="absolute inset-0 bg-gradient-to-r from-primary-600/10 to-accent-500/10"></div>
        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center">
            <div className="flex justify-center mb-6">
              <div className="w-20 h-20 bg-gradient-to-r from-primary-500 to-accent-500 rounded-2xl flex items-center justify-center shadow-2xl">
                <Rocket className="w-10 h-10 text-white" />
              </div>
            </div>
            
            <h1 className="text-5xl md:text-6xl font-display font-bold text-gray-900 mb-6">
              Choose Your <span className="gradient-text">Success Plan</span>
            </h1>
            
            <p className="text-xl text-gray-600 mb-8 max-w-3xl mx-auto leading-relaxed">
              Unlock the full potential of AI-powered resume analysis. Choose the plan that fits your career goals 
              and start landing more interviews today.
            </p>
            
            <div className="inline-flex items-center space-x-2 bg-accent-50 text-accent-700 px-4 py-2 rounded-full text-sm font-medium">
              <Sparkles className="w-4 h-4" />
              <span>30-day money-back guarantee on all paid plans</span>
            </div>
          </div>
        </div>
      </section>

      {/* Pricing Cards */}
      <section className="py-20">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {plans.map((plan, index) => (
              <Card 
                key={index} 
                className={`relative p-8 ${plan.popular ? 'ring-2 ring-primary-500 shadow-2xl scale-105' : ''}`}
                hover
              >
                {plan.popular && (
                  <div className="absolute -top-4 left-1/2 transform -translate-x-1/2">
                    <div className="bg-gradient-to-r from-primary-500 to-accent-500 text-white px-6 py-2 rounded-full text-sm font-semibold shadow-lg">
                      Most Popular
                    </div>
                  </div>
                )}
                
                <div className="text-center">
                  <div className={`w-16 h-16 bg-gradient-to-r ${plan.color} rounded-2xl flex items-center justify-center mx-auto mb-6 text-white`}>
                    {plan.icon}
                  </div>
                  
                  <h3 className="text-2xl font-display font-bold text-gray-900 mb-2">
                    {plan.name}
                  </h3>
                  
                  <div className="mb-4">
                    <span className="text-4xl font-display font-bold text-gray-900">
                      {plan.price}
                    </span>
                    <span className="text-gray-600 ml-2">
                      {plan.period}
                    </span>
                  </div>
                  
                  <p className="text-gray-600 mb-8 leading-relaxed">
                    {plan.description}
                  </p>
                  
                  <div className="space-y-4 mb-8">
                    {plan.features.map((feature, featureIndex) => (
                      <div key={featureIndex} className="flex items-center space-x-3">
                        <Check className="w-5 h-5 text-accent-500 flex-shrink-0" />
                        <span className="text-gray-700">{feature}</span>
                      </div>
                    ))}
                  </div>
                  
                  {isAuthenticated ? (
                    <Button 
                      className="w-full" 
                      variant={plan.popular ? 'primary' : 'outline'}
                      size="lg"
                    >
                      {plan.cta}
                    </Button>
                  ) : (
                    <Link to="/signup">
                      <Button 
                        className="w-full" 
                        variant={plan.popular ? 'primary' : 'outline'}
                        size="lg"
                      >
                        {plan.cta}
                      </Button>
                    </Link>
                  )}
                </div>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* Features Comparison */}
      <section className="py-20 bg-white/50 backdrop-blur-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-display font-bold text-gray-900 mb-4">
              Why Upgrade to Pro?
            </h2>
            <p className="text-xl text-gray-600">
              See the difference our advanced features can make in your job search
            </p>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-12 items-center">
            <div>
              <h3 className="text-2xl font-display font-bold text-gray-900 mb-6">
                Advanced AI Analysis
              </h3>
              <div className="space-y-4">
                <div className="flex items-start space-x-3">
                  <Check className="w-5 h-5 text-accent-500 mt-1 flex-shrink-0" />
                  <div>
                    <h4 className="font-semibold text-gray-900">Industry-Specific Insights</h4>
                    <p className="text-gray-600">Get recommendations tailored to your specific industry and role.</p>
                  </div>
                </div>
                <div className="flex items-start space-x-3">
                  <Check className="w-5 h-5 text-accent-500 mt-1 flex-shrink-0" />
                  <div>
                    <h4 className="font-semibold text-gray-900">ATS Optimization</h4>
                    <p className="text-gray-600">Ensure your resume passes Applicant Tracking Systems.</p>
                  </div>
                </div>
                <div className="flex items-start space-x-3">
                  <Check className="w-5 h-5 text-accent-500 mt-1 flex-shrink-0" />
                  <div>
                    <h4 className="font-semibold text-gray-900">Unlimited Analyses</h4>
                    <p className="text-gray-600">Analyze as many resumes as you need without restrictions.</p>
                  </div>
                </div>
              </div>
            </div>
            
            <div className="relative">
              <img
                src="https://images.pexels.com/photos/3184291/pexels-photo-3184291.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop"
                alt="Advanced analytics"
                className="rounded-2xl shadow-2xl"
              />
              <div className="absolute -bottom-6 -right-6 w-32 h-32 bg-gradient-to-r from-primary-500 to-accent-500 rounded-2xl flex items-center justify-center shadow-xl">
                <Zap className="w-16 h-16 text-white" />
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* FAQ Section */}
      <section className="py-20">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-display font-bold text-gray-900 mb-4">
              Frequently Asked Questions
            </h2>
            <p className="text-xl text-gray-600">
              Everything you need to know about ResumeAI
            </p>
          </div>
          
          <div className="space-y-6">
            {faqs.map((faq, index) => (
              <Card key={index} className="p-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-3">
                  {faq.question}
                </h3>
                <p className="text-gray-600 leading-relaxed">
                  {faq.answer}
                </p>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 bg-gradient-to-r from-primary-600 to-accent-600">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-4xl font-display font-bold text-white mb-6">
            Ready to Accelerate Your Career?
          </h2>
          <p className="text-xl text-primary-100 mb-8 leading-relaxed">
            Join thousands of professionals who have already transformed their job search with ResumeAI.
          </p>
          {!isAuthenticated && (
            <Link to="/signup">
              <Button size="xl" variant="secondary" className="group">
                Start Your Free Trial
                <ArrowRight className="w-5 h-5 ml-2 group-hover:translate-x-1 transition-transform" />
              </Button>
            </Link>
          )}
        </div>
      </section>
    </div>
  );
};

export default PricingPage;