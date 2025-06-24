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
            templateData.put("name", escapeLatex(p.getName()));
            templateData.put("email", escapeLatex(p.getEmail()));
            templateData.put("phone", escapeLatex(p.getPhone()));
            templateData.put("location", escapeLatex(p.getAddress()));
            templateData.put("github", escapeLatex(p.getGithubUrl()));
            templateData.put("linkedin", escapeLatex(p.getLinkedinUrl()));
        } else {
            templateData.put("name", "");
            templateData.put("email", "");
            templateData.put("phone", "");
            templateData.put("location", "");
            templateData.put("github", "");
            templateData.put("linkedin", "");
        }

        // Education
        StringBuilder eduBuilder = new StringBuilder();
        if (resume.getEducations() != null) {
            for (Education edu : resume.getEducations()) {
                String dates = (edu.getStartDate() != null ? edu.getStartDate().toString() : "") + " -- " + (edu.getEndDate() != null ? edu.getEndDate().toString() : "Present");
                eduBuilder.append(String.format("\\resumeSubheading{%s}{%s}{%s}{%s}\n", 
                    escapeLatex(edu.getInstitutionName()), escapeLatex(dates), escapeLatex(edu.getDegree()), escapeLatex(edu.getFieldOfStudy())));
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
                    escapeLatex(exp.getJobTitle()), escapeLatex(dates), escapeLatex(exp.getCompanyName()), escapeLatex(exp.getLocation())));
                if (exp.getDescription() != null && !exp.getDescription().isBlank()) {
                    expBuilder.append("    \\resumeItemListStart\n");
                    for (String item : exp.getDescription().split("\\r?\\n")) {
                        if (item != null && !item.trim().isEmpty()) expBuilder.append(String.format("        \\resumeItem{%s}\n", escapeLatex(item.trim())));
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
                String heading = String.format("\\textbf{%s} | \\emph{%s}", escapeLatex(proj.getName()), escapeLatex(proj.getTechStack()));
                projBuilder.append(String.format("\\resumeProjectHeading{%s}{%s}\n", heading, escapeLatex(proj.getDate())));
                if (proj.getAchievements() != null && !proj.getAchievements().isEmpty()) {
                    projBuilder.append("    \\resumeItemListStart\n");
                    for (String ach : proj.getAchievements()) {
                        if (ach != null && !ach.trim().isEmpty()) projBuilder.append(String.format("        \\resumeItem{%s}\n", escapeLatex(ach.trim())));
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
                        String cat = s.getCategory() != null && !s.getCategory().isBlank() ? escapeLatex(s.getCategory()) : "Other";
                        byCategory.computeIfAbsent(cat, k -> new java.util.ArrayList<>()).add(escapeLatex(s.getSkillName()));
                    });

            StringBuilder skillsBuilder = new StringBuilder();
            int idx = 0;
            for (var entry : byCategory.entrySet()) {
                if (idx++ > 0) skillsBuilder.append(" \\\\n"); // new line between categories
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
                certBuilder.append(String.format("        \\textbf{%s} \\hfill \\textbf{\\textit{Issued %s}}\\\\n", escapeLatex(cert.getName()), escapeLatex(issuedDate)));
                certBuilder.append(String.format("        \\textit{%s} \\hfill \\href{%s}{\\textit{ Link}}\n", escapeLatex(cert.getInstitution()), escapeLatex(cert.getUrl())));
                certBuilder.append("    }\n");
            }
        }
        if (log.isDebugEnabled()) log.debug("Generated certificates LaTeX:\n{}", certBuilder);
        templateData.put("certificates", certBuilder.length() == 0 ? "\\item{}" : certBuilder.toString());

        return templateData;
    }

    private String escapeLatex(String s) {
        if (s == null || "null".equalsIgnoreCase(s.trim())) {
            return "";
        }
        String trimmed = s.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        return trimmed.replace("\\", "\\textbackslash{}")
                .replace("&", "\\&")
                .replace("%", "\\%")
                .replace("$", "\\$")
                .replace("#", "\\#")
                .replace("_", "\\_")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("~", "\\textasciitilde{}")
                .replace("^", "\\textasciicircum{}");
    }
}
