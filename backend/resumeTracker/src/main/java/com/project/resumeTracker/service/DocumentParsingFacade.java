package com.project.resumeTracker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.tika.Tika; // Using Apache Tika for robust MIME type detection

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class DocumentParsingFacade {

    private final DocumentParserService pdfParserService;
    private final DocumentParserService wordParserService;
    private final Tika tika = new Tika(); // For reliable MIME type detection

    public String parseDocument(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty.");
        }

        String mimeType;
        try {
            mimeType = tika.detect(file.getInputStream());
        } catch (IOException e) {
            // Fallback to original filename extension if Tika fails
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null && originalFilename.toLowerCase().endsWith(".pdf")) {
                mimeType = "application/pdf";
            } else if (originalFilename != null && (originalFilename.toLowerCase().endsWith(".doc") || originalFilename.toLowerCase().endsWith(".docx"))) {
                // Approximate for Word, WordParserService will handle specific .doc vs .docx
                mimeType = "application/msword"; 
            } else {
                throw new IllegalArgumentException("Could not determine file type or unsupported file type.");
            }
        }

        return parseDocument(file.getInputStream(), mimeType);
    }

    public String parseDocument(InputStream inputStream, String mimeType) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream cannot be null.");
        }
        if (mimeType == null || mimeType.isBlank()) {
            throw new IllegalArgumentException("MIME type cannot be null or blank.");
        }

        if ("application/pdf".equalsIgnoreCase(mimeType)) {
            return pdfParserService.extractText(inputStream);
        } else if ("application/msword".equalsIgnoreCase(mimeType) ||
                   "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equalsIgnoreCase(mimeType)) {
            return wordParserService.extractText(inputStream);
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + mimeType + ". Please upload a PDF, DOC, or DOCX file.");
        }
    }
}
