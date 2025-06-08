package com.project.resumeTracker.service;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
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

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("File name is null");
        }

        try (InputStream inputStream = file.getInputStream()) {
            if (fileName.toLowerCase().endsWith(".docx")) {
                try (XWPFDocument document = new XWPFDocument(inputStream);
                     XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                    return extractor.getText();
                }
            } else if (fileName.toLowerCase().endsWith(".doc")) {
                try (HWPFDocument document = new HWPFDocument(inputStream);
                     WordExtractor extractor = new WordExtractor(document)) {
                    return extractor.getText();
                }
            } else {
                throw new IllegalArgumentException("Unsupported Word document format: " + fileName);
            }
        }
    }
}
