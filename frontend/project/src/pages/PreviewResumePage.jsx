import React from 'react';
import { useParams } from 'react-router-dom';
import { useAnalysis } from '../context/AnalysisContext';
import ResumeDisplay from '../components/dashboard/ResumeDisplay';
import { Card } from '../components/common/Card';
import { Download } from 'lucide-react';
import Button from '../components/common/Button';

const PreviewResumePage = () => {
  const { id } = useParams();
  const { resumes } = useAnalysis();
  const resume = resumes.find(r => r.id === id);

  if (!resume) {
    return (
      <Card className="p-6">
        <p className="text-gray-500">Resume not found</p>
      </Card>
    );
  }

  return (
    <div className="max-w-7xl mx-auto p-6">
      <Card className="p-6">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-2xl font-semibold">Preview - {resume.originalFilename}</h1>
          <Button
            variant="outline"
            onClick={() => window.open(`/edit-resume/${id}`, '_blank')}
          >
            <Edit className="w-4 h-4 mr-2" />
            Edit Resume
          </Button>
        </div>
        <ResumeDisplay resume={resume} />
      </Card>
    </div>
  );
};

export default PreviewResumePage;
