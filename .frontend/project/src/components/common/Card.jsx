import React from 'react';

const Card = ({ children, className = '', hover = false, gradient = false, ...props }) => {
  const baseClasses = 'bg-white rounded-2xl shadow-sm border border-gray-200/50';
  const hoverClasses = hover ? 'card-hover cursor-pointer' : '';
  const gradientClasses = gradient ? 'bg-gradient-to-br from-white to-gray-50/50' : '';
  
  return (
    <div 
      className={`${baseClasses} ${hoverClasses} ${gradientClasses} ${className}`}
      {...props}
    >
      {children}
    </div>
  );
};

export default Card;