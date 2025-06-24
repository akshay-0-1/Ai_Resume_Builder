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
        PersonalDetails personalDetails = resume.getPersonalDetails();
        if (personalDetails != null) {
            templateData.put("name", escapeLatex(personalDetails.getName()));
            templateData.put("email", escapeLatex(personalDetails.getEmail()));
            templateData.put("phone", escapeLatex(personalDetails.getPhone()));
            templateData.put("location", escapeLatex(personalDetails.getAddress()));
            templateData.put("github", escapeLatex(personalDetails.getGithubUrl()));
            templateData.put("linkedin", escapeLatex(personalDetails.getLinkedinUrl()));
        } else {
            templateData.put("name", "");
            templateData.put("email", "");
            templateData.put("phone", "");
            templateData.put("location", "");
            templateData.put("github", "");
            templateData.put("linkedin", "");
        }

        // Education
        StringBuilder educationBuilder = new StringBuilder();
        if (resume.getEducations() != null && !resume.getEducations().isEmpty()) {
            for (Education education : resume.getEducations()) {
                educationBuilder.append("    \\resumeSubheading\n");
                String startDate = education.getStartDate() != null ? education.getStartDate().toString() : "";
                String endDate = education.getEndDate() != null ? education.getEndDate().toString() : "Present";
                educationBuilder.append("      {").append(escapeLatex(education.getInstitutionName())).append("}{").append(escapeLatex(startDate)).append(" -- ").append(escapeLatex(endDate)).append("}\n");
                educationBuilder.append("      {").append(escapeLatex(education.getDegree())).append("}{").append(escapeLatex(education.getFieldOfStudy())).append("}\n");

                if (education.getDescription() != null && !education.getDescription().isBlank()) {
                    educationBuilder.append("    \\resumeItemListStart\n");
                    String[] items = education.getDescription().split("\\r?\\n");
                    for (String item : items) {
                        if (item != null && !item.trim().isEmpty()) {
                            educationBuilder.append("        \\resumeItem{").append(escapeLatex(item.trim())).append("}\n");
                        }
                    }
                    educationBuilder.append("    \\resumeItemListEnd\n");
                }
            }
        }
        templateData.put("education", educationBuilder.toString());

        // Experience
        StringBuilder experienceBuilder = new StringBuilder();
        if (resume.getWorkExperiences() != null && !resume.getWorkExperiences().isEmpty()) {
            for (WorkExperience exp : resume.getWorkExperiences()) {
                experienceBuilder.append("    \\resumeSubheading\n");
                String startDate = exp.getStartDate() != null ? exp.getStartDate().toString() : "";
                String endDate = exp.getEndDate() != null ? exp.getEndDate().toString() : "Present";
                experienceBuilder.append("      {").append(escapeLatex(exp.getJobTitle())).append("}{").append(escapeLatex(startDate)).append(" -- ").append(escapeLatex(endDate)).append("}\n");
                experienceBuilder.append("      {").append(escapeLatex(exp.getCompanyName())).append("}{").append(escapeLatex(exp.getLocation())).append("}\n");
                
                if (exp.getDescription() != null && !exp.getDescription().isBlank()) {
                    experienceBuilder.append("    \\resumeItemListStart\n");
                    String[] items = exp.getDescription().split("\\r?\\n");
                    for (String item : items) {
                        if (item != null && !item.trim().isEmpty()) {
                            experienceBuilder.append("        \\resumeItem{").append(escapeLatex(item.trim())).append("}\n");
                        }
                    }
                    experienceBuilder.append("    \\resumeItemListEnd\n");
                }
            }
        }
        templateData.put("experience", experienceBuilder.toString());

        // Projects
        StringBuilder projectsBuilder = new StringBuilder();
        if (resume.getProjects() != null && !resume.getProjects().isEmpty()) {
            projectsBuilder.append("\\section{Projects}\n");
            projectsBuilder.append("    \\resumeSubHeadingListStart\n");
            for (Project project : resume.getProjects()) {
                projectsBuilder.append("    \\resumeProjectHeading\n");
                projectsBuilder.append("      {\\textbf{").append(escapeLatex(project.getName())).append("} | \\textit{").append(escapeLatex(project.getTechStack())).append("}}{").append(project.getDate() != null ? project.getDate().toString() : "").append("}\n");
                if (project.getAchievements() != null && !project.getAchievements().isEmpty()) {
                    projectsBuilder.append("    \\resumeItemListStart\n");
                    for (String achievement : project.getAchievements()) {
                        if (achievement != null && !achievement.trim().isEmpty()) {
                            projectsBuilder.append("        \\resumeItem{").append(escapeLatex(achievement.trim())).append("}\n");
                        }
                    }
                    projectsBuilder.append("    \\resumeItemListEnd\n");
                }
            }
            projectsBuilder.append("    \\resumeSubHeadingListEnd\n");
        }
        templateData.put("projects", projectsBuilder.toString());

        // Skills
        log.info("======================================================");
        log.info("PREPARING SKILLS FOR LATEX TEMPLATE (Java 21 String Templates)");
        log.info("======================================================");

        String skillsContent = "";
        if (resume.getSkills() != null && !resume.getSkills().isEmpty()) {
            log.info("Processing {} skills with Java 21 String Templates.", resume.getSkills().size());
            
            skillsContent = resume.getSkills().stream()
                .filter(skill -> skill != null && skill.getSkillName() != null && !skill.getSkillName().isBlank())
                .map(skill -> {
                    String skillName = escapeLatex(skill.getSkillName());
                    String proficiency = skill.getProficiencyLevel();
                    // Defensively check for proficiency to avoid "null" strings
                    if (proficiency == null || proficiency.isBlank() || "null".equalsIgnoreCase(proficiency.trim())) {
                        return "\\item \\textbf{" + skillName + "}";
                    } else {
                        String escapedProficiency = escapeLatex(proficiency);
                        return "\\item \\textbf{" + skillName + "}: " + escapedProficiency;
                    }
                })
                .collect(java.util.stream.Collectors.joining("\n"));

        } else {
            log.warn("Skills list is null or empty. Setting empty string for skills.");
        }
        templateData.put("skills", skillsContent);

        log.info("======================================================");
        log.info("FINAL GENERATED SKILLS CONTENT (Java 21):");
        log.info("\n---\n{}\n---", skillsContent);
        log.info("======================================================");

        // Certificates
        StringBuilder certificatesBuilder = new StringBuilder();
        if (resume.getCertificates() != null && !resume.getCertificates().isEmpty()) {
            certificatesBuilder.append("\\section{Certificates}\n");
            certificatesBuilder.append("    \\begin{itemize}\n");
            for (Certificate certificate : resume.getCertificates()) {
                certificatesBuilder.append("    \\item{\n");
                certificatesBuilder.append("        \\textbf{").append(escapeLatex(certificate.getName())).append("} \\hfill \\textbf{\\textit{Issued ").append(escapeLatex(certificate.getDate() != null ? certificate.getDate().toString() : "")).append("}}\\\\n");
                certificatesBuilder.append("        \\textit{").append(escapeLatex(certificate.getInstitution())).append("} \\hfill \\href{").append(escapeLatex(certificate.getUrl())).append("}{\\textit{ Link}}\n");
                certificatesBuilder.append("    }\n");
            }
            certificatesBuilder.append("    \\end{itemize}\n");
        }
        templateData.put("certificates", certificatesBuilder.toString());

        log.info("Prepared LaTeX template data for resumeId: {}. Data: {}", resume.getId(), templateData);
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
