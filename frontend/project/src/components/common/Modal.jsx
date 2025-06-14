import React from 'react';
import { X } from 'lucide-react';
import Card from './Card';

const Modal = ({ isOpen, onClose, title, children }) => {
  if (!isOpen) return null;

  return (
    <div
      className="fixed inset-0 bg-black bg-opacity-50 backdrop-blur-sm flex justify-center items-center z-50"
      onClick={onClose}
    >
      <div
        className="relative w-full max-w-lg"
        onClick={(e) => e.stopPropagation()}
      >
        <Card className="p-6">
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-xl font-display font-semibold text-gray-900">{title}</h3>
            <button
              onClick={onClose}
              className="p-2 rounded-full text-gray-500 hover:bg-gray-100 hover:text-gray-800 transition-colors"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
          {children}
        </Card>
      </div>
    </div>
  );
};

export default Modal;
