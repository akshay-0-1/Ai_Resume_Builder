package com.project.resumeTracker.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service("pdfParserService")
public class PdfParserService implements DocumentParserService {

    @Override
    public String extractText(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Failed to parse empty file.");
        }
        try (InputStream inputStream = file.getInputStream()) {
            return extractText(inputStream);
        }
    }

    @Override
    public String extractText(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        }
    }
}
