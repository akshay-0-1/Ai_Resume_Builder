import React from 'react';
import { Field, FieldArray, useFormikContext } from 'formik';

// Re-implemented clean version of the resume sections form. This file is meant to replace the corrupted
// ResumeSectionsForm.jsx while preserving the same public API (default export and props).

// -----------------------------------------------------------------------------
// Utility sub-components
// -----------------------------------------------------------------------------

// Stable section components defined outside main switcher to preserve DOM focus

const EducationSection = () => {
  const { values } = useFormikContext();
  return (
    <>
      <SectionHeading>Education</SectionHeading>
      <FieldArray name="educations">
        {({ push, remove }) => (
          <div className="space-y-4">
            {values.educations?.map((_, idx) => (
              <div key={idx} className="border rounded p-4 space-y-2">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                  <Field name={`educations[${idx}].institutionName`} placeholder="Institution Name" className="w-full px-2 py-1 border rounded" />
                  <Field name={`educations[${idx}].degree`} placeholder="Degree" className="w-full px-2 py-1 border rounded" />
                  <Field name={`educations[${idx}].fieldOfStudy`} placeholder="Field Of Study" className="w-full px-2 py-1 border rounded" />
                  <Field name={`educations[${idx}].grade`} placeholder="Grade" className="w-full px-2 py-1 border rounded" />
                  <Field name={`educations[${idx}].startDate`} type="text" placeholder="Start Date" className="w-full px-2 py-1 border rounded" />
                  <Field name={`educations[${idx}].endDate`} type="text" placeholder="End Date" className="w-full px-2 py-1 border rounded" />
                </div>
                <Field as="textarea" rows="2" name={`educations[${idx}].description`} placeholder="Description" className="w-full px-2 py-1 border rounded" />
                <RemoveBtn onClick={() => remove(idx)} />
              </div>
            ))}
            <AddBtn label="Education" onClick={() => push({ institutionName: '', degree: '', fieldOfStudy: '', grade: '', startDate: '', endDate: '', description: '' })} />
          </div>
        )}
      </FieldArray>
    </>
  );
};

const ExperienceSection = () => {
  const { values } = useFormikContext();
  return (
    <>
      <SectionHeading>Experience</SectionHeading>
      <FieldArray name="workExperiences">
        {({ push, remove }) => (
          <div className="space-y-4">
            {values.workExperiences?.map((_, idx) => (
              <div key={idx} className="border rounded p-4 space-y-2">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                  <Field name={`workExperiences[${idx}].jobTitle`} placeholder="Job Title" className="w-full px-2 py-1 border rounded" />
                  <Field name={`workExperiences[${idx}].companyName`} placeholder="Company Name" className="w-full px-2 py-1 border rounded" />
                  <Field name={`workExperiences[${idx}].location`} placeholder="Location" className="w-full px-2 py-1 border rounded" />
                  <label className="flex items-center space-x-2 text-sm">
                    <Field type="checkbox" name={`workExperiences[${idx}].isCurrentJob`} />
                    <span>Current Job</span>
                  </label>
                  <Field name={`workExperiences[${idx}].startDate`} type="text" placeholder="Start Date" className="w-full px-2 py-1 border rounded" />
                  <Field name={`workExperiences[${idx}].endDate`} type="text" placeholder="End Date" className="w-full px-2 py-1 border rounded" />
                </div>
                <Field as="textarea" rows="3" name={`workExperiences[${idx}].description`} placeholder="Description" className="w-full px-2 py-1 border rounded" />
                <RemoveBtn onClick={() => remove(idx)} />
              </div>
            ))}
            <AddBtn label="Experience" onClick={() => push({ jobTitle: '', companyName: '', location: '', startDate: '', endDate: '', isCurrentJob: false, description: '' })} />
          </div>
        )}
      </FieldArray>
    </>
  );
};

const ProjectsSection = () => {
  const { values } = useFormikContext();
  return (
    <>
      <SectionHeading>Projects</SectionHeading>
      <FieldArray name="projects">
        {({ push, remove }) => (
          <div className="space-y-4">
            {values.projects?.map((_, idx) => (
              <div key={idx} className="border rounded p-4 space-y-2">
                <Field name={`projects[${idx}].name`} placeholder="Project Name" className="w-full px-2 py-1 border rounded" />
                <Field name={`projects[${idx}].techStack`} placeholder="Tech Stack" className="w-full px-2 py-1 border rounded" />
                <Field name={`projects[${idx}].date`} type="text" placeholder="Date" className="w-full px-2 py-1 border rounded" />
                <Field as="textarea" rows="3" name={`projects[${idx}].achievementsText`} placeholder="Achievements (one per line)" className="w-full px-2 py-1 border rounded" />
                <RemoveBtn onClick={() => remove(idx)} />
              </div>
            ))}
            <AddBtn label="Project" onClick={() => push({ name: '', techStack: '', date: '', achievementsText: '' })} />
          </div>
        )}
      </FieldArray>
    </>
  );
};

