import React from 'react';
import Navbar from './Navbar';
import { ThemeProvider } from '../../context/ThemeContext';
import ToastProvider from '../common/ToastProvider';

const Layout = ({ children }) => {
  return (
    <ThemeProvider>
      <ToastProvider>
        <div className="min-h-screen flex flex-col bg-white dark:bg-secondary-900 text-gray-800 dark:text-gray-100 transition-colors duration-300">
          <Navbar />
          <main className="flex-1 w-full">
            {children}
          </main>
        </div>
      </ToastProvider>
    </ThemeProvider>
  );
};

export default Layout;
