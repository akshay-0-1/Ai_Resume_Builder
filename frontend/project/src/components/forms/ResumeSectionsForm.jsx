// Deprecated legacy version of ResumeSectionsForm.
// The implementation has moved to ResumeSectionsFormV2.jsx.
export default function DeprecatedResumeSectionsForm() {
  return null;
}

import { Field, FieldArray, useFormikContext } from 'formik';

// Utility sub-components = ({ children }) => (
  <h3 className="text-lg font-semibold mt-6 mb-2 border-b pb-1">{children}</h3>
);
 = ({ onClick }) => (
  <button type="button" onClick={onClick} className="text-red-600 text-xs hover:underline">
    Remove
  </button>
);
 = ({ onClick, label }) => (
  <button type="button" onClick={onClick} className="text-blue-600 text-sm hover:underline">
    + Add {label}
  </button>
);
 = ({ active }) => {
  const { values, submitForm } = useFormikContext();

  // ---------- Render helpers for each section ---------- //
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
                  <Field name={`educations[${idx}].startDate`} type="date" placeholder="Start Date" className="w-full px-2 py-1 border rounded" />
                  <Field name={`educations[${idx}].endDate`} type="date" placeholder="End Date" className="w-full px-2 py-1 border rounded" />
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
                  <Field name={`workExperiences[${idx}].startDate`} type="date" placeholder="Start Date" className="w-full px-2 py-1 border rounded" />
                  <Field name={`workExperiences[${idx}].endDate`} type="date" placeholder="End Date" className="w-full px-2 py-1 border rounded" />
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
                <Field name={`projects[${idx}].date`} type="date" placeholder="Date" className="w-full px-2 py-1 border rounded" />
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
                <Field name={`certificates[${idx}].date`} type="date" placeholder="Date" className="w-full px-2 py-1 border rounded" />
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

  // If nothing selected
  if (!active) return null;

  const sectionComponents = {
    education: <Education />,
    experience: <Experience />,
    projects: <Projects />,
    skills: <Skills />,
    certificates: <Certificates />,
  };

  return (
    <>
      {sectionComponents[active]}
      <div className="mt-4">
        <button type="button" onClick={submitForm} className="px-4 py-2 bg-blue-600 text-white rounded shadow">
          Save Section
        </button>
      </div>
    </>
  );

 * Renders dynamic FieldArray sections for the resume form (education, experience, skills, projects, certificates).
 * The component expects the parent Formik form to have the following shape:
 * {
 *   educations: [ { institutionName, degree, fieldOfStudy, startDate, endDate, grade, description } ],
 *   workExperiences: [ { jobTitle, companyName, location, startDate, endDate, isCurrentJob, description } ],
 *   skills: [ { skillName, proficiencyLevel } ],
 *   projects: [ { name, techStack, date, achievements: [] } ],
 *   certificates: [ { name, date, institution, url } ] */
 = ({ children }) => (
  <h3 className="text-lg font-semibold mt-6 mb-2 border-b pb-1">{children}</h3>
);
 = ({ onClick }) => (
  <button
    type="button"
    onClick={onClick}
    className="text-red-600 text-xs hover:underline"
  >
    Remove
  </button>
);
 = ({ onClick, label }) => (
  <button
    type="button"
    onClick={onClick}
    className="text-blue-600 text-sm hover:underline"
  >
    + Add {label}
  </button>
);
 = ({ active }) => {
  const { values, submitForm } = useFormikContext();

  if (!active) return null;




  return (
    <>
      {/* EDUCATION */}
      {active === 'education' && (
        <>
      <SectionHeading>Education</SectionHeading>
      <FieldArray name="educations">
        {({ push, remove }) => (
          <div className="space-y-4">
            {values.educations && values.educations.map((_, idx) => (
              <div key={idx} className="border rounded p-4 space-y-2">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                  <Field name={`educations[${idx}].institutionName`} placeholder="Institution Name" className="w-full px-2 py-1 border rounded" />
                  <Field name={`educations[${idx}].degree`} placeholder="Degree" className="w-full px-2 py-1 border rounded" />
                  <Field name={`educations[${idx}].fieldOfStudy`} placeholder="Field Of Study" className="w-full px-2 py-1 border rounded" />
                  <Field name={`educations[${idx}].grade`} placeholder="Grade" className="w-full px-2 py-1 border rounded" />
                  <Field name={`educations[${idx}].startDate`} type="date" placeholder="Start Date" className="w-full px-2 py-1 border rounded" />
                  <Field name={`educations[${idx}].endDate`} type="date" placeholder="End Date" className="w-full px-2 py-1 border rounded" />
                </div>
                <Field as="textarea" rows="2" name={`educations[${idx}].description`} placeholder="Description" className="w-full px-2 py-1 border rounded" />
                <RemoveBtn onClick={() => remove(idx)} />
              </div>
            ))}
            <AddBtn label="Education" onClick={() => push({ institutionName: '', degree: '', fieldOfStudy: '', startDate: '', endDate: '', grade: '', description: '' })} />
          </div>
        )}
      </FieldArray>
      )

      {/* EXPERIENCE */}
      {active === 'experience' && (
      <SectionHeading>Experience</SectionHeading>
      <FieldArray name="workExperiences">
        {({ push, remove }) => (
          <div className="space-y-4">
            {values.workExperiences && values.workExperiences.map((_, idx) => (
              <div key={idx} className="border rounded p-4 space-y-2">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                  <Field name={`workExperiences[${idx}].jobTitle`} placeholder="Job Title" className="w-full px-2 py-1 border rounded" />
                  <Field name={`workExperiences[${idx}].companyName`} placeholder="Company Name" className="w-full px-2 py-1 border rounded" />
                  <Field name={`workExperiences[${idx}].location`} placeholder="Location" className="w-full px-2 py-1 border rounded" />
                  <label className="flex items-center space-x-2 text-sm">
                    <Field type="checkbox" name={`workExperiences[${idx}].isCurrentJob`} />
                    <span>Current Job</span>
                  </label>
                  <Field name={`workExperiences[${idx}].startDate`} type="date" placeholder="Start Date" className="w-full px-2 py-1 border rounded" />
                  <Field name={`workExperiences[${idx}].endDate`} type="date" placeholder="End Date" className="w-full px-2 py-1 border rounded" />
                </div>
                <Field as="textarea" rows="3" name={`workExperiences[${idx}].description`} placeholder="Description" className="w-full px-2 py-1 border rounded" />
                <RemoveBtn onClick={() => remove(idx)} />
              </div>
            ))}
            <AddBtn label="Experience" onClick={() => push({ jobTitle: '', companyName: '', location: '', startDate: '', endDate: '', isCurrentJob: false, description: '' })} />
          </div>
        )}
      </FieldArray>
      )

      {/* PROJECTS */}
      {active === 'projects' && (
      <SectionHeading>Projects</SectionHeading>
      <FieldArray name="projects">
        {({ push, remove }) => (
          <div className="space-y-4">
            {values.projects && values.projects.map((proj, idx) => (
              <div key={idx} className="border rounded p-4 space-y-2">
                <Field name={`projects[${idx}].name`} placeholder="Project Name" className="w-full px-2 py-1 border rounded" />
                <Field name={`projects[${idx}].techStack`} placeholder="Tech Stack" className="w-full px-2 py-1 border rounded" />
                <Field name={`projects[${idx}].date`} type="date" placeholder="Date" className="w-full px-2 py-1 border rounded" />
                {/* Achievements as textarea newline-separated */}
                <Field as="textarea" rows="3" name={`projects[${idx}].achievementsText`} placeholder="Achievements (one per line)" className="w-full px-2 py-1 border rounded" />
                <RemoveBtn onClick={() => remove(idx)} />
              </div>
            ))}
            <AddBtn label="Project" onClick={() => push({ name: '', techStack: '', date: '', achievementsText: '' })} />
          </div>
        )}
      </FieldArray>
      )

      {/* SKILLS */}
      {active === 'skills' && (
      <SectionHeading>Skills</SectionHeading>
      <FieldArray name="skills">
        {({ push, remove }) => (
          <div className="space-y-4">
            {values.skills && values.skills.map((_, idx) => (
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
      )

      {/* CERTIFICATES */}
      {active === 'certificates' && (
      <SectionHeading>Certificates</SectionHeading>
      <FieldArray name="certificates">
        {({ push, remove }) => (
          <div className="space-y-4">
            {values.certificates && values.certificates.map((_, idx) => (
              <div key={idx} className="border rounded p-4 space-y-2">
                <Field name={`certificates[${idx}].name`} placeholder="Certificate Name" className="w-full px-2 py-1 border rounded" />
                <Field name={`certificates[${idx}].institution`} placeholder="Institution" className="w-full px-2 py-1 border rounded" />
                <Field name={`certificates[${idx}].date`} type="date" placeholder="Date" className="w-full px-2 py-1 border rounded" />
                <Field name={`certificates[${idx}].url`} placeholder="URL" className="w-full px-2 py-1 border rounded" />
                <RemoveBtn onClick={() => remove(idx)} />
              </div>
            ))}
            <AddBtn label="Certificate" onClick={() => push({ name: '', institution: '', date: '', url: '' })} />
          </div>
        )}
      </FieldArray>
      )
      <div className="mt-4">
        <button type="button" onClick={submitForm} className="px-4 py-2 bg-blue-600 text-white rounded shadow">Save Section</button>
      </div>
    </>
  );

