import React from 'react';
import { Check, Zap } from 'lucide-react';

const Features = () => (
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
            loading="lazy"
          />
          <div className="absolute -bottom-6 -right-6 w-32 h-32 bg-gradient-to-r from-primary-500 to-accent-500 rounded-2xl flex items-center justify-center shadow-xl">
            <Zap className="w-16 h-16 text-white" />
          </div>
        </div>
      </div>
    </div>
  </section>
);

export default Features;
