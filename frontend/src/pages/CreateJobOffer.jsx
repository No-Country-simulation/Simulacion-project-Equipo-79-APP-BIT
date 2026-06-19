import { useState } from 'react';

const PinIcon = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
    <circle cx="12" cy="10" r="3"/>
  </svg>
);

const ChevronIcon = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="6 9 12 15 18 9"/>
  </svg>
);

const CreateJobOffer = () => {
  const [formData, setFormData] = useState({
    title: '',
    experienceLevel: 'MID',
    jobType: 'FULL_TIME',
    salaryRange: '',
    region: '',
    department: '',
    description: '',
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    console.log('Form submitted:', formData);
    // TODO: Connect with backend API POST /jobs
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

          {/* ── Row 2: Job Type + Salary Range ── */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">

            {/* Job Type */}
            <div className="flex flex-col">
              <label className={labelClass}>Job Type</label>
              <div className="relative">
                <select
                  name="jobType"
                  value={formData.jobType}
                  onChange={handleChange}
                  className={`${inputClass} appearance-none pr-9 cursor-pointer`}
                >
                  <option value="FULL_TIME">Full-time</option>
                  <option value="PART_TIME">Part-time</option>
                  <option value="CONTRACT">Contract</option>
                  <option value="INTERN">Internship</option>
                </select>
                <span className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-gray-400">
                  <ChevronIcon />
                </span>
              </div>
            </div>

            {/* Salary Range */}
            <div className="flex flex-col">
              <label className={labelClass}>Salary Range (Optional)</label>
              <input
                type="text"
                name="salaryRange"
                value={formData.salaryRange}
                onChange={handleChange}
                placeholder="e.g. $80k - $120k"
                className={inputClass}
              />
            </div>
          </div>

          {/* ── Row 3: Region / Location + Department ── */}
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

            {/* Department */}
            <div className="flex flex-col">
              <label className={labelClass}>Department</label>
              <input
                type="text"
                name="department"
                value={formData.department}
                onChange={handleChange}
                placeholder="Strategy & Growth"
                className={inputClass}
                required
              />
            </div>
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
              className="bg-[#006B5F] hover:bg-[#005a50] active:scale-95 text-white text-sm font-semibold px-8 py-2.5 rounded-lg transition-all shadow-sm cursor-pointer"
            >
              Post Job Offer
            </button>
          </div>

        </form>
      </div>
    </div>
  );
};

export default CreateJobOffer;