const SkillsSection = () => {
  const { values } = useFormikContext();
  return (
    <>
      <SectionHeading>Skills</SectionHeading>
      <FieldArray name="skills">
        {({ push, remove }) => (
          <div className="space-y-4">
            {values.skills?.map((_, idx) => (
              <div key={idx} className="flex items-center space-x-3">
                <Field name={`skills[${idx}].skillName`} placeholder="Skill" className="w-full px-2 py-1 border rounded" />
                <Field as="select" name={`skills[${idx}].proficiencyLevel`} className="px-2 py-1 border rounded">
                  <option value="">Level</option>
                  <option value="Beginner">Beginner</option>
                  <option value="Intermediate">Intermediate</option>
                  <option value="Advanced">Advanced</option>
                  <option value="Expert">Expert</option>
                </Field>
                <RemoveBtn onClick={() => remove(idx)} />
              </div>
            ))}
            <AddBtn label="Skill" onClick={() => push({ skillName: '', proficiencyLevel: '' })} />
          </div>
        )}
      </FieldArray>
    </>
  );
};

const CertificatesSection = () => {
  const { values } = useFormikContext();
  return (
    <>
      <SectionHeading>Certificates</SectionHeading>
      <FieldArray name="certificates">
        {({ push, remove }) => (
          <div className="space-y-4">
            {values.certificates?.map((_, idx) => (
              <div key={idx} className="border rounded p-4 space-y-2">
                <Field name={`certificates[${idx}].name`} placeholder="Certificate Name" className="w-full px-2 py-1 border rounded" />
                <Field name={`certificates[${idx}].institution`} placeholder="Institution" className="w-full px-2 py-1 border rounded" />
                <Field name={`certificates[${idx}].date`} type="text" placeholder="Date" className="w-full px-2 py-1 border rounded" />
                <Field name={`certificates[${idx}].url`} placeholder="URL" className="w-full px-2 py-1 border rounded" />
                <RemoveBtn onClick={() => remove(idx)} />
              </div>
            ))}
            <AddBtn label="Certificate" onClick={() => push({ name: '', institution: '', date: '', url: '' })} />
          </div>
        )}
      </FieldArray>
    </>
  );
};

const SectionHeading = ({ children }) => (
  <h3 className="text-lg font-semibold mt-6 mb-2 border-b pb-1">{children}</h3>
);

const RemoveBtn = ({ onClick }) => (
  <button
    type="button"
    onClick={onClick}
    className="text-red-600 text-xs hover:underline"
  >
    Remove
  </button>
);

const AddBtn = ({ onClick, label }) => (
  <button
    type="button"
    onClick={onClick}
    className="text-blue-600 text-sm hover:underline"
  >
    + Add {label}
  </button>
);

// -----------------------------------------------------------------------------
// Main component
// -----------------------------------------------------------------------------

