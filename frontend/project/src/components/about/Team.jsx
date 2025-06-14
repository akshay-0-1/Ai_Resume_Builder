import React from 'react';
import { Award } from 'lucide-react';
import Card from '../common/Card';
import { team } from './data.jsx';

const Team = () => (
  <section className="py-20 bg-gradient-to-r from-primary-50 to-accent-50">
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div className="text-center mb-16">
        <h2 className="text-4xl font-display font-bold text-gray-900 mb-4">
          Meet Our Team
        </h2>
        <p className="text-xl text-gray-600">
          The passionate individuals behind ResumeAI's success
        </p>
      </div>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
        {team.map((member, index) => (
          <Card key={index} hover className="p-6 text-center group">
            <div className="relative mb-6">
              <img
                src={member.image}
                alt={member.name}
                className="w-24 h-24 rounded-full mx-auto object-cover group-hover:scale-110 transition-transform duration-300"
                loading="lazy"
              />
              <div className="absolute -bottom-2 -right-2 w-8 h-8 bg-gradient-to-r from-primary-500 to-accent-500 rounded-full flex items-center justify-center">
                <Award className="w-4 h-4 text-white" />
              </div>
            </div>
            <h3 className="text-lg font-display font-semibold text-gray-900 mb-2">
              {member.name}
            </h3>
            <p className="text-primary-600 font-medium mb-3">
              {member.role}
            </p>
            <p className="text-sm text-gray-600 leading-relaxed">
              {member.bio}
            </p>
          </Card>
        ))}
      </div>
    </div>
  </section>
);

export default Team;
