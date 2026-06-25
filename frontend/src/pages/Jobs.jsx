import { useEffect, useState } from 'react';
import { Link } from 'react-router';
import { listJobs } from '../api/jobs.js';
import PinIcon from '../components/icons/PinIcon';

const levelColors = {
  JUNIOR: 'bg-blue-100 text-blue-700',
  MID: 'bg-amber-100 text-amber-700',
  SENIOR: 'bg-emerald-100 text-emerald-700',
};

const Jobs = () => {
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadJobs = async () => {
      try {
        const data = await listJobs();
        setJobs(Array.isArray(data) ? data : []);
      } finally {
        setLoading(false);
      }
    };

    loadJobs();
  }, []);

  return (
    <div className="p-8 max-w-5xl mx-auto">
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Job Offers</h1>
          <p className="text-sm text-gray-500 mt-1">Manage your job postings and view matched candidates.</p>
        </div>
        <Link to="/create-job"
          className="bg-[#006B5F] hover:bg-[#005a50] active:scale-95 text-white text-sm font-semibold px-5 py-2.5 rounded-lg transition-all shadow-sm cursor-pointer">
          + New Job
        </Link>
      </div>

      {loading ? (
        <div className="rounded-2xl border border-gray-200 bg-white p-8 text-sm text-gray-500 shadow-sm">
          Loading jobs...
        </div>
      ) : (
        <div className="flex flex-col gap-4">
          {jobs.map(job => (
            <div key={job.id} className="bg-white rounded-2xl border border-gray-200 shadow-sm p-6 hover:shadow-md transition-shadow">
              <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div className="flex-1">
                  <div className="flex items-center gap-3 flex-wrap">
                    <h2 className="text-lg font-bold text-gray-800">{job.title}</h2>
                    <span className={`text-[10px] font-bold uppercase tracking-wider px-2 py-1 rounded-full ${levelColors[job.experienceLevel]}`}>
                      {job.experienceLevel}
                    </span>
                  </div>
                  <p className="text-sm text-gray-500 mt-1.5 flex items-center gap-1">
                    <PinIcon /> {job.region} · {job.department}
                  </p>
                  <p className="text-xs text-gray-400 mt-1">{job.description}</p>
                </div>
                <Link to={`/job/${job.id}/candidates`}
                  className="bg-[#006B5F]/5 hover:bg-[#006B5F]/10 text-[#006B5F] border border-[#006B5F]/20 text-sm font-semibold px-5 py-2.5 rounded-lg transition-all cursor-pointer text-center whitespace-nowrap">
                  View Matches
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Jobs;
