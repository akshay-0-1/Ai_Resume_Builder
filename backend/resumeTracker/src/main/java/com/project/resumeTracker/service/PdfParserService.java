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
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is null or empty");
        }

        try (InputStream inputStream = file.getInputStream();
             PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        }
    }
}
