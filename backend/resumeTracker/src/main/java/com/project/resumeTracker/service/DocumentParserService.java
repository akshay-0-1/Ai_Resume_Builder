package com.project.resumeTracker.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;

public interface DocumentParserService {
    String extractText(MultipartFile file) throws IOException;
    String extractText(InputStream inputStream) throws IOException;
}
