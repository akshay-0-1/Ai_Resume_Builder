import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { 
  FileText, 
  Sparkles, 
  Target, 
  TrendingUp, 
  Users, 
  CheckCircle, 
  ArrowRight,
  Star,
  Zap,
  Shield,
  Clock
} from 'lucide-react';
import Button from '../components/common/Button';
import Card from '../components/common/Card';
import Navbar from '../components/layout/Navbar';
import SuccessStories from '../components/landing/SuccessStories';

const LandingPage = () => {
  const { isAuthenticated } = useAuth();

  const features = [
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

  const stats = [
    { number: '50K+', label: 'Resumes Analyzed' },
    { number: '85%', label: 'Success Rate' },
    { number: '2.5x', label: 'More Interviews' },
    { number: '24/7', label: 'Available' }
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 via-white to-blue-50">
      <Navbar />
      
      {/* Hero Section */}
      <section className="relative overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-r from-primary-600/10 to-accent-500/10"></div>
        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
          <div className="text-center">
            <div className="flex justify-center mb-6">
              <div className="relative">
                <div className="w-20 h-20 bg-gradient-to-r from-primary-500 to-accent-500 rounded-2xl flex items-center justify-center shadow-2xl animate-bounce-gentle">
                  <FileText className="w-10 h-10 text-white" />
                </div>
                <Sparkles className="w-6 h-6 text-accent-500 absolute -top-2 -right-2 animate-pulse" />
              </div>
            </div>
            
            <h1 className="text-5xl md:text-7xl font-display font-bold text-gray-900 mb-6 animate-fade-in">
              Land Your{' '}
              <span className="gradient-text">Dream Job</span>
              <br />
              with AI-Powered Resumes
            </h1>
            
            <p className="text-xl text-gray-600 mb-8 max-w-3xl mx-auto leading-relaxed animate-slide-up">
              Transform your resume with intelligent analysis, get targeted improvement suggestions, 
              and increase your interview success rate by up to 300%.
            </p>
            
            <div className="flex flex-col sm:flex-row gap-4 justify-center items-center animate-slide-up">
              {isAuthenticated ? (
                <Link to="/dashboard">
                  <Button size="xl" className="group">
                    Go to Dashboard
                    <ArrowRight className="w-5 h-5 ml-2 group-hover:translate-x-1 transition-transform" />
                  </Button>
                </Link>
              ) : (
                <>
                  <Link to="/signup">
                    <Button size="xl" className="group">
                      Get Started Free
                      <ArrowRight className="w-5 h-5 ml-2 group-hover:translate-x-1 transition-transform" />
                    </Button>
                  </Link>
                  <Link to="/about">
                    <Button variant="outline" size="xl">
                      Learn More
                    </Button>
                  </Link>
                </>
              )}
            </div>
            
            <div className="mt-12 flex justify-center items-center space-x-8 text-sm text-gray-500">
              <div className="flex items-center space-x-2">
                <CheckCircle className="w-4 h-4 text-accent-500" />
                <span>No Credit Card Required</span>
              </div>
              <div className="flex items-center space-x-2">
                <CheckCircle className="w-4 h-4 text-accent-500" />
                <span>Free Forever Plan</span>
              </div>
              <div className="flex items-center space-x-2">
                <CheckCircle className="w-4 h-4 text-accent-500" />
                <span>Instant Results</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Stats Section */}
      <section className="py-16 bg-white/50 backdrop-blur-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-8">
            {stats.map((stat, index) => (
              <div key={index} className="text-center">
                <div className="text-4xl md:text-5xl font-display font-bold gradient-text mb-2">
                  {stat.number}
                </div>
                <div className="text-gray-600 font-medium">{stat.label}</div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-20">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-4xl md:text-5xl font-display font-bold text-gray-900 mb-4">
              Why Choose <span className="gradient-text">ResumeAI</span>?
            </h2>
            <p className="text-xl text-gray-600 max-w-3xl mx-auto">
              Our advanced AI technology provides comprehensive resume analysis and personalized recommendations 
              to help you stand out from the competition.
            </p>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {features.map((feature, index) => (
              <Card key={index} hover gradient className="p-8 text-center group">
                <div className="w-16 h-16 bg-gradient-to-r from-primary-500 to-accent-500 rounded-2xl flex items-center justify-center mx-auto mb-6 text-white group-hover:scale-110 transition-transform duration-300">
                  {feature.icon}
                </div>
                <h3 className="text-xl font-display font-semibold text-gray-900 mb-4">
                  {feature.title}
                </h3>
                <p className="text-gray-600 leading-relaxed">
                  {feature.description}
                </p>
              </Card>
            ))}
          </div>
        </div>
      </section>

      <SuccessStories />

      {/* CTA Section */}
      <section className="py-20 bg-gradient-to-r from-primary-600 to-accent-600">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-4xl md:text-5xl font-display font-bold text-white mb-6">
            Ready to Transform Your Career?
          </h2>
          <p className="text-xl text-primary-100 mb-8 leading-relaxed">
            Join thousands of professionals who have already improved their job search success with ResumeAI.
          </p>
          {!isAuthenticated && (
            <Link to="/signup">
              <Button size="xl" variant="secondary" className="group">
                Start Your Free Analysis
                <ArrowRight className="w-5 h-5 ml-2 group-hover:translate-x-1 transition-transform" />
              </Button>
            </Link>
          )}
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-gray-900 text-white py-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
            <div className="col-span-1 md:col-span-2">
              <div className="flex items-center mb-4">
                <FileText className="h-8 w-8 text-primary-400 mr-2" />
                <span className="text-xl font-display font-bold">ResumeAI</span>
              </div>
              <p className="text-gray-400 mb-4 leading-relaxed">
                Empowering job seekers with AI-powered resume analysis and career guidance. 
                Transform your job search and land your dream position.
              </p>
            </div>
            
            <div>
              <h3 className="font-semibold mb-4">Product</h3>
              <ul className="space-y-2 text-gray-400">
                <li><Link to="/about" className="hover:text-white transition-colors">About</Link></li>
                <li><Link to="/pricing" className="hover:text-white transition-colors">Pricing</Link></li>
                <li><a href="#" className="hover:text-white transition-colors">Features</a></li>
              </ul>
            </div>
            
            <div>
              <h3 className="font-semibold mb-4">Support</h3>
              <ul className="space-y-2 text-gray-400">
                <li><a href="#" className="hover:text-white transition-colors">Help Center</a></li>
                <li><a href="#" className="hover:text-white transition-colors">Contact Us</a></li>
                <li><a href="#" className="hover:text-white transition-colors">Privacy Policy</a></li>
              </ul>
            </div>
          </div>
          
          <div className="border-t border-gray-800 mt-8 pt-8 text-center text-gray-400">
            <p>&copy; 2024 ResumeAI. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default LandingPage;