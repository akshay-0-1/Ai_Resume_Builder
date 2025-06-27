package com.project.resumeTracker.util;

import com.project.resumeTracker.dto.ResumeUpdateDTO;
import com.project.resumeTracker.entity.Resume;
import com.project.resumeTracker.entity.PersonalDetails;
import com.project.resumeTracker.entity.WorkExperience;
import com.project.resumeTracker.entity.Education;
import com.project.resumeTracker.entity.Skill;
import com.project.resumeTracker.entity.Project;
import com.project.resumeTracker.entity.Certificate;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

public class ResumeUtils {

    public static void updateResumeData(Resume resume, ResumeUpdateDTO updateDTO) {
        // Personal details
        if (updateDTO.getPersonalDetails() != null) {
            PersonalDetails personalDetails = resume.getPersonalDetails();
            if (personalDetails == null) {
                personalDetails = new PersonalDetails();
                resume.setPersonalDetails(personalDetails);
            }
            updatePersonalDetails(personalDetails, updateDTO.getPersonalDetails());
        }

        // Collections
        updateCollection(resume::getWorkExperiences, resume::addWorkExperience, updateDTO.getWorkExperiences(), WorkExperience::new);
        updateCollection(resume::getEducations, resume::addEducation, updateDTO.getEducations(), Education::new);
        updateCollection(resume::getSkills, resume::addSkill, updateDTO.getSkills(), Skill::new);
        updateCollection(resume::getProjects, resume::addProject, updateDTO.getProjects(), Project::new);
        updateCollection(resume::getCertificates, resume::addCertificate, updateDTO.getCertificates(), Certificate::new);
    }

    private static <T, DTO> void updateCollection(
            java.util.function.Function<Resume, List<T>> getter,
            java.util.function.BiConsumer<Resume, T> adder,
            List<DTO> dtos,
            java.util.function.Supplier<T> entitySupplier) {

        if (dtos == null || dtos.isEmpty()) return;

        List<T> collection = getter.apply(resume);
        if (collection == null) {
            collection = CollectionUtils.arrayToList(new T[0]);
            resume.setCollection(collection);
        }
        collection.clear();

        for (DTO dto : dtos) {
            T entity = entitySupplier.get();
            updateEntity(entity, dto);
            adder.accept(resume, entity);
        }
    }

    private static void updatePersonalDetails(PersonalDetails entity, PersonalDetailsDTO dto) {
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setAddress(dto.getAddress());
        entity.setLinkedinUrl(dto.getLinkedinUrl());
        entity.setGithubUrl(dto.getGithubUrl());
        entity.setPortfolioUrl(dto.getPortfolioUrl());
        entity.setSummary(dto.getSummary());
    }

    private static void updateEntity(Object entity, Object dto) {
        if (entity instanceof WorkExperience && dto instanceof WorkExperienceDTO) {
            WorkExperience we = (WorkExperience) entity;
            WorkExperienceDTO weDto = (WorkExperienceDTO) dto;
            we.setJobTitle(weDto.getJobTitle());
            we.setCompanyName(weDto.getCompanyName());
            we.setLocation(weDto.getLocation());
            we.setStartDate(weDto.getStartDate());
            we.setEndDate(weDto.getEndDate());
            we.setCurrentJob(Boolean.TRUE.equals(weDto.getCurrentJob()));
            we.setDescription(weDto.getDescription());
        } else if (entity instanceof Education && dto instanceof EducationDTO) {
            Education ed = (Education) entity;
            EducationDTO edDto = (EducationDTO) dto;
            ed.setInstitutionName(edDto.getInstitutionName());
            ed.setDegree(edDto.getDegree());
            ed.setFieldOfStudy(edDto.getFieldOfStudy());
            ed.setStartDate(edDto.getStartDate());
            ed.setEndDate(edDto.getEndDate());
            ed.setGrade(edDto.getGrade());
            ed.setDescription(edDto.getDescription());
        } else if (entity instanceof Skill && dto instanceof SkillDTO) {
            Skill skill = (Skill) entity;
            SkillDTO skillDto = (SkillDTO) dto;
            skill.setSkillName(skillDto.getSkillName());
            skill.setProficiencyLevel(skillDto.getProficiencyLevel());
        } else if (entity instanceof Project && dto instanceof com.project.resumeTracker.dto.Project) {
            Project p = (Project) entity;
            com.project.resumeTracker.dto.Project pDto = (com.project.resumeTracker.dto.Project) dto;
            p.setName(pDto.getName());
            p.setTechStack(pDto.getTechStack());
            p.setDate(pDto.getDate());
            p.setAchievements(pDto.getAchievements());
        } else if (entity instanceof Certificate && dto instanceof com.project.resumeTracker.dto.Certificate) {
            Certificate c = (Certificate) entity;
            com.project.resumeTracker.dto.Certificate cDto = (com.project.resumeTracker.dto.Certificate) dto;
            c.setName(cDto.getName());
            if (cDto.getDate() != null && !cDto.getDate().isEmpty()) {
                c.setDate(java.time.LocalDate.parse(cDto.getDate()));
            }
            c.setInstitution(cDto.getInstitution());
            c.setUrl(cDto.getUrl());
        }
    }

    private ResumeUtils() {
        throw new AssertionError("Cannot instantiate utility class");
    }
}
