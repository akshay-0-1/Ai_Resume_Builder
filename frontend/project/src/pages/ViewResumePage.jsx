import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { resumeService } from '../api/resumeService';
import Spinner from '../components/common/Spinner';
import ResumeDisplay from '../components/dashboard/ResumeDisplay';

function ViewResumePage() {
  const { id } = useParams();
  const [resume, setResume] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchResume = async () => {
      try {
        const response = await resumeService.getResume(id);
        if (response.success) {
          setResume(response.data);
        } else {
          setError(response.error || 'Failed to fetch resume');
        }
      } catch (err) {
        setError(err.message || 'An unexpected error occurred');
      } finally {
        setLoading(false);
      }
    };

    fetchResume();
  }, [id]);

  if (loading) {
    return <div className="flex justify-center items-center h-screen"><Spinner /></div>;
  }

  if (error) {
    return <div className="text-center text-red-500 font-semibold">Error: {error}</div>;
  }

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-3xl font-bold mb-4">Resume Preview</h1>
      {resume && <ResumeDisplay resume={resume} />}
    </div>
  );
}

export default ViewResumePage;
