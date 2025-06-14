import React from 'react';
import { Sparkles } from 'lucide-react';

const Hero = () => (
  <section className="relative overflow-hidden py-20">
    <div className="absolute inset-0 bg-gradient-to-r from-primary-600/10 to-accent-500/10"></div>
    <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div className="text-center">
        <div className="flex justify-center mb-6">
          <div className="w-20 h-20 bg-gradient-to-r from-primary-500 to-accent-500 rounded-2xl flex items-center justify-center shadow-2xl">
            <Sparkles className="w-10 h-10 text-white" />
          </div>
        </div>
        
        <h1 className="text-5xl md:text-6xl font-display font-bold text-gray-900 mb-6">
          About <span className="gradient-text">ResumeAI</span>
        </h1>
        
        <p className="text-xl text-gray-600 mb-8 max-w-4xl mx-auto leading-relaxed">
          We're on a mission to revolutionize the job search process by making professional 
          resume analysis and career guidance accessible to everyone through the power of artificial intelligence.
        </p>
      </div>
    </div>
  </section>
);

export default Hero;
