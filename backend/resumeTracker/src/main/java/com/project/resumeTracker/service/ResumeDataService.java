package com.project.resumeTracker.service;

import com.project.resumeTracker.entity.*;
import com.project.resumeTracker.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeDataService {

    private final DocumentParsingFacade documentParsingFacade;
    private final InformationExtractorService informationExtractorService;
    private final ResumeRepository resumeRepository;

    @Transactional
    public Resume parseAndEnrichResume(MultipartFile multipartFile, Resume resumeToEnrich) throws IOException {
        try {
            // 1. Extract raw text from the document
            String rawText = documentParsingFacade.parseDocument(multipartFile);

            // 2. Extract structured information
            PersonalDetails personalDetails = informationExtractorService.extractPersonalDetails(rawText);
            List<WorkExperience> workExperiences = informationExtractorService.extractWorkExperience(rawText);
            List<Education> educations = informationExtractorService.extractEducation(rawText);
            List<Skill> skills = informationExtractorService.extractSkills(rawText);

            // 3. Populate the existing Resume entity
            // Clear existing collections to avoid duplicates if re-parsing
            resumeToEnrich.getWorkExperiences().clear();
            resumeToEnrich.getEducations().clear();
            resumeToEnrich.getSkills().clear();

            if (personalDetails != null) {
                // If PersonalDetails is a new entity each time, or if it can be updated.
                // Assuming a new PersonalDetails entity is created by extractor and needs to be set.
                // If it could be null and we want to preserve an old one, logic would be different.
                resumeToEnrich.setPersonalDetails(personalDetails);
            } // else, if personalDetails is null, any existing one remains or becomes null if not handled.

            workExperiences.forEach(resumeToEnrich::addWorkExperience);
            educations.forEach(resumeToEnrich::addEducation);
            skills.forEach(resumeToEnrich::addSkill);

            resumeToEnrich.setParsingStatus("COMPLETED");

        } catch (Exception e) {
            // Log error appropriately
            // Consider setting parsingStatus to "FAILED" or "PARTIAL_FAILURE"
            resumeToEnrich.setParsingStatus("FAILED");
            // Optionally re-throw or handle more gracefully
            throw new IOException("Failed to parse and enrich resume: " + e.getMessage(), e);
        }

        // 4. Save the enriched Resume entity (cascades to related entities)
        return resumeRepository.save(resumeToEnrich);
    }
}

