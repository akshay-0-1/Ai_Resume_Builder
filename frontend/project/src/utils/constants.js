export const allowedFileTypes = [
  'application/pdf',
  'application/msword',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
];

export const maxSizeMB = 5;

export const dateOptions = {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
  second: '2-digit',
  hour12: true
};

export const errorMessages = {
  fileTooLarge: `File size must be less than ${maxSizeMB}MB`,
  invalidFileType: 'Please upload a PDF, DOC, or DOCX file',
  emptyContent: 'Resume content cannot be empty',
  saveFailed: 'Failed to save resume',
  loadFailed: 'Could not load resume preview',
  unsupportedType: 'Unsupported file type'
};

export const toastConfig = {
  success: {
    position: 'top-right',
    autoClose: 3000,
    hideProgressBar: false,
    closeOnClick: true,
    pauseOnHover: true,
    draggable: true,
    progress: undefined,
    theme: 'light'
  },
  error: {
    position: 'top-right',
    autoClose: 5000,
    hideProgressBar: false,
    closeOnClick: true,
    pauseOnHover: true,
    draggable: true,
    progress: undefined,
    theme: 'colored'
  }
};
