import React from 'react';

import Hero from '../components/about/Hero';
import Mission from '../components/about/Mission';
import Values from '../components/about/Values';
import TechStack from '../components/about/TechStack';
import CTA from '../components/about/CTA';

const AboutPage = () => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 via-white to-blue-50">

      <Hero />
      <Mission />
      <Values />
      <TechStack />
      <CTA />
    </div>
  );
};

export default AboutPage;