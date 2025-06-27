import React from 'react';
import { Rocket, Sparkles } from 'lucide-react';

const Hero = () => (
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
);

export default Hero;
