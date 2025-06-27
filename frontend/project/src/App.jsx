import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Layout from './components/layout/Layout';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/layout/ProtectedRoute';
import LandingPage from './pages/LandingPage';
import { resumeService } from './api/resumeService';
import ResumeUploader from './components/dashboard/ResumeUploader';
import JobDescriptionInput from './components/dashboard/JobDescriptionInput';
import AnalysisDisplay from './components/dashboard/AnalysisDisplay';
import Spinner from './components/common/Spinner';

// Lazy load page components
const LoginPage = React.lazy(() => import('./pages/LoginPage'));
const SignupPage = React.lazy(() => import('./pages/SignupPage'));
const DashboardPage = React.lazy(() => import('./pages/DashboardPage'));
const AboutPage = React.lazy(() => import('./pages/AboutPage'));
const PricingPage = React.lazy(() => import('./pages/PricingPage'));
const History = React.lazy(() => import('./pages/History'));
const EditResumePage = React.lazy(() => import('./pages/EditResumePage'));
const ViewResumePage = React.lazy(() => import('./pages/ViewResumePage'));

function App() {
  const [state, setState] = useState({
    resumeId: null,
    jobDescription: '',
    result: null,
    loading: false,
    error: null
  });

  const { resumeId, jobDescription, result, loading, error } = state;

  const handleAnalyze = async () => {
    if (!resumeId || !jobDescription) {
      setState(prev => ({ ...prev, error: 'Please select a resume and provide a job description.' }));
      return;
    }

    setState(prev => ({ ...prev, loading: true, error: null, result: null }));

    try {
      const response = await resumeService.analyzeResume(resumeId, jobDescription);
      if (response.success) {
        setState(prev => ({ ...prev, result: response.data.data }));
      } else {
        setState(prev => ({ ...prev, error: response.error || 'An unexpected error occurred.' }));
      }
    } catch (err) {
      setState(prev => ({ ...prev, error: err.message || 'An unexpected error occurred.' }));
    } finally {
      setState(prev => ({ ...prev, loading: false }));
    }
  };

  return (
    <AuthProvider>
      <Router>
        <Layout>
          <Suspense fallback={<Spinner />}> {/* Simplified fallback */}
            <Routes>
              {/* Public routes */}
              <Route path="/" element={<LandingPage />} />
              <Route path="/about" element={<AboutPage />} />
              <Route path="/pricing" element={<PricingPage />} />
              <Route path="/login" element={<LoginPage />} />
              <Route path="/signup" element={<SignupPage />} />

              {/* Protected routes */}
              <Route element={<ProtectedRoute />}>
                <Route path="/dashboard" element={<DashboardPage />} />
                <Route path="/history" element={<History />} />
                <Route path="/resumes/:id" element={
                  <Suspense fallback={<Spinner />}>
                    <ViewResumePage />
                  </Suspense>
                } />
                <Route path="/edit-resume/:id" element={<EditResumePage />} />
                <Route path="/analyze" element={
                  <div className="container mx-auto p-4">
                    <header className="text-center mb-8">
                      <h1 className="text-4xl font-bold text-gray-800">AI Resume Analyzer</h1>
                      <p className="text-lg text-gray-600">Upload your resume and paste a job description to see your match score!</p>
                    </header>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                      <div>
                        <h2 className="text-2xl font-semibold mb-4">Your Resume</h2>
                        <ResumeUploader onUploadSuccess={setResumeId} />
                      </div>
                      <div>
                        <h2 className="text-2xl font-semibold mb-4">Job Description</h2>
                        <JobDescriptionInput value={jobDescription} onChange={setJobDescription} />
                      </div>
                    </div>

                    <div className="mt-8 text-center">
                      <button 
                        onClick={handleAnalyze}
                        disabled={loading || !resumeId || !jobDescription}
                        className="bg-blue-600 text-white font-bold py-3 px-8 rounded-lg hover:bg-blue-700 transition duration-300 disabled:bg-gray-400"
                      >
                        {loading ? 'Analyzing...' : 'Analyze Now'}
                      </button>
                    </div>

                    {loading && <div className="flex justify-center mt-8"><Spinner /></div>}
                    {error && <div className="mt-8 text-center text-red-500 font-semibold">Error: {error}</div>}
                    {result && <AnalysisDisplay result={result} />}
                  </div>
                } />
              </Route>
            </Routes>
          </Suspense>
        </Layout>
      </Router>
    </AuthProvider>
  );
}

export default App;
