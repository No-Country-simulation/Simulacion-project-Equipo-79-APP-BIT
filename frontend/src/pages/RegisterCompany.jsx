import { sileo } from 'sileo';
import { useState, useId } from 'react';
import { registerCompany } from '../api/company.js';
import AddIcon from '../components/icons/AddIcon';
import JobCreatedIcon from '../components/icons/JobCreatedIcon';

const RegisterCompany = () => {
  const companyNameId = useId();
  const industrySectorId = useId();
  const esGoalsId = useId();
  const diversityGoalId = useId();
  const priorityRegionsId = useId();
  const interestGroupsId = useId();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    const name = formData.get(companyNameId);
    const industrySector = formData.get(industrySectorId);
    const esgGoals = formData.get(esGoalsId);
    const diversityGoal = formData.get(diversityGoalId);
    const priorityRegions = formData.get(priorityRegionsId);
    const interestGroups = formData.get(interestGroupsId);

    if (!name || !industrySector) {
      sileo.error({ title: 'Please fill Company Name and Industry Sector!' });
      return;
    }

    try {
      setIsSubmitting(true);
      const company = await registerCompany({
        name,
        industrySector,
        esgGoals: esgGoals || undefined,
        diversityGoal: diversityGoal || undefined,
        priorityRegions: priorityRegions || undefined,
        interestGroups: interestGroups || undefined,
      });

      sileo.success({
        title: "Company created successfully!",
        fill: "#171717",
        icon: <JobCreatedIcon className="size-3.5" />,
        description: (
          <>
            <p className="text-neutral-300/70! font-medium">Name: <b className='text-white'>{company.name}</b></p>
            <p className="text-neutral-300/70! font-medium">Sector: <b className='text-white'>{company.industrySector ?? '—'}</b></p>
            {company.diversityGoal && <p className="text-neutral-300/70! font-medium">Diversity Goal: <b className='text-white'>{company.diversityGoal}</b></p>}
          </>
        ),
      });
      e.target.reset();
    } catch (err) {
      sileo.error({ title: err instanceof Error ? err.message : 'Failed to register company' });
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className='px-6 py-8 bg-white rounded-xl border border-[#C6C6CD]/30 w-full max-w-200'>
      <legend className='text-[20px] text-[#45464D] font-bold tracking-wider uppercase mb-8'>Register Your Company</legend>
      <form className='bg-white' onSubmit={handleSubmit}>
        <div className='mb-6'>
          <label htmlFor={companyNameId} className='text-[#45464D] uppercase text-[12px] mb-1 block'>Company Name *</label>
          <input required type="text" name={companyNameId} id={companyNameId} placeholder='e.g. TechInnovate LATAM' className='px-4 py-4.5 bg-[#F8F9FF] rounded-md border border-[#e5e5ea] w-full' />
        </div>
        <div className='mb-6'>
          <label htmlFor={industrySectorId} className='text-[#45464D] uppercase text-[12px] mb-1 block'>Industry Sector *</label>
          <input required type="text" name={industrySectorId} id={industrySectorId} placeholder='e.g. Technology, Energy, Logistics' className='px-4 py-4.5 bg-[#F8F9FF] rounded-md border border-[#e5e5ea] w-full' />
        </div>
        <div className='mb-6'>
          <label htmlFor={esGoalsId} className='text-[#45464D] uppercase text-[12px] mb-1 block'>ESG Goals</label>
          <input type="text" name={esGoalsId} id={esGoalsId} placeholder='e.g. Reduce carbon footprint by 30%' className='px-4 py-4.5 bg-[#F8F9FF] rounded-md border border-[#e5e5ea] w-full' />
        </div>
        <div className='mb-6'>
          <label htmlFor={diversityGoalId} className='text-[#45464D] uppercase text-[12px] mb-1 block'>Diversity Goal</label>
          <input type="text" name={diversityGoalId} id={diversityGoalId} placeholder='e.g. 30% of shortlist with diverse talent' className='px-4 py-4.5 bg-[#F8F9FF] rounded-md border border-[#e5e5ea] w-full' />
        </div>
        <div className='mb-6'>
          <label htmlFor={priorityRegionsId} className='text-[#45464D] uppercase text-[12px] mb-1 block'>Priority Regions</label>
          <input type="text" name={priorityRegionsId} id={priorityRegionsId} placeholder='e.g. Caribe, Pacífico, Amazonía' className='px-4 py-4.5 bg-[#F8F9FF] rounded-md border border-[#e5e5ea] w-full' />
        </div>
        <div className='mb-6'>
          <label htmlFor={interestGroupsId} className='text-[#45464D] uppercase text-[12px] mb-1 block'>Interest Groups</label>
          <input type="text" name={interestGroupsId} id={interestGroupsId} placeholder='e.g. Women, Youth, Rural population' className='px-4 py-4.5 bg-[#F8F9FF] rounded-md border border-[#e5e5ea] w-full' />
        </div>
        <button type="submit" disabled={isSubmitting} className='bg-[#006B5F] flex justify-center items-center gap-2 w-45 py-4 rounded-md font-semi tracking-wide text-white cursor-pointer hover:bg-[#04594f] transition-colors disabled:opacity-50 disabled:cursor-not-allowed'>
          {isSubmitting ? 'Registering...' : 'Add'}{!isSubmitting && <AddIcon />}
        </button>
      </form>
    </div>
  )
}

export default RegisterCompany
