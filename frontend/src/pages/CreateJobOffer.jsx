import { useEffect, useState } from 'react';
import { sileo } from 'sileo';
import { useNavigate, Link } from 'react-router';
import { createJob } from '../api/jobs.js';
import { listCompanies } from '../api/company.js';
import PinIcon from '../components/icons/PinIcon';
import ChevronIcon from '../components/icons/ChevronIcon';

const CreateJobOffer = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    title: '',
    experienceLevel: 'MID',
    region: '',
    requiredSkills: '',
    companyId: '',
    description: '',
    diversityFocusEnabled: false,
    targetDiversityPercentage: '',
    modality: 'Remoto',
    salaryRange: '',
    contractType: 'Término indefinido',
    softSkills: '',
    experienceYears: '',
    education: '',
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [companies, setCompanies] = useState([]);
  const [companiesLoading, setCompaniesLoading] = useState(true);
  const [companiesError, setCompaniesError] = useState('');

  useEffect(() => {
    let ignore = false;
    const loadCompanies = async () => {
      try {
        setCompaniesLoading(true);
        setCompaniesError('');
        const data = await listCompanies();
        if (ignore) return;
        const list = Array.isArray(data) ? data : [];
        setCompanies(list);
        if (list.length > 0) {
          setFormData(prev => ({ ...prev, companyId: String(list[0].id) }));
        }
      } catch (err) {
        if (!ignore) setCompaniesError(err instanceof Error ? err.message : 'No se pudieron cargar las empresas.');
      } finally {
        if (!ignore) setCompaniesLoading(false);
      }
    };
    loadCompanies();
    return () => { ignore = true; };
  }, []);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
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
      diversityFocusEnabled: formData.diversityFocusEnabled,
      targetDiversityPercentage:
        formData.diversityFocusEnabled && formData.targetDiversityPercentage !== ''
          ? Number(formData.targetDiversityPercentage)
          : null,
      modality: formData.modality,
      salaryRange: formData.salaryRange,
      contractType: formData.contractType,
      softSkills: formData.softSkills
        .split(',')
        .map(skill => skill.trim())
        .filter(Boolean),
      experienceYears:
        formData.experienceYears !== '' ? Number(formData.experienceYears) : null,
      education: formData.education,
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
              <label className={labelClass}>Company</label>
              {companiesLoading ? (
                <div className={`${inputClass} animate-pulse bg-gray-100 text-gray-400`}>Loading companies...</div>
              ) : companiesError ? (
                <p className="text-xs text-red-500 mt-1">{companiesError}</p>
              ) : companies.length === 0 ? (
                <p className="text-xs text-gray-500 mt-1">
                  No companies registered yet.{' '}
                  <Link to="/register-company" className="text-[#006B5F] hover:underline font-semibold">
                    Register one first
                  </Link>
                </p>
              ) : (
                <div className="relative">
                  <select
                    name="companyId"
                    value={formData.companyId}
                    onChange={handleChange}
                    className={`${inputClass} appearance-none pr-9 cursor-pointer`}
                    required
                  >
                    {companies.map(company => (
                      <option key={company.id} value={company.id}>{company.name}</option>
                    ))}
                  </select>
                  <span className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-gray-400">
                    <ChevronIcon />
                  </span>
                </div>
              )}
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
              placeholder="Java, Spring Boot, Micro-services, PostgreSQL, Docker"
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

          {/* ── Row 5: Modality + Contract Type ── */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="flex flex-col">
              <label className={labelClass}>Modality</label>
              <div className="relative">
                <select
                  name="modality"
                  value={formData.modality}
                  onChange={handleChange}
                  className={`${inputClass} appearance-none pr-9 cursor-pointer`}
                >
                  <option value="Remoto">Remote</option>
                  <option value="Híbrido">Hybrid</option>
                  <option value="Presencial">On-site</option>
                </select>
                <span className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-gray-400">
                  <ChevronIcon />
                </span>
              </div>
            </div>

            <div className="flex flex-col">
              <label className={labelClass}>Contract Type</label>
              <div className="relative">
                <select
                  name="contractType"
                  value={formData.contractType}
                  onChange={handleChange}
                  className={`${inputClass} appearance-none pr-9 cursor-pointer`}
                >
                  <option value="Término indefinido">Full-time</option>
                  <option value="Término fijo">Fixed-term</option>
                  <option value="Prestación de servicios">Contract</option>
                  <option value="Freelance">Freelance</option>
                </select>
                <span className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-gray-400">
                  <ChevronIcon />
                </span>
              </div>
            </div>
          </div>

          {/* ── Row 6: Salary + Experience Years ── */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="flex flex-col">
              <label className={labelClass}>Salary Range</label>
              <input
                type="text"
                name="salaryRange"
                value={formData.salaryRange}
                onChange={handleChange}
                placeholder="3,000 - 5,000 USD"
                className={inputClass}
              />
            </div>

            <div className="flex flex-col">
              <label className={labelClass}>Experience Years</label>
              <input
                type="number"
                name="experienceYears"
                value={formData.experienceYears}
                onChange={handleChange}
                placeholder="3"
                min="0"
                className={inputClass}
              />
            </div>
          </div>

          {/* ── Row 7: Education + Soft Skills ── */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="flex flex-col">
              <label className={labelClass}>Education</label>
              <input
                type="text"
                name="education"
                value={formData.education}
                onChange={handleChange}
                placeholder="Computer Science or related fields"
                className={inputClass}
              />
            </div>

            <div className="flex flex-col">
              <label className={labelClass}>Soft Skills</label>
              <input
                type="text"
                name="softSkills"
                value={formData.softSkills}
                onChange={handleChange}
                placeholder="Communication, Leadership, Team Work"
                className={inputClass}
              />
            </div>
          </div>

          {/* ── Row 8: Diversity Focus ── */}
          <div className="rounded-xl border border-gray-200 p-4 bg-gray-50/60">
            <div className="flex items-center justify-between gap-4 flex-wrap">
              <div>
                <p className="text-sm font-semibold text-gray-700">Diversity Focus</p>
                <p className="text-xs text-gray-500 mt-0.5">Enable inclusive hiring target for this position.</p>
              </div>
              <label className="inline-flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  name="diversityFocusEnabled"
                  checked={formData.diversityFocusEnabled}
                  onChange={handleChange}
                  className="w-4 h-4 rounded border-gray-300 text-[#006B5F] focus:ring-[#006B5F] cursor-pointer accent-[#006B5F]"
                />
                <span className="text-sm font-medium text-gray-600">Enable</span>
              </label>
            </div>

            <div className="mt-4 max-w-xs">
              <label className={labelClass}>Target Diversity Percentage</label>
              <input
                type="number"
                name="targetDiversityPercentage"
                value={formData.targetDiversityPercentage}
                onChange={handleChange}
                placeholder="40"
                min="0"
                max="100"
                disabled={!formData.diversityFocusEnabled}
                className={`${inputClass} disabled:bg-gray-100 disabled:text-gray-400 disabled:cursor-not-allowed`}
              />
            </div>
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
              disabled={isSubmitting || companies.length === 0}
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
