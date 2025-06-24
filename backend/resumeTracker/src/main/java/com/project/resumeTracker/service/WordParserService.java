package com.project.resumeTracker.service;

import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.extractor.POITextExtractor;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service("wordParserService")
public class WordParserService implements DocumentParserService {

    @Override
    public String extractText(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is null or empty");
        }
        try (InputStream inputStream = file.getInputStream()) {
            return extractText(inputStream);
        }
    }

    @Override
    public String extractText(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream is null");
        }
        try (POITextExtractor extractor = ExtractorFactory.createExtractor(inputStream)) {
            return extractor.getText();
        } catch (Exception e) {
            throw new IOException("Failed to extract text from Word document stream", e);
        }
    }
}
