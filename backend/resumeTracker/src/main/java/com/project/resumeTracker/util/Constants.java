package com.project.resumeTracker.util;

import java.util.Set;

public class Constants {
    // File Upload Settings
    public static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    // Parsing Statuses
    public static final String PARSING_STATUS_QUEUED = "QUEUED";
    public static final String PARSING_STATUS_COMPLETED = "COMPLETED";
    public static final String PARSING_STATUS_FAILED = "FAILED";
    public static final String PARSING_STATUS_PDF_GENERATED = "PDF_GENERATED";
    public static final String PARSING_STATUS_FAILED_UPLOAD = "FAILED_UPLOAD_OR_INITIAL_SAVE";

    // Error Messages
    public static final String ERROR_FILE_EMPTY = "File is empty";
    public static final String ERROR_FILE_TOO_LARGE = "File size exceeds maximum limit of 10MB";
    public static final String ERROR_INVALID_FILE_TYPE = "Invalid file type. Only PDF, DOC, and DOCX files are allowed";
    public static final String ERROR_RESUME_NOT_FOUND = "Resume not found.";
    public static final String ERROR_ACCESS_DENIED = "Access denied.";
    public static final String ERROR_PDF_GENERATION_FAILED = "Failed to regenerate PDF after update";

    // Date Formats
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    // Private constructor to prevent instantiation
    private Constants() {
        throw new AssertionError("Cannot instantiate utility class");
    }
}
