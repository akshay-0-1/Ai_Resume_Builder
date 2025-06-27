import { allowedFileTypes, maxSizeMB, dateOptions } from './constants';

export const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes';
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(1024));
  return parseFloat((bytes / Math.pow(1024, i)).toFixed(2)) + ' ' + sizes[i];
};

export const validateFile = (file) => {
  if (!file) return false;
  
  // Validate file type
  if (!allowedFileTypes.includes(file.type)) {
    return { valid: false, message: 'invalidFileType' };
  }

  // Validate file size
  const maxSize = maxSizeMB * 1024 * 1024;
  if (file.size > maxSize) {
    return { valid: false, message: 'fileTooLarge' };
  }

  return { valid: true };
};

export const formatDate = (dateArray) => {
  if (!dateArray) return '';
  return new Date(...dateArray).toLocaleString('en-US', dateOptions);
};

export const getFileIcon = (type) => {
  const iconProps = { className: 'w-5 h-5' };
  return type === 'application/pdf' ? (
    <FileText {...iconProps} className="text-red-500" />
  ) : (
    <FileText {...iconProps} className="text-blue-500" />
  );
};

export const createToast = (type, message) => {
  const config = type === 'success' ? toastConfig.success : toastConfig.error;
  return toast[type](message, config);
};
