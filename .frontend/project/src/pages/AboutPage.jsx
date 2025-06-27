import React from 'react';
import Navbar from '../components/layout/Navbar';
import Hero from '../components/about/Hero';
import Mission from '../components/about/Mission';
import Values from '../components/about/Values';
import Team from '../components/about/Team';
import Timeline from '../components/about/Timeline';
import CTA from '../components/about/CTA';

const AboutPage = () => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 via-white to-blue-50">
      <Navbar />
      <Hero />
      <Mission />
      <Values />
      <Team />
      <Timeline />
      <CTA />
    </div>
  );
};

export default AboutPage;