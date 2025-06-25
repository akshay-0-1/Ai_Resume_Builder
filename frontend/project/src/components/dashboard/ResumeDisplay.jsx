import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Document, Page, pdfjs } from 'react-pdf';
import { renderAsync } from 'docx-preview';
import ReactQuill from 'react-quill';
import 'react-quill/dist/quill.snow.css';
import 'react-pdf/dist/esm/Page/AnnotationLayer.css';
import 'react-pdf/dist/esm/Page/TextLayer.css';

import Card from '../common/Card';
import Button from '../common/Button';
import { Download, ZoomIn, ZoomOut, ChevronLeft, ChevronRight, Loader2, Edit, Save, XCircle } from 'lucide-react';
import { toast } from 'react-hot-toast';
import axiosInstance from '../../api/axiosConfig';
import { resumeService } from '../../api/resumeService';

// PDF.js worker setup
pdfjs.GlobalWorkerOptions.workerSrc = `//cdnjs.cloudflare.com/ajax/libs/pdf.js/${pdfjs.version}/pdf.worker.min.js`;

const ResumeDisplay = ({ resume }) => {
  const [fileUrl, setFileUrl] = useState(null);
  const [fileType, setFileType] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [editorContent, setEditorContent] = useState('');
  const docxContainerRef = useRef(null);
  const quillRef = useRef(null);

  // PDF State
  const [numPages, setNumPages] = useState(null);
  const [pageNumber, setPageNumber] = useState(1);
  const [scale, setScale] = useState(1.2);

  const fetchAndSetFile = useCallback(async () => {
    if (!resume || !resume.id) return;

    const checkStatusAndMaybeDownload = async () => {
      try {
        const statusResp = await axiosInstance.get(`/resumes/${resume.id}/status`, { validateStatus: () => true });
        const statusCode = statusResp.status;

        if (statusCode === 202 || statusCode === 404) {
          // still processing – try again after retry-after if provided
          const retryAfter = parseInt(statusResp.headers['retry-after'] || '3', 10) * 1000;
          setTimeout(checkStatusAndMaybeDownload, retryAfter);
          return;
        }

        if (statusCode === 409) {
          const msg = statusResp.data?.message || 'PDF generation failed.';
          toast.error(msg);
          setIsLoading(false);
          return;
        }
        // 200 OK – ready to download
        await downloadPdf();
      } catch (err) {
        console.error('Status check failed', err);
        toast.error('Could not check resume status.');
        setIsLoading(false);
      }
    };

    const downloadPdf = async () => {
      let stillProcessing = false; // flag to keep loader active while 202 responses arrive
      try {
        const response = await axiosInstance.get(`/resumes/${resume.id}/download`, {
          responseType: 'blob',
          timeout: 60000,
        });

        // If backend still processing it may return 202 even though axios treats it as success
        if (response.status === 202) {
          stillProcessing = true;
          const retryAfter = parseInt(response.headers['retry-after'] || '3', 10) * 1000;
          setTimeout(checkStatusAndMaybeDownload, retryAfter);
          return;
        }

        const mime = response.headers['content-type'] || resume.mimeType || 'application/pdf';
        const blob = new Blob([response.data], { type: mime });
        const url = URL.createObjectURL(blob);

        setFileUrl(url);
        setFileType(mime);

        if (mime.includes('word') && docxContainerRef.current) {
          docxContainerRef.current.innerHTML = '';
          await renderAsync(blob, docxContainerRef.current);
        }
      } catch (error) {
        if (error.response) {
          const status = error.response.status;
          if (status === 202) {
                stillProcessing = true;
            // PDF still processing; restart polling
            const retryAfter = parseInt(error.response.headers['retry-after'] || '3', 10) * 1000;
            setTimeout(checkStatusAndMaybeDownload, retryAfter);
            return;
          }
          if (status === 409) {
            const msg = error.response.data?.message || 'PDF generation failed.';
            toast.error(msg);
            setIsLoading(false);
            return;
          }
        }
        console.error('Error downloading resume file:', error);
        toast.error('Could not load resume preview.');
      } finally {
        if (!stillProcessing) {
          setIsLoading(false);
        }
      }
    };

    // start flow
    setIsLoading(true);
    setFileUrl(null);
    setFileType('');
    setPageNumber(1);
    setNumPages(null);

    // First attempt to download (this will trigger compilation if PDF not ready)
    await downloadPdf();
  }, [resume]);

  useEffect(() => {
    if (!isEditing) {
      fetchAndSetFile();
    }

    return () => {
      if (fileUrl) {
        URL.revokeObjectURL(fileUrl);
      }
    };
  }, [resume, isEditing, fetchAndSetFile]);

  const handleEditClick = async () => {
    if (!resume || !resume.id) return;
    setIsLoading(true);
    try {
      // Fetch the full resume data, including the rich htmlContent
      const response = await axiosInstance.get(`/resumes/${resume.id}`);
      const fullResume = response.data.data;

      // Use htmlContent if available, otherwise fallback to the plain text conversion
      const contentToEdit = fullResume.htmlContent || fullResume.resumeContent.split('\n').map(p => `<p>${p}</p>`).join('');

      setEditorContent(contentToEdit);
      setIsEditing(true);
    } catch (error) {
      console.error('Error fetching resume for editing:', error);
      toast.error('Could not load resume content for editing.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleCancelClick = () => {
    setIsEditing(false);
    setEditorContent('');
  };

  const handleSaveClick = async () => {
    if (!editorContent.trim()) {
      toast.error('Resume content cannot be empty.');
      return;
    }
    setIsSaving(true);
    try {
      // The backend controller expects a JSON object with the key 'htmlContent'.
      // axiosInstance will automatically set the Content-Type to application/json.
      const response = await axiosInstance.put(`/resumes/${resume.id}/content`, {
        htmlContent: editorContent
      });

      if (response.data && response.data.success) {
        toast.success('Resume updated successfully!');
        setIsEditing(false); // This triggers the useEffect to refetch the preview
      } else {
        throw new Error(response.data.error || 'An unknown error occurred during save.');
      }
    } catch (error) {
      console.error('--- Save Operation Failed: Full Error Object ---', error);
      const errorMessage = error.response?.data?.error || error.response?.data?.message || 'Network error or server is down.';
      toast.error(`Save failed: ${errorMessage}`);
    } finally {
      setIsSaving(false);
    }
  };

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

    if (isEditing) {
      return (
        <div className="bg-white h-full">
          <ReactQuill
            ref={quillRef}
            theme="snow"
            value={editorContent}
            onChange={setEditorContent}
            className="h-[calc(100%-42px)]"
          />
        </div>
      );
    }

    if (!fileUrl) return null;

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
    if (isEditing || fileType !== 'application/pdf' || !numPages) return null;

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

  const renderHeaderButtons = () => {
    if (isEditing) {
      return (
        <div className="flex items-center space-x-2">
          <Button onClick={handleCancelClick} variant="secondary" size="md">
            <XCircle className="w-4 h-4 mr-2" />
            Cancel
          </Button>
          <Button onClick={handleSaveClick} isLoading={isSaving} size="md">
            <Save className="w-4 h-4 mr-2" />
            Save
          </Button>
        </div>
      );
    }

    return (
      <div className="flex items-center space-x-2">
        <Button onClick={handleEditClick} disabled={!resume || isLoading} variant="outline" size="md">
          <Edit className="w-4 h-4 mr-2" />
          Edit
        </Button>
        <Button onClick={handleDownload} disabled={!resume || isLoading} size="md">
          <Download className="w-4 h-4 mr-2" />
          Download
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
          {renderHeaderButtons()}
        </div>

        {renderPdfControls()}

        <div className={`h-[70vh] overflow-auto p-2 border rounded-md ${isEditing ? 'bg-white' : 'bg-gray-100'} flex justify-center`}>
          {renderContent()}
        </div>
      </div>
    </Card>
  );
};

export default ResumeDisplay;
