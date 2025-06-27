import React from 'react';

import Hero from '../components/pricing/Hero';
import PricingCards from '../components/pricing/PricingCards';
import Features from '../components/pricing/Features';
import Faq from '../components/pricing/Faq';
import CTA from '../components/pricing/CTA';

const PricingPage = () => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 via-white to-blue-50">
      
      <Hero />
      <PricingCards />
      <Features />
      <Faq />
      <CTA />
    </div>
  );
};

export default PricingPage;