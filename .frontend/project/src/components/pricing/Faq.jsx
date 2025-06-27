import React from 'react';
import Card from '../common/Card';
import { faqs } from './data.jsx';

const Faq = () => (
  <section className="py-20">
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
      <div className="text-center mb-16">
        <h2 className="text-4xl font-display font-bold text-gray-900 mb-4">
          Frequently Asked Questions
        </h2>
        <p className="text-xl text-gray-600">
          Everything you need to know about ResumeAI
        </p>
      </div>
      
      <div className="space-y-6">
        {faqs.map((faq, index) => (
          <Card key={index} className="p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-3">
              {faq.question}
            </h3>
            <p className="text-gray-600 leading-relaxed">
              {faq.answer}
            </p>
          </Card>
        ))}
      </div>
    </div>
  </section>
);

export default Faq;
