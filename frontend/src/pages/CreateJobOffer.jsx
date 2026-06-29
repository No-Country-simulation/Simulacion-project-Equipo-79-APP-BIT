import { useState } from 'react';
import { sileo } from 'sileo';
import { useNavigate } from 'react-router';
import { createJob } from '../api/jobs.js';
import PinIcon from '../components/icons/PinIcon';
import ChevronIcon from '../components/icons/ChevronIcon';

const CreateJobOffer = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    title: '',
    experienceLevel: 'MID',
    region: '',
    requiredSkills: '',
    companyId: 1,
    description: '',
  });
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const payload = {
      title: formData.title,
      description: formData.description,
      region: formData.region,
      requiredSkills: formData.requiredSkills
        .split(',')
        .map(skill => skill.trim())
        .filter(Boolean),
      experienceLevel: formData.experienceLevel,
      companyId: Number(formData.companyId),
    };

    try {
      setIsSubmitting(true);
      await createJob(payload);
      sileo.success({ title: 'Job offer created successfully!' });
      navigate('/job');
    } catch (error) {
      sileo.error({
        title: 'Could not create job offer',
        description: error instanceof Error ? error.message : 'Unexpected error',
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const labelClass =
    'text-[11px] font-bold uppercase tracking-wider text-gray-500 mb-1.5';
  const inputClass =
    'w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm text-gray-700 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-[#2B6952]/20 focus:border-[#2B6952] bg-white transition-all';

  return (
    <div className="p-8 max-w-4xl mx-auto">
      {/* Header Area */}
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-800">Create Job Offer</h1>
        <p className="text-sm text-gray-500 mt-1">Fill in the details to post a new position on the ESG Matching Portal.</p>
      </div>

      {/* Card */}
      <div className="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
        <form onSubmit={handleSubmit} className="p-8 flex flex-col gap-6">

          {/* ── Row 1: Job Title + Experience Level ── */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">

            {/* Job Title */}
            <div className="flex flex-col">
              <label className={labelClass}>Job Title</label>
              <input
                type="text"
                name="title"
                value={formData.title}
                onChange={handleChange}
                placeholder="e.g. Senior ESG Data Analyst"
                className={inputClass}
                required
              />
            </div>

            {/* Experience Level */}
            <div className="flex flex-col">
              <label className={labelClass}>Experience Level</label>
              <div className="relative">
                <select
                  name="experienceLevel"
                  value={formData.experienceLevel}
                  onChange={handleChange}
                  className={`${inputClass} appearance-none pr-9 cursor-pointer`}
                >
                  <option value="JUNIOR">Junior</option>
                  <option value="MID">Mid-Level</option>
                  <option value="SENIOR">Senior</option>
                </select>
                <span className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-gray-400">
                  <ChevronIcon />
                </span>
              </div>
            </div>
          </div>

          {/* ── Row 2: Region + Company ── */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">

            {/* Region / Location */}
            <div className="flex flex-col">
              <label className={labelClass}>Region / Location</label>
              <div className="relative">
                <span className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">
                  <PinIcon />
                </span>
                <input
                  type="text"
                  name="region"
                  value={formData.region}
                  onChange={handleChange}
                  placeholder="Remote / New York / London"
                  className={`${inputClass} pl-9`}
                  required
                />
              </div>
            </div>

            {/* Company */}
            <div className="flex flex-col">
              <label className={labelClass}>Company ID</label>
              <input
                type="number"
                name="companyId"
                value={formData.companyId}
                onChange={handleChange}
                min="1"
                placeholder="1"
                className={inputClass}
                required
              />
            </div>
          </div>

          {/* ── Row 3: Required Skills ── */}
          <div className="flex flex-col">
            <label className={labelClass}>Required Skills</label>
            <input
              type="text"
              name="requiredSkills"
              value={formData.requiredSkills}
              onChange={handleChange}
              placeholder="Java, Spring Boot, Microservicios, PostgreSQL, Docker"
              className={inputClass}
              required
            />
          </div>

          {/* ── Row 4: Description Summary ── */}
          <div className="flex flex-col">
            <label className={labelClass}>Description Summary</label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              placeholder="Briefly describe the role's impact on company ESG goals..."
              rows={6}
              className={`${inputClass} resize-none`}
              required
            />
          </div>

          {/* ── Action Buttons ── */}
          <div className="border-t border-gray-100 pt-6 flex justify-end gap-3">
            <button
              type="button"
              className="px-6 py-2.5 text-sm font-semibold text-gray-600 hover:bg-gray-50 rounded-lg transition-colors cursor-pointer"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="bg-[#006B5F] hover:bg-[#005a50] active:scale-95 disabled:cursor-not-allowed disabled:opacity-70 text-white text-sm font-semibold px-8 py-2.5 rounded-lg transition-all shadow-sm cursor-pointer"
            >
              {isSubmitting ? 'Posting...' : 'Post Job Offer'}
            </button>
          </div>

        </form>
      </div>
    </div>
  );
};

export default CreateJobOffer;
