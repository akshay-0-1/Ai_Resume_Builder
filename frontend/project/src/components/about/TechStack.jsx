import React from 'react';
import { 
  Code, 
  Database, 
  Terminal, 
  GitBranch, 
  Sparkles 
} from 'lucide-react';

const TechStack = () => {
  return (
    <section className="py-20">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center mb-16">
          <h2 className="text-4xl font-display font-bold text-gray-900 mb-4">Built with Modern Tech</h2>
          <p className="text-xl text-gray-600">Explore the technologies that power this resume builder</p>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          {/* Frontend Tech Stack */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-200/50 p-8">
            <div className="flex items-center justify-between mb-6">
              <div className="flex items-center space-x-3">
                <Code className="w-6 h-6 text-primary-500" />
                <h3 className="text-xl font-semibold text-gray-900">Frontend</h3>
              </div>
              <Sparkles className="w-4 h-4 text-accent-500 animate-pulse" />
            </div>
            <div className="space-y-4">
              <div className="flex items-center space-x-3">
                <Terminal className="w-4 h-4 text-gray-500" />
                <span className="text-gray-600">React 18</span>
              </div>
              <div className="flex items-center space-x-3">
                <Terminal className="w-4 h-4 text-gray-500" />
                <span className="text-gray-600">Tailwind CSS</span>
              </div>
              <div className="flex items-center space-x-3">
                <Terminal className="w-4 h-4 text-gray-500" />
                <span className="text-gray-600">Framer Motion</span>
              </div>
            </div>
          </div>

          {/* Backend Tech Stack */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-200/50 p-8">
            <div className="flex items-center justify-between mb-6">
              <div className="flex items-center space-x-3">
                <Database className="w-6 h-6 text-primary-500" />
                <h3 className="text-xl font-semibold text-gray-900">Backend</h3>
              </div>
              <Sparkles className="w-4 h-4 text-accent-500 animate-pulse" />
            </div>
            <div className="space-y-4">
              <div className="flex items-center space-x-3">
                <Terminal className="w-4 h-4 text-gray-500" />
                <span className="text-gray-600">Spring Boot</span>
              </div>
              <div className="flex items-center space-x-3">
                <Terminal className="w-4 h-4 text-gray-500" />
                <span className="text-gray-600">Java 17+</span>
              </div>
              <div className="flex items-center space-x-3">
                <Terminal className="w-4 h-4 text-gray-500" />
                <span className="text-gray-600">PostgreSQL</span>
              </div>
            </div>
          </div>

          {/* Development Process */}
          <div className="col-span-2">
            <div className="bg-white rounded-2xl shadow-sm border border-gray-200/50 p-8">
              <div className="flex items-center justify-between mb-6">
                <div className="flex items-center space-x-3">
                  <GitBranch className="w-6 h-6 text-primary-500" />
                  <h3 className="text-xl font-semibold text-gray-900">Development Process</h3>
                </div>
                <Sparkles className="w-4 h-4 text-accent-500 animate-pulse" />
              </div>
              <div className="space-y-6">
                <div>
                  <h4 className="text-lg font-semibold text-gray-900 mb-2">Frontend</h4>
                  <p className="text-gray-600">Modern UI components and responsive design</p>
                </div>
                <div>
                  <h4 className="text-lg font-semibold text-gray-900 mb-2">Backend</h4>
                  <p className="text-gray-600">RESTful APIs and data processing</p>
                </div>
                <div>
                  <h4 className="text-lg font-semibold text-gray-900 mb-2">Integration</h4>
                  <p className="text-gray-600">File processing and AI analysis</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default TechStack;
