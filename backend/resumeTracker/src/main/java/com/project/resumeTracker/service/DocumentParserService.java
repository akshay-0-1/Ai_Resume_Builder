package com.project.resumeTracker.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface DocumentParserService {
    String extractText(MultipartFile file) throws IOException;
}
