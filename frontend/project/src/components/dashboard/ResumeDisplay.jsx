import React, { useState, useEffect, useRef } from 'react';
import { Document, Page, pdfjs } from 'react-pdf';
import { renderAsync } from 'docx-preview';
import 'react-pdf/dist/esm/Page/AnnotationLayer.css';
import 'react-pdf/dist/esm/Page/TextLayer.css';

import Card from '../common/Card';
import Button from '../common/Button';
import { Download, ZoomIn, ZoomOut, ChevronLeft, ChevronRight, Loader2 } from 'lucide-react';
import { toast } from 'react-hot-toast';
import axiosInstance from '../../api/axiosConfig';

// PDF.js worker setup is crucial for react-pdf
pdfjs.GlobalWorkerOptions.workerSrc = `//cdnjs.cloudflare.com/ajax/libs/pdf.js/${pdfjs.version}/pdf.worker.min.js`;

const ResumeDisplay = ({ resume }) => {
  const [fileUrl, setFileUrl] = useState(null);
  const [fileType, setFileType] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const docxContainerRef = useRef(null);

  // PDF State
  const [numPages, setNumPages] = useState(null);
  const [pageNumber, setPageNumber] = useState(1);
  const [scale, setScale] = useState(1.2);

  useEffect(() => {
    // Function to fetch the file and prepare it for display
    const fetchAndSetFile = async () => {
      if (resume && resume.id) {
        setIsLoading(true);
        setFileUrl(null);
        setFileType('');
        setPageNumber(1);
        setNumPages(null);

        try {
          // Fetch the file as a blob from our new backend endpoint
          const response = await axiosInstance.get(`/resumes/${resume.id}/download`, {
            responseType: 'blob',
          });

          const blob = new Blob([response.data], { type: resume.mimeType });
          const url = URL.createObjectURL(blob);

          setFileUrl(url);
          setFileType(resume.mimeType);

          // If it's a word document, render it into the container
          if (resume.mimeType.includes('word') && docxContainerRef.current) {
            docxContainerRef.current.innerHTML = ''; // Clear previous preview
            await renderAsync(blob, docxContainerRef.current);
          }
        } catch (error) {
          console.error('Error fetching resume file:', error);
          toast.error('Could not load resume preview.');
        } finally {
          setIsLoading(false);
        }
      }
    };

    fetchAndSetFile();

    // Cleanup the object URL to avoid memory leaks
    return () => {
      if (fileUrl) {
        URL.revokeObjectURL(fileUrl);
      }
    };
  }, [resume]);

  const handleDownload = () => {
    if (!resume || !fileUrl) return;
    const link = document.createElement('a');
    link.href = fileUrl;
    link.setAttribute('download', resume.originalFilename || 'resume');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    toast.success('Download started!');
  };

  const onDocumentLoadSuccess = ({ numPages }) => {
    setNumPages(numPages);
  };

  const renderContent = () => {
    if (isLoading) {
      return (
        <div className="flex flex-col items-center justify-center h-full text-gray-500">
          <Loader2 className="w-8 h-8 animate-spin mb-2" />
          <p>Loading preview...</p>
        </div>
      );
    }

    if (!resume) {
      return (
        <div className="flex items-center justify-center h-full">
            <p className="text-gray-500">Select or upload a resume to see the preview.</p>
        </div>
      );
    }

    if (!fileUrl) return null; // Don't render anything if the URL isn't ready

    if (fileType === 'application/pdf') {
      return (
        <Document
          file={fileUrl}
          onLoadSuccess={onDocumentLoadSuccess}
          loading={<Loader2 className="w-8 h-8 animate-spin text-gray-500" />}
          error="Failed to load PDF file."
        >
          <Page pageNumber={pageNumber} scale={scale} renderTextLayer={false} />
        </Document>
      );
    }

    if (fileType.includes('word')) {
      return <div ref={docxContainerRef} className="docx-preview-container bg-white p-4" />;
    }

    return <p className="text-gray-500">Preview is not available for this file type.</p>;
  };

  const renderPdfControls = () => {
    if (fileType !== 'application/pdf' || !numPages) return null;

    return (
      <div className="flex items-center justify-center space-x-4 p-2 bg-gray-100 rounded-md mb-2">
        <Button variant="ghost" size="icon" onClick={() => setScale(s => s - 0.1)} disabled={scale <= 0.5}>
          <ZoomOut className="w-5 h-5" />
        </Button>
        <span className="text-sm font-medium">{(scale * 100).toFixed(0)}%</span>
        <Button variant="ghost" size="icon" onClick={() => setScale(s => s + 0.1)} disabled={scale >= 2.5}>
          <ZoomIn className="w-5 h-5" />
        </Button>
        <div className="w-px h-6 bg-gray-300" />
        <Button variant="ghost" size="icon" onClick={() => setPageNumber(p => Math.max(1, p - 1))} disabled={pageNumber <= 1}>
          <ChevronLeft className="w-5 h-5" />
        </Button>
        <span className="text-sm font-medium">
          Page {pageNumber} of {numPages}
        </span>
        <Button variant="ghost" size="icon" onClick={() => setPageNumber(p => Math.min(numPages, p + 1))} disabled={pageNumber >= numPages}>
          <ChevronRight className="w-5 h-5" />
        </Button>
      </div>
    );
  };

  return (
    <Card>
      <div className="p-6">
        <div className="flex justify-between items-center mb-4">
          <h3 className="text-lg font-semibold text-gray-900 truncate pr-4">
            {resume ? resume.originalFilename : 'Your Resume'}
          </h3>
          <Button onClick={handleDownload} disabled={!resume || isLoading} size="md">
            <Download className="w-4 h-4 mr-2" />
            Download
          </Button>
        </div>

        {renderPdfControls()}

        <div className="h-[70vh] overflow-auto p-2 border rounded-md bg-gray-100 flex justify-center">
          {renderContent()}
        </div>
      </div>
    </Card>
  );
};

export default ResumeDisplay;
