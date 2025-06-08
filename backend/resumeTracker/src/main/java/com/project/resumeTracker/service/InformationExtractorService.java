package com.project.resumeTracker.service;

import com.project.resumeTracker.entity.Education;
import com.project.resumeTracker.entity.PersonalDetails;
import com.project.resumeTracker.entity.Skill;
import com.project.resumeTracker.entity.WorkExperience;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InformationExtractorService {

    // Regex patterns (these are basic examples and will need significant refinement)
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\+?\\d{1,3}[- ]?)?((\\(\\d{3}\\))|\\d{3})[- .]?\\d{3}[- .]?\\d{4}");
    private static final Pattern NAME_PATTERN = Pattern.compile("^([A-Z][a-z]+(?:\\s[A-Z][a-z]+){1,2})", Pattern.MULTILINE); // Very basic: looks for 2-3 capitalized words at the start of a line.

    // Common section headers
    private static final Pattern EXPERIENCE_HEADER_PATTERN = Pattern.compile("^(Experience|Work Experience|Employment History|Professional Experience)\\s*:?$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    private static final Pattern EDUCATION_HEADER_PATTERN = Pattern.compile("^(Education|Academic Qualifications|Educational Background)\\s*:?$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    private static final Pattern SKILLS_HEADER_PATTERN = Pattern.compile("^(Skills|Technical Skills|Proficiencies)\\s*:?$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    // Basic date pattern (needs improvement for various formats)
    private static final Pattern DATE_RANGE_PATTERN = Pattern.compile("((?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec|January|February|March|April|May|June|July|August|September|October|November|December|Present|Current)[a-z]*\\.?\\s*\\d{0,4})\\s*[-–toTO]+\\s*((?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec|January|February|March|April|May|June|July|August|September|October|November|December|Present|Current)[a-z]*\\.?\\s*\\d{0,4})", Pattern.CASE_INSENSITIVE);
    private static final Pattern YEAR_PATTERN = Pattern.compile("\\b(19|20)\\d{2}\\b");

    private static final List<String> COMMON_SKILLS_KEYWORDS = List.of(
        "Java", "Python", "JavaScript", "C++", "C#", "SQL", "React", "Angular", "Vue", "Node.js",
        "Spring Boot", "Django", "Flask", "Ruby on Rails", "PHP", "HTML", "CSS", "AWS", "Azure", "GCP",
        "Docker", "Kubernetes", "Git", "Agile", "Scrum", "Project Management", "Data Analysis", "Machine Learning"
        // This list should be expanded significantly
    );

    public PersonalDetails extractPersonalDetails(String rawText) {
        PersonalDetails details = new PersonalDetails();
        details.setName(extractName(rawText));
        details.setEmail(extractEmail(rawText));
        details.setPhone(extractPhoneNumber(rawText));
        // Address, URLs, summary extraction would require more specific patterns or section analysis
        return details;
    }

    public String extractName(String text) {
        // Attempt 1: Look for a line starting with "Name:"
        Pattern nameLabelPattern = Pattern.compile("^Name\\s*:(.*)$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher labelMatcher = nameLabelPattern.matcher(text);
        if (labelMatcher.find()) {
            return labelMatcher.group(1).trim();
        }

        // Attempt 2: Use the general NAME_PATTERN (first few lines usually contain name)
        String[] lines = text.split("\\r?\\n");
        for (int i = 0; i < Math.min(lines.length, 5); i++) { // Check first 5 lines
            Matcher nameMatcher = NAME_PATTERN.matcher(lines[i]);
            if (nameMatcher.find()) {
                return nameMatcher.group(1).trim();
            }
        }
        return null; // Name extraction is highly heuristic
    }

    public String extractEmail(String text) {
        Matcher matcher = EMAIL_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    public String extractPhoneNumber(String text) {
        Matcher matcher = PHONE_PATTERN.matcher(text);
        if (matcher.find()) {
            String phoneNumber = matcher.group();
            // Normalize: remove common non-digit characters except leading '+'
            return phoneNumber.replaceAll("[^\\d+]", "");
        }
        return null;
    }

    // Placeholder methods for more complex extractions
    public List<WorkExperience> extractWorkExperience(String rawText) {
        List<WorkExperience> experiences = new ArrayList<>();
        String experienceSectionText = findSection(rawText, EXPERIENCE_HEADER_PATTERN);

        if (experienceSectionText != null && !experienceSectionText.isEmpty()) {
            // Split the section into potential entries. This is highly heuristic.
            // Common delimiters could be multiple newlines, or lines starting with a potential job title/company.
            String[] potentialEntries = experienceSectionText.split("\\r?\\n\\s*\\r?\\n"); // Split by blank lines

            for (String entryText : potentialEntries) {
                if (entryText.trim().isEmpty()) continue;

                WorkExperience exp = new WorkExperience();
                Matcher dateMatcher = DATE_RANGE_PATTERN.matcher(entryText);
                String jobTitleAndCompany = entryText;

                if (dateMatcher.find()) {
                    exp.setStartDate(parseDate(dateMatcher.group(1)));
                    exp.setEndDate(parseDate(dateMatcher.group(2)));
                    // Assume text before date is job title/company, text after is description
                    jobTitleAndCompany = entryText.substring(0, dateMatcher.start()).trim();
                    if (dateMatcher.end() < entryText.length()) {
                        exp.setDescription(entryText.substring(dateMatcher.end()).trim());
                    }
                } else {
                    // If no clear date range, try to find standalone years for start/end
                    // This is even more heuristic
                }

                // Simplistic attempt to split job title and company
                // Assumes format like "Job Title at Company Name" or "Company Name - Job Title"
                String[] titleCompanyParts = jobTitleAndCompany.split("\\r?\\n"); // Split by newlines first
                if (titleCompanyParts.length > 0) {
                    String firstLine = titleCompanyParts[0].trim();
                    // Further split by common separators like 'at', '-', ','
                    // This needs a lot more refinement.
                    // For now, let's assume the first significant line part is the job title
                    // and the next could be the company, or vice-versa.
                    // This is a placeholder for more robust parsing logic.
                    exp.setJobTitle(firstLine); // Highly likely to be inaccurate or combined
                    if (titleCompanyParts.length > 1) {
                        exp.setCompanyName(titleCompanyParts[1].trim());
                    }
                }
                
                // Add if we have at least a potential title or date
                if (exp.getJobTitle() != null || exp.getStartDate() != null) {
                    experiences.add(exp);
                }
            }
        }
        return experiences;
    }

    public List<Education> extractEducation(String rawText) {
        List<Education> educations = new ArrayList<>();
        String educationSectionText = findSection(rawText, EDUCATION_HEADER_PATTERN);

        if (educationSectionText != null && !educationSectionText.isEmpty()) {
            String[] potentialEntries = educationSectionText.split("\\r?\\n\\s*\\r?\\n");

            for (String entryText : potentialEntries) {
                if (entryText.trim().isEmpty()) continue;

                Education edu = new Education();
                Matcher dateMatcher = DATE_RANGE_PATTERN.matcher(entryText);
                String degreeAndInstitution = entryText;

                if (dateMatcher.find()) {
                    edu.setStartDate(parseDate(dateMatcher.group(1)));
                    edu.setEndDate(parseDate(dateMatcher.group(2)));
                    degreeAndInstitution = entryText.substring(0, dateMatcher.start()).trim();
                     if (dateMatcher.end() < entryText.length()) {
                        edu.setDescription(entryText.substring(dateMatcher.end()).trim());
                    }
                }

                // Simplistic attempt for degree and institution
                String[] degreeInstParts = degreeAndInstitution.split("\\r?\\n");
                if (degreeInstParts.length > 0) {
                    edu.setDegree(degreeInstParts[0].trim()); // Placeholder
                    if (degreeInstParts.length > 1) {
                        edu.setInstitutionName(degreeInstParts[1].trim()); // Placeholder
                    }
                }
                // Field of study and grade extraction would be more complex

                if (edu.getDegree() != null || edu.getInstitutionName() != null || edu.getStartDate() != null) {
                    educations.add(edu);
                }
            }
        }
        return educations;
    }

    public List<Skill> extractSkills(String rawText) {
        List<Skill> extractedSkills = new ArrayList<>();
        String skillsSection = findSection(rawText, SKILLS_HEADER_PATTERN);
        String textToSearch = (skillsSection != null) ? skillsSection : rawText; // Search in section or whole text

        for (String keyword : COMMON_SKILLS_KEYWORDS) {
            Pattern skillPattern = Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = skillPattern.matcher(textToSearch);
            if (matcher.find()) {
                Skill skill = new Skill();
                skill.setSkillName(keyword); // Store the canonical keyword form
                // Proficiency level extraction would be more complex (e.g. "Java (Advanced)")
                extractedSkills.add(skill);
            }
        }
        // Remove duplicates that might arise if a skill is mentioned multiple times
        return extractedSkills.stream().distinct().collect(Collectors.toList());
    }

    private String findSection(String rawText, Pattern headerPattern) {
        Matcher headerMatcher = headerPattern.matcher(rawText);
        if (headerMatcher.find()) {
            int sectionStart = headerMatcher.end();
            // Try to find the start of the next common section or end of document
            Matcher nextExperienceHeader = EXPERIENCE_HEADER_PATTERN.matcher(rawText);
            Matcher nextEducationHeader = EDUCATION_HEADER_PATTERN.matcher(rawText);
            Matcher nextSkillsHeader = SKILLS_HEADER_PATTERN.matcher(rawText);

            int sectionEnd = rawText.length();

            if (nextExperienceHeader.find(sectionStart) && !headerPattern.pattern().equals(EXPERIENCE_HEADER_PATTERN.pattern())) {
                sectionEnd = Math.min(sectionEnd, nextExperienceHeader.start());
            }
            if (nextEducationHeader.find(sectionStart) && !headerPattern.pattern().equals(EDUCATION_HEADER_PATTERN.pattern())) {
                sectionEnd = Math.min(sectionEnd, nextEducationHeader.start());
            }
            if (nextSkillsHeader.find(sectionStart) && !headerPattern.pattern().equals(SKILLS_HEADER_PATTERN.pattern())) {
                sectionEnd = Math.min(sectionEnd, nextSkillsHeader.start());
            }
            return rawText.substring(sectionStart, sectionEnd).trim();
        }
        return null;
    }

    // Helper method to parse dates - very basic, needs robust implementation
    private LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        String processedDateString = dateString.trim();

        // Handle "present" or "current" case-insensitively
        if (processedDateString.toLowerCase().contains("present") || processedDateString.toLowerCase().contains("current")) {
            return LocalDate.now(); // Or null if you prefer to represent 'present' differently
        }

        // List of YearMonth formatters
        DateTimeFormatter[] yearMonthFormatters = new DateTimeFormatter[]{
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("MMMM yyyy").toFormatter(Locale.ENGLISH),
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("MMM yyyy").toFormatter(Locale.ENGLISH),
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("MMMM. yyyy").toFormatter(Locale.ENGLISH), // For "Month."
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("MMM. yyyy").toFormatter(Locale.ENGLISH),   // For "Mon."
            DateTimeFormatter.ofPattern("MM/yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("M/yyyy", Locale.ENGLISH)
        };

        for (DateTimeFormatter formatter : yearMonthFormatters) {
            try {
                YearMonth ym = YearMonth.parse(processedDateString, formatter);
                return ym.atDay(1); // Default to first day of the month
            } catch (DateTimeParseException e) {
                // Try next formatter
            }
        }

        // Try parsing as year only
        try {
            DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy", Locale.ENGLISH);
            Year year = Year.parse(processedDateString, yearFormatter);
            return year.atDay(1); // Default to Jan 1st of that year
        } catch (DateTimeParseException e) {
            // Failed to parse as year only
        }

        log.warn("Could not parse date: {}", dateString);
        return null; // Or throw an exception
    }

    // Add more extraction methods as needed (e.g., for address, URLs, summary)

}
