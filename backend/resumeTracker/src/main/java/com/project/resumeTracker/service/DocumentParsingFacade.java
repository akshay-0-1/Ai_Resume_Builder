package com.project.resumeTracker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.tika.Tika; // Using Apache Tika for robust MIME type detection

import java.io.IOException;

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

        if ("application/pdf".equalsIgnoreCase(mimeType)) {
            return pdfParserService.extractText(file);
        } else if ("application/msword".equalsIgnoreCase(mimeType) || 
                   "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equalsIgnoreCase(mimeType)) {
            return wordParserService.extractText(file);
        } else {
            // Check filename as a fallback or for more specific error
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null) {
                if (originalFilename.toLowerCase().endsWith(".pdf")) {
                    return pdfParserService.extractText(file);
                }
                if (originalFilename.toLowerCase().endsWith(".doc") || originalFilename.toLowerCase().endsWith(".docx")) {
                    return wordParserService.extractText(file);
                }
            }
            throw new IllegalArgumentException("Unsupported file type: " + mimeType + ". Please upload a PDF, DOC, or DOCX file.");
        }
    }
}
