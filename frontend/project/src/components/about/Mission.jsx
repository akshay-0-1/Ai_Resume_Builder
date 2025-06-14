import React from 'react';
import { TrendingUp } from 'lucide-react';

const Mission = () => (
  <section className="py-20 bg-white/50 backdrop-blur-sm">
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
        <div>
          <h2 className="text-4xl font-display font-bold text-gray-900 mb-6">
            Our Mission
          </h2>
          <p className="text-lg text-gray-600 mb-6 leading-relaxed">
            At ResumeAI, we believe that finding the right job shouldn't be a matter of luck or connections. 
            Our advanced AI technology levels the playing field by providing everyone with access to 
            professional-grade resume analysis and career guidance.
          </p>
          <p className="text-lg text-gray-600 leading-relaxed">
            We've helped thousands of job seekers improve their resumes, increase their interview rates, 
            and ultimately land their dream jobs. Our goal is to make career success achievable for everyone, 
            regardless of their background or experience level.
          </p>
        </div>
        <div className="relative">
          <img
            src="https://images.pexels.com/photos/3184360/pexels-photo-3184360.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop"
            alt="Team collaboration"
            className="rounded-2xl shadow-2xl"
            loading="lazy"
          />
          <div className="absolute -bottom-6 -right-6 w-32 h-32 bg-gradient-to-r from-primary-500 to-accent-500 rounded-2xl flex items-center justify-center shadow-xl">
            <TrendingUp className="w-16 h-16 text-white" />
          </div>
        </div>
      </div>
    </div>
  </section>
);

export default Mission;
