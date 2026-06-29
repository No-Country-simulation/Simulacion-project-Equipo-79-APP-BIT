import { sileo } from 'sileo';
import AddIcon from '../components/icons/AddIcon';
import { useId } from 'react';
import { useNavigate } from 'react-router';
import JobCreatedIcon from '../components/icons/JobCreatedIcon';
import { createCompany } from '../api/company.js';

const RegisterCompany = () => {
  const navigate = useNavigate();
  const companyNameId = useId();
  const industrySectorId = useId();
  const esGoalsId = useId();

  const handleSubmit = async (e) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    const companyName = formData.get(companyNameId);
    const industrySector = formData.get(industrySectorId);
    const esGoals = formData.get(esGoalsId);

    if (!companyName || !industrySector || !esGoals) {
      sileo.error({ title: 'Please fill all fields!' });
      return;
    }

    const newCompany = {
      name: companyName,
      industrySector,
      esgGoals: esGoals,
    }

    try {
      await createCompany(newCompany);
      sileo.success({
        title: 'Company created successfully!',
        fill: '#171717',
        icon: <JobCreatedIcon className="size-3.5" />,
        description: (
          <>
            <p className="text-neutral-300/70! font-medium">Name: <b className='text-white'>{companyName}</b></p>
            <p className="text-neutral-300/70! font-medium">Industry sector: <b className='text-white'>{industrySector}</b></p>
            <p className="text-neutral-300/70! font-medium">ES goals: <b className='text-white'>{esGoals}</b></p>
          </>
        ),
      });
      navigate('/');
    } catch (error) {
      sileo.error({
        title: 'Could not create company',
        description: error instanceof Error ? error.message : 'Unexpected error',
      });
    }
  }

  return (
    <div className='px-6 py-8 bg-white rounded-xl border border-[#C6C6CD]/30 w-full max-w-200'>
      <legend className='text-[20px] text-[#45464D] font-bold tracking-wider uppercase mb-8'>Register Your Company</legend>
      <form className='bg-white' onSubmit={handleSubmit}>
        <div className='mb-6'>
          <label htmlFor={companyNameId} className='text-[#45464D] uppercase text-[12px] mb-1 block'>Company Name</label>
          <input type="text" name={companyNameId} id={companyNameId} placeholder='e.g. Senior ESG Data Analyst' className='px-4 py-4.5 bg-[#F8F9FF] rounded-md border border-[#e5e5ea] w-full' />
        </div>
        <div className='mb-6'>
          <label htmlFor={industrySectorId} className='text-[#45464D] uppercase text-[12px] mb-1 block'>Industry Sector</label>
          <input type="text" name={industrySectorId} id={industrySectorId} placeholder='e.g. Energy, , technology, logistics' className='px-4 py-4.5 bg-[#F8F9FF] rounded-md border border-[#e5e5ea] w-full' />
        </div>
        <div className='mb-6'>
          <label htmlFor={esGoalsId} className='text-[#45464D] uppercase text-[12px] mb-1 block'>ES Goals</label>
          <input type="text" name={esGoalsId} id={esGoalsId} placeholder='e.g. Zero waste' className='px-4 py-4.5 bg-[#F8F9FF] rounded-md border border-[#e5e5ea] w-full' />
        </div>
        <button type="submit" className='bg-[#006B5F] flex justify-center items-center gap-2 w-45 py-4 rounded-md font-semi tracking-wide text-white cursor-pointer hover:bg-[#04594f] transition-colors'>Add<AddIcon /></button>
      </form>
    </div>
  )
}

export default RegisterCompany
