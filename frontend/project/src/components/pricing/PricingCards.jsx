import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Check } from 'lucide-react';
import Card from '../common/Card';
import Button from '../common/Button';
import { plans } from './data.jsx';

const PricingCards = () => {
  const { isAuthenticated } = useAuth();

  return (
    <section className="py-20">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {plans.map((plan, index) => (
            <Card 
              key={index} 
              className={`relative p-8 ${plan.popular ? 'ring-2 ring-primary-500 shadow-2xl scale-105' : ''}`}
              hover
            >
              {plan.popular && (
                <div className="absolute -top-4 left-1/2 transform -translate-x-1/2">
                  <div className="bg-gradient-to-r from-primary-500 to-accent-500 text-white px-6 py-2 rounded-full text-sm font-semibold shadow-lg">
                    Most Popular
                  </div>
                </div>
              )}
              
              <div className="text-center">
                <div className={`w-16 h-16 bg-gradient-to-r ${plan.color} rounded-2xl flex items-center justify-center mx-auto mb-6 text-white`}>
                  {plan.icon}
                </div>
                
                <h3 className="text-2xl font-display font-bold text-gray-900 mb-2">
                  {plan.name}
                </h3>
                
                <div className="mb-4">
                  <span className="text-4xl font-display font-bold text-gray-900">
                    {plan.price}
                  </span>
                  <span className="text-gray-600 ml-2">
                    {plan.period}
                  </span>
                </div>
                
                <p className="text-gray-600 mb-8 leading-relaxed">
                  {plan.description}
                </p>
                
                <div className="space-y-4 mb-8">
                  {plan.features.map((feature, featureIndex) => (
                    <div key={featureIndex} className="flex items-center space-x-3">
                      <Check className="w-5 h-5 text-accent-500 flex-shrink-0" />
                      <span className="text-gray-700">{feature}</span>
                    </div>
                  ))}
                </div>
                
                {isAuthenticated ? (
                  <Button 
                    className="w-full" 
                    variant={plan.popular ? 'primary' : 'outline'}
                    size="lg"
                  >
                    {plan.cta}
                  </Button>
                ) : (
                  <Link to="/signup">
                    <Button 
                      className="w-full" 
                      variant={plan.popular ? 'primary' : 'outline'}
                      size="lg"
                    >
                      {plan.cta}
                    </Button>
                  </Link>
                )}
              </div>
            </Card>
          ))}
        </div>
      </div>
    </section>
  );
};

export default PricingCards;
