import React from 'react';
import Card from '../common/Card';
import { features } from './data.jsx';

const Features = () => (
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
);

export default Features;
