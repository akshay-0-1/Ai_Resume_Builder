import React from 'react';
import Card from '../common/Card';
import { values } from './data.jsx';

const Values = () => (
  <section className="py-20">
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div className="text-center mb-16">
        <h2 className="text-4xl font-display font-bold text-gray-900 mb-4">
          Our Values
        </h2>
        <p className="text-xl text-gray-600 max-w-3xl mx-auto">
          These core values guide everything we do and shape how we build products that truly serve our users.
        </p>
      </div>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
        {values.map((value, index) => (
          <Card key={index} hover gradient className="p-8 text-center group">
            <div className="w-16 h-16 bg-gradient-to-r from-primary-500 to-accent-500 rounded-2xl flex items-center justify-center mx-auto mb-6 text-white group-hover:scale-110 transition-transform duration-300">
              {value.icon}
            </div>
            <h3 className="text-xl font-display font-semibold text-gray-900 mb-4">
              {value.title}
            </h3>
            <p className="text-gray-600 leading-relaxed">
              {value.description}
            </p>
          </Card>
        ))}
      </div>
    </div>
  </section>
);

export default Values;
