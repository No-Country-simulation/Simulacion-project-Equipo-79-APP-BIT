import { useEffect, useState } from 'react';
import { Link } from 'react-router';
import { sileo } from 'sileo';
import { listJobs, deleteJob } from '../api/jobs.js';
import PinIcon from '../components/icons/PinIcon';

const levelColors = {
  JUNIOR: 'bg-blue-100 text-blue-700',
  MID: 'bg-amber-100 text-amber-700',
  SENIOR: 'bg-emerald-100 text-emerald-700',
};

const Jobs = () => {
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [reload, setReload] = useState(0);

  useEffect(() => {
    const loadJobs = async () => {
      setLoading(true);
      try {
        const data = await listJobs();
        setJobs(Array.isArray(data) ? data : []);
      } finally {
        setLoading(false);
      }
    };

    loadJobs();
  }, [reload]);

  const handleDelete = async (id, title) => {
    if (!window.confirm(`Are you sure you want to delete "${title}"? This action cannot be undone.`)) {
      return;
    }
    try {
      await deleteJob(id);
      sileo.success({ title: 'Job offer deleted successfully!' });
      setReload(prev => prev + 1);
    } catch (error) {
      sileo.error({
        title: 'Could not delete job offer',
        description: error instanceof Error ? error.message : 'Unexpected error',
      });
    }
  };

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
      ) : jobs.length === 0 ? (
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-16 text-center">
          <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-gray-50 flex items-center justify-center">
            <svg className="w-8 h-8 text-gray-300" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="2" y="7" width="20" height="14" rx="2" ry="2" /><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16" /></svg>
          </div>
          <p className="text-gray-500 font-medium">No jobs posted yet.</p>
          <Link to="/create-job" className="text-[#006B5F] hover:underline font-semibold mt-1 inline-block">Post your first job</Link>
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
                <div className="flex items-center gap-2">
                  <Link to={`/edit-job/${job.id}`}
                    className="bg-gray-100 hover:bg-gray-200 text-gray-600 text-sm font-semibold px-4 py-2.5 rounded-lg transition-all cursor-pointer text-center whitespace-nowrap">
                    Edit
                  </Link>
                  <button onClick={() => handleDelete(job.id, job.title)}
                    className="bg-red-50 hover:bg-red-100 text-red-600 text-sm font-semibold px-4 py-2.5 rounded-lg transition-all cursor-pointer text-center whitespace-nowrap">
                    Delete
                  </button>
                  <Link to={`/job/${job.id}/candidates`}
                    className="bg-[#006B5F]/5 hover:bg-[#006B5F]/10 text-[#006B5F] border border-[#006B5F]/20 text-sm font-semibold px-5 py-2.5 rounded-lg transition-all cursor-pointer text-center whitespace-nowrap">
                    View Matches
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Jobs;
