import React from 'react';

// Simple skeleton placeholder. Pass custom Tailwind classes via `className`.
const Skeleton = ({ className = '' }) => (
  <div className={`bg-gray-200 dark:bg-gray-700 rounded-md animate-pulse ${className}`} />
);

export default Skeleton;
