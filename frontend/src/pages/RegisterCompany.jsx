import { useState } from 'react';
import { sileo } from 'sileo';
import { useNavigate } from 'react-router';
import { registerCompany } from '../api/company.js';
import JobCreatedIcon from '../components/icons/JobCreatedIcon';

const RegisterCompany = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    name: '',
    industrySector: '',
    esgGoals: '',
    priorityRegions: '',
    interestGroups: '',
    nit: '',
    size: '',
    city: '',
  });
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const { name, industrySector, ...optional } = formData;

    if (!name || !industrySector) {
      sileo.error({ title: 'Please fill Company Name and Industry Sector!' });
      return;
    }

    const payload = {
      name,
      industrySector,
      ...Object.fromEntries(
        Object.entries(optional).filter(([, v]) => v !== '')
      ),
    };

    try {
      setIsSubmitting(true);
      const company = await registerCompany(payload);
      sileo.success({
        title: "Company created successfully!",
        fill: "#171717",
        icon: <JobCreatedIcon className="size-3.5" />,
        description: (
          <>
            <p className="text-neutral-300/70! font-medium">Name: <b className='text-white'>{company.name}</b></p>
            <p className="text-neutral-300/70! font-medium">Sector: <b className='text-white'>{company.industrySector ?? '—'}</b></p>
          </>
        ),
      });
      navigate('/');
    } catch (err) {
      sileo.error({ title: err instanceof Error ? err.message : 'Failed to register company' });
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
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-800">Register Your Company</h1>
        <p className="text-sm text-gray-500 mt-1">
          Set up your company profile to start posting ESG-focused job offers and tracking diversity metrics.
        </p>
      </div>

      <div className="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
        <form onSubmit={handleSubmit} className="p-8 flex flex-col gap-6">

          <div>
            <h2 className="text-sm font-bold text-gray-700 mb-4 flex items-center gap-2">
              <span className="w-1.5 h-1.5 rounded-full bg-[#006B5F]" />
              Basic Information
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="flex flex-col">
                <label className={labelClass}>Company Name *</label>
                <input type="text" name="name" value={formData.name} onChange={handleChange} placeholder="e.g. TechInnovate LATAM" className={inputClass} required />
              </div>
              <div className="flex flex-col">
                <label className={labelClass}>Industry Sector *</label>
                <input type="text" name="industrySector" value={formData.industrySector} onChange={handleChange} placeholder="e.g. Technology, Energy, Logistics" className={inputClass} required />
              </div>
              <div className="flex flex-col">
                <label className={labelClass}>NIT</label>
                <input type="text" name="nit" value={formData.nit} onChange={handleChange} placeholder="e.g. 900123456-7" className={inputClass} />
              </div>
              <div className="flex flex-col">
                <label className={labelClass}>Company Size</label>
                <input type="text" name="size" value={formData.size} onChange={handleChange} placeholder="e.g. 50-200 employees" className={inputClass} />
              </div>
              <div className="flex flex-col md:col-span-2">
                <label className={labelClass}>City</label>
                <input type="text" name="city" value={formData.city} onChange={handleChange} placeholder="e.g. Bogotá" className={inputClass} />
              </div>
            </div>
          </div>

          <div className="border-t border-gray-100 pt-6">
            <h2 className="text-sm font-bold text-gray-700 mb-4 flex items-center gap-2">
              <span className="w-1.5 h-1.5 rounded-full bg-purple-500" />
              Diversity Profile
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="flex flex-col">
                <label className={labelClass}>ESG Goals</label>
                <input type="text" name="esgGoals" value={formData.esgGoals} onChange={handleChange} placeholder="e.g. Reduce carbon footprint by 30%" className={inputClass} />
              </div>
              <div className="flex flex-col">
                <label className={labelClass}>Priority Regions</label>
                <input type="text" name="priorityRegions" value={formData.priorityRegions} onChange={handleChange} placeholder="e.g. Caribe, Pacífico, Amazonía" className={inputClass} />
              </div>
              <div className="flex flex-col">
                <label className={labelClass}>Interest Groups</label>
                <input type="text" name="interestGroups" value={formData.interestGroups} onChange={handleChange} placeholder="e.g. Women, Youth, Rural population" className={inputClass} />
              </div>
            </div>
          </div>

          <div className="border-t border-gray-100 pt-6 flex justify-end gap-3">
            <button type="button" onClick={() => navigate('/')} className="px-6 py-2.5 text-sm font-semibold text-gray-600 hover:bg-gray-50 rounded-lg transition-colors cursor-pointer">
              Cancel
            </button>
            <button type="submit" disabled={isSubmitting} className="bg-[#006B5F] hover:bg-[#005a50] active:scale-95 disabled:cursor-not-allowed disabled:opacity-70 text-white text-sm font-semibold px-8 py-2.5 rounded-lg transition-all shadow-sm cursor-pointer">
              {isSubmitting ? 'Registering...' : 'Register Company'}
            </button>
          </div>

        </form>
      </div>
    </div>
  );
};

export default RegisterCompany;
