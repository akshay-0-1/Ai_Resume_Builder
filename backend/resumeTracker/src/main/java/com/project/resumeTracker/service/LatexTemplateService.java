package com.project.resumeTracker.service;

import org.springframework.stereotype.Service;
import com.project.resumeTracker.entity.Resume;
import com.project.resumeTracker.entity.Education;
import com.project.resumeTracker.entity.WorkExperience;
import com.project.resumeTracker.entity.Project;
import com.project.resumeTracker.entity.Skill;
import com.project.resumeTracker.entity.Certificate;
import com.project.resumeTracker.entity.PersonalDetails;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class LatexTemplateService {


    private static final Logger log = LoggerFactory.getLogger(LatexTemplateService.class);
    private static final String TEMPLATE_PATH = "templates/";
    private static final String DEFAULT_TEMPLATE = "professional-resume.tex";

    public String getTemplateContent() throws IOException {
        Resource resource = new ClassPathResource("templates/professional-resume.tex");
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public String replacePlaceholders(String template, Map<String, String> data) {
        String result = template;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue() != null ? entry.getValue() : "");
        }
        return result;
    }

    public Map<String, String> prepareDataForTemplate(Resume resume) {
        Map<String, String> templateData = new HashMap<>();

        // Personal Information
        PersonalDetails p = resume.getPersonalDetails();
        if (p != null) {
            String name = escapeLatexSpecialChars(p.getName());
            String location = escapeLatexSpecialChars(p.getAddress());
            // Create a single line heading with proper spacing
            StringBuilder heading = new StringBuilder();
            heading.append("{\\Huge \\scshape \\textbf{").append(name).append("}} \\\\ ");
            if (!location.isEmpty()) {
                heading.append("\\hspace{0.5em}").append(location).append(" \\\\ ");
            }

            templateData.put("heading", heading.toString());

            StringBuilder contactInfo = new StringBuilder();
            contactInfo.append("{\\small ");

            String phone = p.getPhone() != null ? escapeLatexSpecialChars(p.getPhone()) : "";
            String email = p.getEmail() != null ? escapeLatexSpecialChars(p.getEmail()) : "";
            String github = p.getGithubUrl() != null ? escapeLatexSpecialChars(p.getGithubUrl()) : "";
            String linkedin = p.getLinkedinUrl() != null ? escapeLatexSpecialChars(p.getLinkedinUrl()) : "";

            if (!phone.isEmpty()) {
                contactInfo.append("\\faPhone\\ ").append(phone).append(" \\hspace{0.5em} ");
            }
            if (!email.isEmpty()) {
                contactInfo.append("\\href{mailto:").append(email).append("}{\\faEnvelope\\ \\underline{")
                             .append(email).append("}} \\hspace{0.5em} ");
            }
            if (!github.isEmpty()) {
                contactInfo.append("\\href{").append(github).append("}{\\faGithub\\ \\underline{")
                              .append(github.substring(github.lastIndexOf("/") + 1)).append("}} \\hspace{0.5em} ");
            }
            if (!linkedin.isEmpty()) {
                contactInfo.append("\\href{https://www.linkedin.com/in/").append(linkedin).append("}{\\faLinkedin\\ \\underline{linkedin.com/in/")
                              .append(linkedin).append("}}");
            }

            contactInfo.append("}");
            templateData.put("contactInfo", contactInfo.toString());
        } else {
            templateData.put("heading", "");
            templateData.put("contactInfo", "");
        }

        // Education
        StringBuilder eduBuilder = new StringBuilder();
        if (resume.getEducations() != null) {
            for (Education edu : resume.getEducations()) {
                String dates = (edu.getStartDate() != null ? edu.getStartDate().toString() : "") + " -- " + (edu.getEndDate() != null ? edu.getEndDate().toString() : "Present");
                eduBuilder.append(String.format("\\resumeSubheading{%s}{%s}{%s}{%s}\n", 
                    escapeLatexSpecialChars(edu.getInstitutionName()), escapeLatexSpecialChars(dates), escapeLatexSpecialChars(edu.getDegree()), escapeLatexSpecialChars(edu.getFieldOfStudy())));
            }
        }
        templateData.put("education", eduBuilder.length() == 0 ? "\\item{}" : eduBuilder.toString());

        // Experience
        StringBuilder expBuilder = new StringBuilder();
        if (resume.getWorkExperiences() != null) {
            for (WorkExperience exp : resume.getWorkExperiences()) {
                String dates = (exp.getStartDate() != null ? exp.getStartDate().format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy")) : "") + " -- " + 
                               (exp.isCurrentJob() || exp.getEndDate() == null ? "Present" : exp.getEndDate().format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy")));
                expBuilder.append(String.format("\\resumeSubheading{%s}{%s}{%s}{%s}\n", 
                    escapeLatexSpecialChars(exp.getJobTitle()), escapeLatexSpecialChars(dates), escapeLatexSpecialChars(exp.getCompanyName()), escapeLatexSpecialChars(exp.getLocation())));
                if (exp.getDescription() != null && !exp.getDescription().isBlank()) {
                    expBuilder.append("    \\resumeItemListStart\n");
                    for (String item : exp.getDescription().split("\\r?\\n")) {
                        if (item != null && !item.trim().isEmpty()) expBuilder.append(String.format("        \\resumeItem{%s}\n", escapeLatexSpecialChars(item.trim())));
                    }
                    expBuilder.append("    \\resumeItemListEnd\n");
                }
            }
        }
        templateData.put("experience", expBuilder.length() == 0 ? "\\item{}" : expBuilder.toString());

        // Projects
        StringBuilder projBuilder = new StringBuilder();
        if (resume.getProjects() != null) {
            for (Project proj : resume.getProjects()) {
                String heading = String.format("\\textbf{%s} | \\emph{%s}", escapeLatexSpecialChars(proj.getName()), escapeLatexSpecialChars(proj.getTechStack()));
                projBuilder.append(String.format("\\resumeProjectHeading{%s}{%s}\n", heading, escapeLatexSpecialChars(proj.getDate())));
                if (proj.getAchievements() != null && !proj.getAchievements().isEmpty()) {
                    projBuilder.append("    \\resumeItemListStart\n");
                    for (String ach : proj.getAchievements()) {
                        if (ach != null && !ach.trim().isEmpty()) projBuilder.append(String.format("        \\resumeItem{%s}\n", escapeLatexSpecialChars(ach.trim())));
                    }
                    projBuilder.append("    \\resumeItemListEnd\n");
                }
            }
        }
        if (log.isDebugEnabled()) log.debug("Generated projects LaTeX:\n{}", projBuilder);
        templateData.put("projects", projBuilder.length() == 0 ? "\\item{}" : projBuilder.toString());

        // Skills
        if (resume.getSkills() != null && !resume.getSkills().isEmpty()) {
            java.util.Map<String, java.util.List<String>> byCategory = new java.util.LinkedHashMap<>();
            resume.getSkills().stream()
                    .filter(s -> s != null && s.getSkillName() != null && !s.getSkillName().isBlank())
                    .forEach(s -> {
                        String cat = s.getCategory() != null && !s.getCategory().isBlank() ? escapeLatexSpecialChars(s.getCategory()) : "Other";
                        byCategory.computeIfAbsent(cat, k -> new java.util.ArrayList<>()).add(escapeLatexSpecialChars(s.getSkillName()));
                    });

            StringBuilder skillsBuilder = new StringBuilder();
            int idx = 0;
            for (var entry : byCategory.entrySet()) {
                if (idx++ > 0) skillsBuilder.append(" \\\\"); // new line between categories
                skillsBuilder.append(String.format("\\textbf{%s}{: %s}", entry.getKey(), String.join(", ", entry.getValue())));
            }
            String skillsBlock = skillsBuilder.toString();
            templateData.put("skills", skillsBlock.isBlank() ? "\\item{}" : "\\item{" + skillsBlock + "}");
        } else {
            templateData.put("skills", "\\item{}");
        }

        // Certificates
        StringBuilder certBuilder = new StringBuilder();
        if (resume.getCertificates() != null) {
            for (Certificate cert : resume.getCertificates()) {
                String issuedDate = cert.getDate() != null ? cert.getDate().toString() : "";
                certBuilder.append("    \\item{\n");
                certBuilder.append(String.format("        \\textbf{%s} \\hfill \\textbf{\\textit{Issued %s}} \\\\ ", escapeLatexSpecialChars(cert.getName()), escapeLatexSpecialChars(issuedDate)));
                certBuilder.append(String.format("        \\textit{%s} \\hfill \\href{%s}{\\textit{ Link}}\n", escapeLatexSpecialChars(cert.getInstitution()), escapeLatexSpecialChars(cert.getUrl())));
                certBuilder.append("    }\n");
            }
        }
        if (log.isDebugEnabled()) log.debug("Generated certificates LaTeX:\n{}", certBuilder);
        templateData.put("certificates", certBuilder.length() == 0 ? "\\item{}" : certBuilder.toString());

        return templateData;
    }

    private String escapeLatexSpecialChars(String input) {
        if (input == null || input.isEmpty()) return "";
        return input
            .replace("\\", "\\\\")
            .replace("{", "\\{")
            .replace("}", "\\}")
            .replace("%", "\\%")
            .replace("#", "\\#")
            .replace("_", "\\_")
            .replace("&", "\\&")
            .replace("$", "\\$")
            .replace("~", "\\textasciitilde{}")
            .replace("^", "\\textasciicircum{}")
            // replace common unicode bullet characters with LaTeX-safe bullet symbol
            .replace("\u2022", "$\\bullet$") // •
            .replace("\u25AA", "$\\blacksmallsquare$");
    }
}
