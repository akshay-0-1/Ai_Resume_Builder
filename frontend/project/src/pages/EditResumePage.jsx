import React, { useEffect, useState, useMemo } from 'react';
import { useParams } from 'react-router-dom';
import { Formik, Field } from 'formik';
import * as Yup from 'yup';
import { toast } from 'react-hot-toast';
import axiosInstance from '../api/axiosConfig';
import Button from '../components/common/Button';
import Spinner from '../components/common/Spinner';
import ResumeSectionsForm from '../components/forms/ResumeSectionsFormV2';
import ResumeDisplay from '../components/dashboard/ResumeDisplay';

const PersonalSchema = Yup.object().shape({
  name: Yup.string().required('Required'),
  email: Yup.string().email('Invalid').required('Required'),
  phone: Yup.string(),
  address: Yup.string(),
  linkedinUrl: Yup.string(),
  githubUrl: Yup.string(),
});

// Overall resume schema combines personal and section arrays (basic validation)
// ----------------------------------------------------------------------------------
// Date helpers to convert between UI (DD-MM-YYYY) and backend ISO (YYYY-MM-DD)
// ----------------------------------------------------------------------------------
const isoToUi = (iso) => {
  if (!iso) return '';
  if (typeof iso === 'string') {
    const parts = iso.split('-');
    if (parts.length === 3) {
      const [y, m, d] = parts;
      return `${d}-${m}-${y}`;
    }
    return iso; // already formatted differently
  }
  if (iso instanceof Date) {
    const d = String(iso.getDate()).padStart(2, '0');
    const m = String(iso.getMonth() + 1).padStart(2, '0');
    const y = iso.getFullYear();
    return `${d}-${m}-${y}`;
  }
  // unknown type
  return String(iso);
};


