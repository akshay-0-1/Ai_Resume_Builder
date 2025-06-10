package com.project.resumeTracker.service;

import com.project.resumeTracker.dto.ResumeDataDTO;
import com.project.resumeTracker.entity.*;
import com.project.resumeTracker.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ResumeDataService {

    private final InformationExtractorService informationExtractorService;
    private final ResumeRepository resumeRepository;

    @Transactional
    public Resume parseAndEnrichResume(String rawText, Resume resumeToEnrich) throws IOException {
        try {
            // 1. Extract structured information using the new service
            ResumeDataDTO extractedData = informationExtractorService.extractInformationFromText(rawText);

            // 2. Populate the existing Resume entity from the DTO
            if (extractedData != null) {
                resumeToEnrich.setPersonalDetails(extractedData.getPersonalDetails());

                // Clear and repopulate collections
                resumeToEnrich.getWorkExperiences().clear();
                if (extractedData.getWorkExperiences() != null) {
                    extractedData.getWorkExperiences().forEach(resumeToEnrich::addWorkExperience);
                }

                resumeToEnrich.getEducations().clear();
                if (extractedData.getEducations() != null) {
                    extractedData.getEducations().forEach(resumeToEnrich::addEducation);
                }

                resumeToEnrich.getSkills().clear();
                if (extractedData.getSkills() != null) {
                    extractedData.getSkills().forEach(resumeToEnrich::addSkill);
                }
            }

            resumeToEnrich.setParsingStatus("COMPLETED");

        } catch (Exception e) {
            resumeToEnrich.setParsingStatus("FAILED");
            throw new IOException("Failed to parse and enrich resume: " + e.getMessage(), e);
        }

        // 3. Save the enriched Resume entity
        return resumeRepository.save(resumeToEnrich);
    }
}
