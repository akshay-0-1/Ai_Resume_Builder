import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { ArrowRight } from 'lucide-react';
import Button from '../common/Button';

const CTA = () => {
  const { isAuthenticated } = useAuth();

  return (
    <section className="py-20 bg-gradient-to-r from-primary-600 to-accent-600">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
        <h2 className="text-4xl font-display font-bold text-white mb-6">
          Ready to Accelerate Your Career?
        </h2>
        <p className="text-xl text-primary-100 mb-8 leading-relaxed">
          Join thousands of professionals who have already transformed their job search with ResumeAI.
        </p>
        {!isAuthenticated && (
          <Link to="/signup">
            <Button size="xl" variant="secondary" className="group">
              Start Your Free Trial
              <ArrowRight className="w-5 h-5 ml-2 group-hover:translate-x-1 transition-transform" />
            </Button>
          </Link>
        )}
      </div>
    </section>
  );
};

export default CTA;