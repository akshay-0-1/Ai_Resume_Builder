import React from 'react';
import { Copy, CheckCircle } from 'lucide-react';
import Card from '../common/Card';
import { toast } from 'react-toastify';

const TargetedChangeCard = ({ section, suggestion }) => {
  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(suggestion);
      toast.success('Suggestion copied to clipboard!');
    } catch (error) {
      toast.error('Failed to copy to clipboard');
    }
  };

  const getSectionIcon = (section) => {
    const iconMap = {
      'Skills': '🔧',
      'Experience': '💼',
      'Projects': '🚀',
      'Education': '🎓',
      'Summary': '📝',
      'Project Experience': '🚀',
      'Technical Skills': '💻',
      'Work Experience': '💼'
    };
    
    return iconMap[section] || '📋';
  };

  const getSectionColor = (section) => {
    const colorMap = {
      'Skills': 'bg-blue-50 text-blue-700 border-blue-200',
      'Experience': 'bg-green-50 text-green-700 border-green-200',
      'Projects': 'bg-purple-50 text-purple-700 border-purple-200',
      'Education': 'bg-amber-50 text-amber-700 border-amber-200',
      'Summary': 'bg-pink-50 text-pink-700 border-pink-200',
      'Project Experience': 'bg-purple-50 text-purple-700 border-purple-200',
      'Technical Skills': 'bg-blue-50 text-blue-700 border-blue-200',
      'Work Experience': 'bg-green-50 text-green-700 border-green-200'
    };
    
    return colorMap[section] || 'bg-gray-50 text-gray-700 border-gray-200';
  };

  return (
    <Card className="p-4 hover:shadow-md transition-shadow">
      <div className="flex items-start justify-between space-x-3">
        <div className="flex-1">
          <div className="flex items-center space-x-2 mb-3">
            <span className="text-lg">{getSectionIcon(section)}</span>
            <span className={`px-3 py-1 rounded-full text-sm font-medium border ${getSectionColor(section)}`}>
              {section}
            </span>
          </div>
          
          <p className="text-gray-700 leading-relaxed">
            {suggestion}
          </p>
        </div>
        
        <button
          onClick={handleCopy}
          className="flex-shrink-0 p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-lg transition-colors"
          title="Copy suggestion"
        >
          <Copy className="w-4 h-4" />
        </button>
      </div>
    </Card>
  );
};

export default TargetedChangeCard;