import React from 'react';
import { Target } from 'lucide-react';
import Card from '../common/Card';
import { milestones } from './data.jsx';

const Timeline = () => (
  <section className="py-20">
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
      <div className="text-center mb-16">
        <h2 className="text-4xl font-display font-bold text-gray-900 mb-4">
          Our Journey
        </h2>
        <p className="text-xl text-gray-600">
          Key milestones in our mission to transform career success
        </p>
      </div>
      
      <div className="relative">
        <div className="absolute left-1/2 transform -translate-x-1/2 w-1 h-full bg-gradient-to-b from-primary-500 to-accent-500 rounded-full"></div>
        
        <div className="space-y-12">
          {milestones.map((milestone, index) => (
            <div key={index} className={`flex items-center ${index % 2 === 0 ? 'flex-row' : 'flex-row-reverse'}`}>
              <div className={`w-1/2 ${index % 2 === 0 ? 'pr-8 text-right' : 'pl-8 text-left'}`}>
                <Card hover gradient className="p-6">
                  <div className="text-2xl font-display font-bold gradient-text mb-2">
                    {milestone.year}
                  </div>
                  <h3 className="text-xl font-semibold text-gray-900 mb-3">
                    {milestone.title}
                  </h3>
                  <p className="text-gray-600 leading-relaxed">
                    {milestone.description}
                  </p>
                </Card>
              </div>
              
              <div className="relative z-10">
                <div className="w-12 h-12 bg-gradient-to-r from-primary-500 to-accent-500 rounded-full flex items-center justify-center shadow-lg">
                  <Target className="w-6 h-6 text-white" />
                </div>
              </div>
              
              <div className="w-1/2"></div>
            </div>
          ))}
        </div>
      </div>
    </div>
  </section>
);

export default Timeline;
