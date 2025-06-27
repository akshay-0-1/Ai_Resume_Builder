import React from 'react';

import Hero from '../components/landing/Hero';
import Stats from '../components/landing/Stats';
import Features from '../components/landing/Features';
import SuccessStories from '../components/landing/SuccessStories';
import CTA from '../components/landing/CTA';
import Footer from '../components/landing/Footer';

const LandingPage = () => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 via-white to-blue-50 w-full">
      <Hero />
      <Stats />
      <Features />
      <SuccessStories />
      <CTA />
      <Footer />
    </div>
  );
};

export default LandingPage;