const uiToIso = (ui) => {
  if (!ui) return null;
  if (typeof ui === 'string') {
    const clean = ui.trim();
    // expect DD-MM-YYYY
    if (/^\d{2}-\d{2}-\d{4}$/.test(clean)) {
      const [d, m, y] = clean.split('-');
      return `${y}-${m}-${d}`;
    }
    // already ISO or invalid – let backend ignore
    return null;
  }
  if (ui instanceof Date) {
    const y = ui.getFullYear();
    const m = String(ui.getMonth() + 1).padStart(2, '0');
    const d = String(ui.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }
  return ui ? String(ui) : null;
};


// ------------------------------------

const ResumeSchema = Yup.object().shape({
  ...PersonalSchema.fields,
  educations: Yup.array().of(Yup.object()).nullable(),
  workExperiences: Yup.array().of(Yup.object()).nullable(),
  skills: Yup.array().of(Yup.object()).nullable(),
  projects: Yup.array().of(Yup.object()).nullable(),
  certificates: Yup.array().of(Yup.object()).nullable(),
});

const EditResumePage = () => {
  const { id } = useParams();
  const [initialValues, setInitialValues] = useState(null);
  const [saving, setSaving] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);
  const [activeSection, setActiveSection] = useState('personal');

  useEffect(() => {
    const fetchResume = async () => {
      try {
        const resp = await axiosInstance.get(`/resumes/${id}`);
        const data = resp.data.data;
        // transform dates for UI
        const educationsUi = (data.educations || []).map(e => ({ ...e, startDate: isoToUi(e.startDate), endDate: isoToUi(e.endDate) }));
        const workUi = (data.workExperiences || []).map(w => ({ ...w, startDate: isoToUi(w.startDate), endDate: isoToUi(w.endDate) }));
        const certsUi = (data.certificates || []).map(c => ({ ...c, date: isoToUi(c.date) }));
        const projectsUi = (data.projects || []).map(p => ({ ...p, date: isoToUi(p.date), achievementsText: (p.achievements || []).join('\n') }));
        setInitialValues({
          name: data.personalDetails?.name || '',
          email: data.personalDetails?.email || '',
          phone: data.personalDetails?.phone || '',
          address: data.personalDetails?.address || '',
          linkedinUrl: data.personalDetails?.linkedinUrl || '',
          githubUrl: data.personalDetails?.githubUrl || '',
          educations: educationsUi,
          workExperiences: workUi,
          skills: data.skills || [],
          certificates: certsUi,
          projects: projectsUi,
        });
      } catch (e) {
        console.error('Failed to load resume details', e);
        toast.error('Failed to load resume details');
      }
    };
    fetchResume();
  }, [id]);

  const onSubmit = async (values) => {
    setSaving(true);
    console.log('Save clicked', values);
    try {
      const payload = {
        personalDetails: {
          name: values.name,
          email: values.email,
          phone: values.phone,
          address: values.address,
          linkedinUrl: values.linkedinUrl,
          githubUrl: values.githubUrl,
        },
        educations: values.educations.map(e => ({ ...e, startDate: uiToIso(e.startDate), endDate: uiToIso(e.endDate) })),
        workExperiences: values.workExperiences.map(w => ({ ...w, startDate: uiToIso(w.startDate), endDate: uiToIso(w.endDate) })),
        skills: values.skills,
        certificates: values.certificates.map(c => ({ ...c, date: uiToIso(c.date) })),
        projects: values.projects.map(p => ({
          name: p.name,
          techStack: p.techStack,
          date: uiToIso(p.date),
          achievements: (p.achievementsText || '').split('\n').map(s => s.trim()).filter(Boolean),
        })),
      };

      const resp = await axiosInstance.put(`/resumes/${id}`, payload);
      console.log('Save success', resp);
      toast.success('Saved');
      // give backend some time to regenerate new PDF before refreshing preview
      const ts = Date.now();
      setTimeout(() => setRefreshKey(ts), 4000);
    } catch (e) {
      console.error('Save failed', e);
      toast.error('Save failed');
    } finally {
      setSaving(false);
    }
  };

  // Helper to download latest PDF
  const handleDownload = async () => {
    try {
      const resp = await axiosInstance.get(`/resumes/${id}/download`, { responseType: 'blob' });
      const mime = resp.headers['content-type'] || 'application/pdf';
      const blob = new Blob([resp.data], { type: mime });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `${initialValues?.name || 'resume'}.pdf`);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      toast.success('Download started');
    } catch (e) {
      toast.error('Download failed');
    }
  };

  const resumeObj = useMemo(() => ({ id }), [id]);

  if (!initialValues) {
    return (
      <div className="w-full h-screen flex justify-center items-center">
        <Spinner />
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6">
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 lg:h-[90vh]">
        {/* Left – form */}
        <div className="lg:col-span-5 bg-white p-6 rounded shadow h-full overflow-y-auto">
          {(!activeSection || activeSection === 'personal') && (
              <h2 className="text-xl font-semibold mb-2">Personal Details</h2>
            )}
            <select value={activeSection} onChange={e => setActiveSection(e.target.value)} className="mb-4 w-full border px-3 py-2 rounded">
              <option value="">-- Select Section --</option>
              <option value="personal">Personal Details</option>
              <option value="education">Education</option>
              <option value="experience">Experience</option>
              <option value="projects">Projects</option>
              <option value="skills">Skills</option>
              <option value="certificates">Certificates</option>
            </select>
          <Formik
            initialValues={initialValues}
            validationSchema={ResumeSchema}
            enableReinitialize
            onSubmit={onSubmit}
          >
            {({ errors, touched, handleSubmit }) => {
              const isPersonal = !activeSection || activeSection === 'personal';
              if (process.env.NODE_ENV === 'development' && Object.keys(errors).length) {
                console.log('Formik errors', errors);
              }
              return (
              <form onSubmit={handleSubmit} className="space-y-4">
                {isPersonal && (
                <>
                <div>
                  <label className="block text-sm font-medium">Name</label>
                  <Field name="name" className="w-full px-3 py-2 border rounded" />
                  {errors.name && touched.name && <div className="text-red-500 text-xs">{errors.name}</div>}
                </div>
                <div>
                  <label className="block text-sm font-medium">Email</label>
                  <Field name="email" type="email" className="w-full px-3 py-2 border rounded" />
                  {errors.email && touched.email && <div className="text-red-500 text-xs">{errors.email}</div>}
                </div>
                <div>
                  <label className="block text-sm font-medium">Phone</label>
                  <Field name="phone" className="w-full px-3 py-2 border rounded" />
                </div>
                <div>
                  <label className="block text-sm font-medium">Address</label>
                  <Field name="address" className="w-full px-3 py-2 border rounded" />
                </div>
                <div>
                  <label className="block text-sm font-medium">LinkedIn URL</label>
                  <Field name="linkedinUrl" className="w-full px-3 py-2 border rounded" />
                </div>
                <div>
                  <label className="block text-sm font-medium">GitHub URL</label>
                  <Field name="githubUrl" className="w-full px-3 py-2 border rounded" />
                </div>
                </>
                )}
                {!isPersonal && <ResumeSectionsForm active={activeSection} />}

                <div className="flex items-center space-x-3">
                  <Button type="submit" isLoading={saving} onClick={() => console.log('Save button clicked')}>Save</Button>
                  <Button type="button" variant="secondary" onClick={handleDownload}>Download</Button>
                </div>
              </form>
              );
            }}
          </Formik>
        </div>

        {/* Right – preview */}
        <div className="lg:col-span-7 h-full overflow-y-auto">
          <ResumeDisplay
            resume={resumeObj}
            showEditButton={false}
            refreshKey={refreshKey}
          />
        </div>
      </div>
    </div>
  );
};

export default EditResumePage;
