import React, { useState, lazy, Suspense } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/layout/ProtectedRoute';
import LandingPage from './pages/LandingPage';
// Lazy load page components for better performance
const LoginPage = lazy(() => import('./pages/LoginPage'));
const SignupPage = lazy(() => import('./pages/SignupPage'));
const DashboardPage = lazy(() => import('./pages/DashboardPage'));
const AboutPage = lazy(() => import('./pages/AboutPage'));
const PricingPage = lazy(() => import('./pages/PricingPage'));
const History = lazy(() => import('./pages/History'));
import { resumeService } from './api/resumeService';
import ResumeUploader from './components/dashboard/ResumeUploader';
import JobDescriptionInput from './components/dashboard/JobDescriptionInput';
import AnalysisDisplay from './components/dashboard/AnalysisDisplay';
import Spinner from './components/common/Spinner';

function App() {
  const [resumeId, setResumeId] = useState(null);
  const [jobDescription, setJobDescription] = useState('');
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleAnalyze = async () => {
    if (!resumeId || !jobDescription) {
      setError('Please select a resume and provide a job description.');
      return;
    }

    setLoading(true);
    setError(null);
    setResult(null);

    try {
      const response = await resumeService.analyzeResume(resumeId, jobDescription);
      if (response.success) {
        setResult(response.data.data); // The actual analysis is in response.data.data
      } else {
        setError(response.error || 'An unexpected error occurred.');
      }
    } catch (err) {
      setError(err.message || 'An unexpected error occurred.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthProvider>
        <Router future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
          <div className="App">
            <Suspense fallback={<div className="w-full h-screen flex justify-center items-center"><Spinner /></div>}>
              <Routes>
                <Route path="/" element={<LandingPage />} />
                <Route path="/about" element={<AboutPage />} />
                <Route path="/pricing" element={<PricingPage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/signup" element={<SignupPage />} />
                <Route 
                  path="/dashboard" 
                  element={
                    <ProtectedRoute>
                      <DashboardPage />
                    </ProtectedRoute>
                  } 
                />
                <Route 
                  path="/history" 
                  element={
                    <ProtectedRoute>
                      <History />
                    </ProtectedRoute>
                  } 
                />
                <Route 
                  path="/analyze" 
                  element={
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
                  } 
                />
              </Routes>
            </Suspense>
            
            <Toaster
              position="top-right"
              toastOptions={{
                duration: 4000,
                style: {
                  background: '#1f2937',
                  color: '#f9fafb',
                  borderRadius: '12px',
                  padding: '16px',
                  fontSize: '14px',
                  fontWeight: '500',
                },
                success: {
                  duration: 3000,
                  iconTheme: {
                    primary: '#10b981',
                    secondary: '#f9fafb',
                  },
                },
                error: {
                  duration: 4000,
                  iconTheme: {
                    primary: '#ef4444',
                    secondary: '#f9fafb',
                  },
                },
              }}
            />
          </div>
        </Router>
    </AuthProvider>
  );
}

export default App;
