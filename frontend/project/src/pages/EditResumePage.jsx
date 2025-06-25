import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Formik, Form, Field } from 'formik';
import * as Yup from 'yup';
import { toast } from 'react-hot-toast';
import axiosInstance from '../api/axiosConfig';
import Button from '../components/common/Button';
import Spinner from '../components/common/Spinner';
import ResumeDisplay from '../components/dashboard/ResumeDisplay';

const PersonalSchema = Yup.object().shape({
  name: Yup.string().required('Required'),
  email: Yup.string().email('Invalid').required('Required'),
  phone: Yup.string().required('Required'),
  address: Yup.string(),
  linkedinUrl: Yup.string(),
  githubUrl: Yup.string(),
  portfolioUrl: Yup.string(),
  summary: Yup.string(),
});

const EditResumePage = () => {
  const { id } = useParams();
  const [initialValues, setInitialValues] = useState(null);
  const [saving, setSaving] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);

  useEffect(() => {
    const fetchResume = async () => {
      try {
        const resp = await axiosInstance.get(`/resumes/${id}`);
        const data = resp.data.data;
        setInitialValues({
          name: data.personalDetails?.name || '',
          email: data.personalDetails?.email || '',
          phone: data.personalDetails?.phone || '',
          address: data.personalDetails?.address || '',
          linkedinUrl: data.personalDetails?.linkedinUrl || '',
          githubUrl: data.personalDetails?.githubUrl || '',
          portfolioUrl: data.personalDetails?.portfolioUrl || '',
          summary: data.personalDetails?.summary || '',
        });
      } catch (e) {
        toast.error('Failed to load resume details');
      }
    };
    fetchResume();
  }, [id]);

  const onSubmit = async (values) => {
    setSaving(true);
    try {
      await axiosInstance.put(`/resumes/${id}`, {
        personalDetails: values,
      });
      toast.success('Saved');
      setRefreshKey(Date.now());
    } catch (e) {
      toast.error('Save failed');
    } finally {
      setSaving(false);
    }
  };

  if (!initialValues) {
    return (
      <div className="w-full h-screen flex justify-center items-center">
        <Spinner />
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6">
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left – form */}
        <div className="lg:col-span-5 bg-white p-6 rounded shadow">
          <h2 className="text-xl font-semibold mb-4">Personal Details</h2>
          <Formik
            initialValues={initialValues}
            validationSchema={PersonalSchema}
            enableReinitialize
            onSubmit={onSubmit}
          >
            {({ errors, touched }) => (
              <Form className="space-y-4">
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
                <div>
                  <label className="block text-sm font-medium">Portfolio URL</label>
                  <Field name="portfolioUrl" className="w-full px-3 py-2 border rounded" />
                </div>
                <div>
                  <label className="block text-sm font-medium">Summary</label>
                  <Field as="textarea" name="summary" className="w-full px-3 py-2 border rounded" rows={3} />
                </div>
                <Button type="submit" isLoading={saving}>Save</Button>
              </Form>
            )}
          </Formik>
        </div>

        {/* Right – preview */}
        <div className="lg:col-span-7">
          <ResumeDisplay
            resume={{ id }}
            showEditButton={false}
            refreshKey={refreshKey}
          />
        </div>
      </div>
    </div>
  );
};

export default EditResumePage;
