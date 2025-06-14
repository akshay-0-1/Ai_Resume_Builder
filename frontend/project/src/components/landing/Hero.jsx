import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { FileText, Sparkles, ArrowRight, CheckCircle } from 'lucide-react';
import Button from '../common/Button';

const Hero = () => {
  const { isAuthenticated } = useAuth();

  return (
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
  );
};

export default Hero;
