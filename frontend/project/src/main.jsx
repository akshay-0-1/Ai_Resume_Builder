import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import App from './App.jsx';
import './index.css';
import { AnalysisProvider } from './context/AnalysisContext.jsx';

const rootElement = document.getElementById('root');

if (rootElement) {
  createRoot(rootElement).render(
    <StrictMode>
      <AnalysisProvider>
        <App />
      </AnalysisProvider>
    </StrictMode>
  );
} else {
  console.error('Failed to find the root element');
}
