import React from 'react';
import { ToastContainer } from 'react-toastify';

// Override default toast styles with blue colors
const toastStyles = {
  success: {
    backgroundColor: '#3b82f6',
    color: '#ffffff',
  },
  error: {
    backgroundColor: '#dc2626',
    color: '#ffffff',
  },
  info: {
    backgroundColor: '#3b82f6',
    color: '#ffffff',
  },
  warning: {
    backgroundColor: '#f59e0b',
    color: '#ffffff',
  },
};

export const ToastProvider = ({ children }) => (
  <>
    {children}
    <ToastContainer
      position="top-right"
      autoClose={4000}
      newestOnTop
      closeOnClick
      pauseOnHover
      draggable
      className="rounded-lg p-3"
      toastClassName={(toast) => `rounded-lg p-3 ${toastStyles[toast.type].backgroundColor} text-white`}
    />
  </>
);

export default ToastProvider;
