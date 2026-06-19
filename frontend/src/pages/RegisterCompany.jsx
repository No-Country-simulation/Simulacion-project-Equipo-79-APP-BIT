import AddIcon from '../components/icons/AddIcon';
import companiesData from '../data/companies.json' with { type: 'json' };

const companyData = [
  {
    name: 'TechInnovate LATAM',
    industrySector: 'Software Development',
    esgGoals: 'Reducción de huella de carbono digital y fomento de empleo en zonas rurales.'
  },
];


const RegisterCompany = () => {
  return (
    <div className='px-6 py-8 bg-white rounded-xl border border-[#C6C6CD]/30 w-full max-w-200'>
      <legend className='text-[20px] text-[#45464D] font-bold tracking-wider uppercase mb-8'>Register Your Company</legend>
      <form className='bg-white'>
        <div className='mb-6'>
          <label htmlFor="companyName" className='text-[#45464D] uppercase text-[12px] mb-1 block'>Company Name</label>
          <input type="text" name="companyName" id="companyName" placeholder='e.g. Senior ESG Data Analyst' className='px-4 py-4.5 bg-[#F8F9FF] rounded-md border border-[#e5e5ea] w-full' />
        </div>
        <div className='mb-6'>
          <label htmlFor="industrySector" className='text-[#45464D] uppercase text-[12px] mb-1 block'>Industry Sector</label>
          <input type="text" name="industrySector" id="industrySector" className='px-4 py-4.5 bg-[#F8F9FF] rounded-md border border-[#e5e5ea] w-full' />
        </div>
        <div className='mb-6'>
          <label htmlFor="esGoals" className='text-[#45464D] uppercase text-[12px] mb-1 block'>ES Goals</label>
          <input type="text" name="esGoals" id="esGoals" className='px-4 py-4.5 bg-[#F8F9FF] rounded-md border border-[#e5e5ea] w-full' />
        </div>
        <button type="submit" className='bg-[#006B5F] flex justify-center items-center gap-2 w-45 py-4 rounded-md font-semi tracking-wide text-white cursor-pointer hover:bg-[#04594f] transition-colors'>Add<AddIcon /></button>
      </form>
    </div>
  )
}

export default RegisterCompany