const ResumeSectionsForm = ({ active }) => {
  const { values, submitForm } = useFormikContext();

  // ---------------- Section render helpers ----------------

  const Education = () => (
    <>
      <SectionHeading>Education</SectionHeading>
      <FieldArray name="educations">
        {({ push, remove }) => (
          <div className="space-y-4">
            {values.educations?.map((_, idx) => (
              <div key={idx} className="border rounded p-4 space-y-2">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                  <Field name={`educations[${idx}].institutionName`} placeholder="Institution Name" className="w-full px-2 py-1 border rounded" />
                  <Field name={`educations[${idx}].degree`} placeholder="Degree" className="w-full px-2 py-1 border rounded" />
                  <Field name={`educations[${idx}].fieldOfStudy`} placeholder="Field Of Study" className="w-full px-2 py-1 border rounded" />
                  <Field name={`educations[${idx}].grade`} placeholder="Grade" className="w-full px-2 py-1 border rounded" />
                  <Field name={`educations[${idx}].startDate`} type="text" placeholder="Start Date" className="w-full px-2 py-1 border rounded" />
                  <Field name={`educations[${idx}].endDate`} type="text" placeholder="End Date" className="w-full px-2 py-1 border rounded" />
                </div>
                <Field as="textarea" rows="2" name={`educations[${idx}].description`} placeholder="Description" className="w-full px-2 py-1 border rounded" />
                <RemoveBtn onClick={() => remove(idx)} />
              </div>
            ))}
            <AddBtn label="Education" onClick={() => push({ institutionName: '', degree: '', fieldOfStudy: '', grade: '', startDate: '', endDate: '', description: '' })} />
          </div>
        )}
      </FieldArray>
    </>
  );

  const Experience = () => (
    <>
      <SectionHeading>Experience</SectionHeading>
      <FieldArray name="workExperiences">
        {({ push, remove }) => (
          <div className="space-y-4">
            {values.workExperiences?.map((_, idx) => (
              <div key={idx} className="border rounded p-4 space-y-2">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                  <Field name={`workExperiences[${idx}].jobTitle`} placeholder="Job Title" className="w-full px-2 py-1 border rounded" />
                  <Field name={`workExperiences[${idx}].companyName`} placeholder="Company Name" className="w-full px-2 py-1 border rounded" />
                  <Field name={`workExperiences[${idx}].location`} placeholder="Location" className="w-full px-2 py-1 border rounded" />
                  <label className="flex items-center space-x-2 text-sm">
                    <Field type="checkbox" name={`workExperiences[${idx}].isCurrentJob`} />
                    <span>Current Job</span>
                  </label>
                  <Field name={`workExperiences[${idx}].startDate`} type="text" placeholder="Start Date" className="w-full px-2 py-1 border rounded" />
                  <Field name={`workExperiences[${idx}].endDate`} type="text" placeholder="End Date" className="w-full px-2 py-1 border rounded" />
                </div>
                <Field as="textarea" rows="3" name={`workExperiences[${idx}].description`} placeholder="Description" className="w-full px-2 py-1 border rounded" />
                <RemoveBtn onClick={() => remove(idx)} />
              </div>
            ))}
            <AddBtn label="Experience" onClick={() => push({ jobTitle: '', companyName: '', location: '', startDate: '', endDate: '', isCurrentJob: false, description: '' })} />
          </div>
        )}
      </FieldArray>
    </>
  );

  const Projects = () => (
    <>
      <SectionHeading>Projects</SectionHeading>
      <FieldArray name="projects">
        {({ push, remove }) => (
          <div className="space-y-4">
            {values.projects?.map((_, idx) => (
              <div key={idx} className="border rounded p-4 space-y-2">
                <Field name={`projects[${idx}].name`} placeholder="Project Name" className="w-full px-2 py-1 border rounded" />
                <Field name={`projects[${idx}].techStack`} placeholder="Tech Stack" className="w-full px-2 py-1 border rounded" />
                <Field name={`projects[${idx}].date`} type="text" placeholder="Date" className="w-full px-2 py-1 border rounded" />
                <Field as="textarea" rows="3" name={`projects[${idx}].achievementsText`} placeholder="Achievements (one per line)" className="w-full px-2 py-1 border rounded" />
                <RemoveBtn onClick={() => remove(idx)} />
              </div>
            ))}
            <AddBtn label="Project" onClick={() => push({ name: '', techStack: '', date: '', achievementsText: '' })} />
          </div>
        )}
      </FieldArray>
    </>
  );

  const Skills = () => (
    <>
      <SectionHeading>Skills</SectionHeading>
      <FieldArray name="skills">
        {({ push, remove }) => (
          <div className="space-y-4">
            {values.skills?.map((_, idx) => (
              <div key={idx} className="flex items-center space-x-3">
                <Field name={`skills[${idx}].skillName`} placeholder="Skill" className="w-full px-2 py-1 border rounded" />
                <Field as="select" name={`skills[${idx}].proficiencyLevel`} className="px-2 py-1 border rounded">
                  <option value="">Level</option>
                  <option value="Beginner">Beginner</option>
                  <option value="Intermediate">Intermediate</option>
                  <option value="Advanced">Advanced</option>
                  <option value="Expert">Expert</option>
                </Field>
                <RemoveBtn onClick={() => remove(idx)} />
              </div>
            ))}
            <AddBtn label="Skill" onClick={() => push({ skillName: '', proficiencyLevel: '' })} />
          </div>
        )}
      </FieldArray>
    </>
  );

  const Certificates = () => (
    <>
      <SectionHeading>Certificates</SectionHeading>
      <FieldArray name="certificates">
        {({ push, remove }) => (
          <div className="space-y-4">
            {values.certificates?.map((_, idx) => (
              <div key={idx} className="border rounded p-4 space-y-2">
                <Field name={`certificates[${idx}].name`} placeholder="Certificate Name" className="w-full px-2 py-1 border rounded" />
                <Field name={`certificates[${idx}].institution`} placeholder="Institution" className="w-full px-2 py-1 border rounded" />
                <Field name={`certificates[${idx}].date`} type="text" placeholder="Date" className="w-full px-2 py-1 border rounded" />
                <Field name={`certificates[${idx}].url`} placeholder="URL" className="w-full px-2 py-1 border rounded" />
                <RemoveBtn onClick={() => remove(idx)} />
              </div>
            ))}
            <AddBtn label="Certificate" onClick={() => push({ name: '', institution: '', date: '', url: '' })} />
          </div>
        )}
      </FieldArray>
    </>
  );

  // ---------------------------------------------------------------------------
  // Rendering
  // ---------------------------------------------------------------------------

  if (!active) return null;

  const sectionComponents = {
    education: <EducationSection />,
    experience: <ExperienceSection />,
    projects: <ProjectsSection />,
    skills: <SkillsSection />,
    certificates: <CertificatesSection />,
  };

  return (
    <>
      {sectionComponents[active]}

    </>
  );
};

export default ResumeSectionsForm;
