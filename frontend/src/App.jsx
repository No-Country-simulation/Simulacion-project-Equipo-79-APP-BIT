import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router';
import { listJobs } from './api/jobs.js';
import { listCandidates } from './api/candidates.js';
import { getRegionInsights } from './api/insights.js';
import { getEsgMetrics } from './api/dashboard.js';
import CandidateMap from './components/CandidateMap';
import 'leaflet/dist/leaflet.css';

const Skeleton = ({ className = '' }) => (
  <div className={`animate-pulse bg-gray-200 rounded-xl ${className}`} />
);

const badgeColorPalette = [
  { bg: 'bg-emerald-100', text: 'text-emerald-700', bar: 'bg-emerald-400' },
  { bg: 'bg-blue-100', text: 'text-blue-700', bar: 'bg-blue-400' },
  { bg: 'bg-purple-100', text: 'text-purple-700', bar: 'bg-purple-400' },
  { bg: 'bg-amber-100', text: 'text-amber-700', bar: 'bg-amber-400' },
  { bg: 'bg-rose-100', text: 'text-rose-700', bar: 'bg-rose-400' },
  { bg: 'bg-cyan-100', text: 'text-cyan-700', bar: 'bg-cyan-400' },
  { bg: 'bg-pink-100', text: 'text-pink-700', bar: 'bg-pink-400' },
  { bg: 'bg-indigo-100', text: 'text-indigo-700', bar: 'bg-indigo-400' },
];

const skillColors = [
  'bg-emerald-100 text-emerald-700',
  'bg-blue-100 text-blue-700',
  'bg-purple-100 text-purple-700',
  'bg-amber-100 text-amber-700',
  'bg-rose-100 text-rose-700',
  'bg-cyan-100 text-cyan-700',
];

const topSkills = (candidates) => {
  const freq = {};
  candidates.forEach(c => (c.skills ?? []).forEach(s => { freq[s] = (freq[s] || 0) + 1; }));
  return Object.entries(freq).sort((a, b) => b[1] - a[1]).slice(0, 6);
};

const esgStatusConfig = {
  CUMPLIDA: { bg: 'bg-emerald-100', text: 'text-emerald-700', dot: 'bg-emerald-500', label: 'Meta cumplida' },
  EN_PROGRESO: { bg: 'bg-amber-100', text: 'text-amber-700', dot: 'bg-amber-500', label: 'En progreso' },
  NO_ALCANZADA: { bg: 'bg-red-100', text: 'text-red-700', dot: 'bg-red-500', label: 'No alcanzada' },
};

function App() {
  const [jobs, setJobs] = useState([]);
  const [candidates, setCandidates] = useState([]);
  const [insights, setInsights] = useState([]);
  const [esg, setEsg] = useState(null);
  const [loading, setLoading] = useState(true);
  const [fetchError, setFetchError] = useState('');

  useEffect(() => {
    let ignore = false;
    const load = async () => {
      try {
        setLoading(true);
        setFetchError('');

        const [jobsData, candidatesData, insightsData, esgData] = await Promise.all([
          listJobs(),
          listCandidates(),
          getRegionInsights(),
          getEsgMetrics(),
        ]);

        if (!ignore) {
          setJobs(Array.isArray(jobsData) ? jobsData : []);
          setCandidates(Array.isArray(candidatesData) ? candidatesData : []);
          setInsights(Array.isArray(insightsData) ? insightsData : []);
          setEsg(esgData);
        }
      } catch (err) {
        if (!ignore) setFetchError(err instanceof Error ? err.message : 'Failed to load data');
      } finally {
        if (!ignore) setLoading(false);
      }
    };
    load();
    return () => { ignore = true; };
  }, []);

  const stats = useMemo(() => {
    const levels = {};
    candidates.forEach(c => {
      levels[c.experienceLevel] = (levels[c.experienceLevel] || 0) + 1;
    });
    const skills = topSkills(candidates);
    const maxSkill = skills.length > 0 ? skills[0][1] : 1;
    return { levels, skills, maxSkill };
  }, [candidates]);

  if (loading) {
    return (
      <div className="p-6 max-w-7xl mx-auto space-y-6">
        <Skeleton className="h-40 w-full" />
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <Skeleton className="h-24" /><Skeleton className="h-24" /><Skeleton className="h-24" /><Skeleton className="h-24" />
        </div>
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <Skeleton className="h-80" /><Skeleton className="h-80" />
        </div>
      </div>
    );
  }

  if (fetchError) {
    return (
      <div className="p-6 max-w-7xl mx-auto">
        <div className="bg-white rounded-3xl border border-red-200 shadow-sm p-16 text-center">
          <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-red-50 flex items-center justify-center">
            <svg className="w-8 h-8 text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2"><path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
          </div>
          <p className="text-gray-600 font-medium">Failed to load dashboard data</p>
          <p className="text-gray-400 text-sm mt-1">{fetchError}</p>
          <button onClick={() => window.location.reload()} className="mt-6 bg-[#006B5F] hover:bg-[#005a50] text-white text-sm font-semibold px-6 py-2.5 rounded-xl transition-all cursor-pointer">
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">

      {/* Hero */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-[#006B5F] via-[#008575] to-[#00A88F] p-8 text-white">
        <div className="absolute top-0 right-0 w-72 h-72 bg-white/5 rounded-full -translate-y-1/2 translate-x-1/3" />
        <div className="absolute bottom-0 left-1/4 w-56 h-56 bg-white/5 rounded-full translate-y-1/3" />
        <div className="relative z-10 flex xl:items-start items-center justify-between">
          <div>
            <div className="flex items-center gap-2 text-white/70 text-sm font-medium mb-2">
              <svg className="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="2" y="3" width="20" height="14" rx="2" ry="2" /><path d="M8 21h8M12 17v4" /></svg>
              BiT Admin Dashboard
            </div>
            <h1 className="text-3xl font-bold tracking-tight">Welcome back</h1>
            <p className="text-white/80 text-sm mt-2 max-w-lg text-balance">
              Overview of your ESG talent matching platform. Monitor candidates, job offers, and regional insights.
            </p>
          </div>
          <div className="hidden md:flex flex-wrap items-center gap-3">
            <Link to="/create-job" className="bg-white text-[#006B5F] hover:bg-white/90 text-sm font-semibold px-5 py-2.5 rounded-xl transition-all">
              Post New Job
            </Link>
            <Link to="/insights" className="bg-white/10 text-white hover:bg-white/20 text-sm font-semibold px-5 py-2.5 rounded-xl transition-all border border-white/20">
              View Insights
            </Link>
          </div>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5 hover:shadow-md transition-shadow">
          <div className="flex items-center gap-2">
            <div className="w-10 h-10 rounded-xl bg-emerald-50 flex items-center justify-center flex-shrink-0">
              <svg className="w-5 h-5 text-emerald-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" /><circle cx="9" cy="7" r="4" /><path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" /></svg>
            </div>
            <div>
              <p className="text-[10px] font-bold uppercase tracking-wider text-gray-400">Candidates</p>
              <p className="text-2xl font-bold text-gray-800">{esg?.totalCandidates ?? candidates.length}</p>
            </div>
          </div>
        </div>
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5 hover:shadow-md transition-shadow">
          <div className="flex items-center gap-2">
            <div className="w-10 h-10 rounded-xl bg-blue-50 flex items-center justify-center flex-shrink-0">
              <svg className="w-5 h-5 text-blue-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="2" y="7" width="20" height="14" rx="2" ry="2" /><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16" /></svg>
            </div>
            <div>
              <p className="text-[10px] font-bold uppercase tracking-wider text-gray-400">Jobs</p>
              <p className="text-2xl font-bold text-gray-800">{esg?.totalJobs ?? jobs.length}</p>
            </div>
          </div>
        </div>
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5 hover:shadow-md transition-shadow">
          <div className="flex items-center gap-2">
            <div className="w-10 h-10 rounded-xl bg-purple-50 flex items-center justify-center flex-shrink-0">
              <svg className="w-5 h-5 text-purple-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M12 20h9M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z" /></svg>
            </div>
            <div>
              <p className="text-[10px] font-bold uppercase tracking-wider text-gray-400">Diversity</p>
              <p className="text-2xl font-bold text-gray-800">{esg?.diversityPercentage ?? 0}<span className="text-sm font-normal text-gray-400">%</span></p>
            </div>
          </div>
        </div>
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5 hover:shadow-md transition-shadow">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-amber-50 flex items-center justify-center flex-shrink-0">
              <svg className="w-5 h-5 text-amber-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" /><circle cx="12" cy="10" r="3" /></svg>
            </div>
            <div>
              <p className="text-[10px] font-bold uppercase tracking-wider text-gray-400">Regions</p>
              <p className="text-2xl font-bold text-gray-800">{esg?.totalRegions ?? insights.length}</p>
            </div>
          </div>
        </div>
      </div>

      {/* ESG Compliance + Diversity Breakdown row */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">

        {/* ESG Compliance */}
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
          <div className="flex items-center justify-between px-6 py-4 border-b border-gray-50">
            <div className="flex items-center gap-2">
              <div className="w-2 h-2 rounded-full bg-[#006B5F]" />
              <h2 className="text-sm font-bold text-gray-700">ESG Compliance</h2>
            </div>
          </div>
          <div className="p-6 space-y-5">
            <div className="text-center">
              <div className="relative w-28 h-28 mx-auto">
                <svg className="w-28 h-28 -rotate-90" viewBox="0 0 120 120">
                  <circle cx="60" cy="60" r="54" fill="none" stroke="#f3f4f6" strokeWidth="10" />
                  <circle cx="60" cy="60" r="54" fill="none" stroke="#006B5F" strokeWidth="10"
                    strokeDasharray={`${(esg?.esgCompliance?.current ?? 0) * 3.39} 339`}
                    strokeLinecap="round" />
                </svg>
                <div className="absolute inset-0 flex flex-col items-center justify-center">
                  <span className="text-2xl font-bold text-gray-800">{esg?.esgCompliance?.current ?? 0}%</span>
                  <span className="text-[10px] text-gray-400">diversity</span>
                </div>
              </div>
              {esg?.esgCompliance && (
                <span className={`inline-flex items-center gap-1.5 mt-3 text-[10px] font-bold px-2.5 py-1 rounded-full ${esgStatusConfig[esg.esgCompliance.status]?.bg ?? 'bg-gray-100'} ${esgStatusConfig[esg.esgCompliance.status]?.text ?? 'text-gray-700'}`}>
                  <span className={`w-1.5 h-1.5 rounded-full ${esgStatusConfig[esg.esgCompliance.status]?.dot ?? 'bg-gray-400'}`} />
                  {esgStatusConfig[esg.esgCompliance.status]?.label ?? esg.esgCompliance.status}
                </span>
              )}
            </div>
            <div className="space-y-3 pt-2">
              <div className="flex justify-between text-xs">
                <span className="text-gray-500">Total candidates</span>
                <span className="font-semibold text-gray-700">{esg?.totalCandidates ?? 0}</span>
              </div>
              <div className="flex justify-between text-xs">
                <span className="text-gray-500">With diversity badge</span>
                <span className="font-semibold text-gray-700">{esg?.totalDiversity ?? 0}</span>
              </div>
              <div className="flex flex-col sm:flex-row justify-between text-xs">
                <span className="text-gray-500">Goal</span>
                <span className="font-semibold text-balance text-gray-700">{esg?.esgCompliance?.goal ?? '—'}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Diversity Breakdown */}
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden lg:col-span-2">
          <div className="flex items-center justify-between px-6 py-4 border-b border-gray-50">
            <div className="flex items-center gap-2">
              <div className="w-2 h-2 rounded-full bg-purple-500" />
              <h2 className="text-sm font-bold text-gray-700">Diversity Breakdown</h2>
            </div>
            <span className="text-xs text-gray-400">{esg?.totalDiversity ?? 0} with badge</span>
          </div>
          <div className="p-6 space-y-4">
            {!esg?.badgeBreakdown || esg.badgeBreakdown.length === 0 ? (
              <p className="text-sm text-gray-400 text-center py-4">No diversity badges recorded.</p>
            ) : (
              esg.badgeBreakdown.map((item, idx) => {
                const colors = badgeColorPalette[idx % badgeColorPalette.length];
                return (
                  <div key={item.badge}>
                    <div className="flex items-center justify-between mb-1.5">
                      <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${colors.bg} ${colors.text}`}>
                        {item.badge}
                      </span>
                      <span className="text-xs font-semibold text-gray-600">{item.count} <span className="text-gray-400 font-normal">({item.percentage}%)</span></span>
                    </div>
                    <div className="w-full h-2 bg-gray-100 rounded-full overflow-hidden">
                      <div className={`h-full rounded-full transition-all duration-500 ${colors.bar}`} style={{ width: `${Math.min(item.percentage, 100)}%` }} />
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">

        {/* Recent Jobs */}
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
          <div className="flex items-center justify-between px-6 py-4 border-b border-gray-50">
            <div className="flex items-center gap-2">
              <div className="w-2 h-2 rounded-full bg-[#006B5F]" />
              <h2 className="text-sm font-bold text-gray-700">Recent Jobs</h2>
            </div>
            <Link to="/job" className="text-xs font-semibold text-[#006B5F] hover:underline">View all</Link>
          </div>
          <div className="divide-y divide-gray-50">
            {jobs.length === 0 ? (
              <div className="p-8 text-center text-sm text-gray-400">
                <p>No jobs posted yet.</p>
                <Link to="/create-job" className="text-[#006B5F] hover:underline font-semibold mt-1 inline-block">Post your first job</Link>
              </div>
            ) : jobs.slice(0, 5).map(job => (
              <Link key={job.id} to={`/job/${job.id}/candidates`} className="flex items-center justify-between px-6 py-4 hover:bg-gray-50 transition-colors group">
                <div className="min-w-0 flex-1">
                  <p className="text-sm font-semibold text-gray-800 group-hover:text-[#006B5F] transition-colors truncate">{job.title}</p>
                  <div className="flex items-center gap-2 mt-0.5 text-xs text-gray-400">
                    {job.region && <span>{job.region}</span>}
                    {job.region && job.experienceLevel && <span>·</span>}
                    {job.experienceLevel && <span>{job.experienceLevel.charAt(0) + job.experienceLevel.slice(1).toLowerCase()}</span>}
                  </div>
                </div>
                <div className="flex items-center gap-3 ml-4">
                  <span className="text-xs text-gray-300">{job.department || ''}</span>
                  <svg className="w-4 h-4 text-gray-300 group-hover:text-[#006B5F] transition-colors" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="9 18 15 12 9 6" /></svg>
                </div>
              </Link>
            ))}
          </div>
        </div>

        {/* Map */}
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
          <div className="flex items-center justify-between px-6 py-4 border-b border-gray-50">
            <div className="flex items-center gap-2">
              <div className="w-2 h-2 rounded-full bg-[#006B5F]" />
              <h2 className="text-sm font-bold text-gray-700">Geographic Distribution</h2>
            </div>
            <Link to="/insights" className="text-xs font-semibold text-[#006B5F] hover:underline">Details</Link>
          </div>
          {insights.length === 0 ? (
            <div className="p-8 text-center text-sm text-gray-400">No region data available.</div>
          ) : (
            <CandidateMap regionInsights={insights} height="320px" />
          )}
        </div>

        {/* Top Skills */}
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden lg:col-span-2">
          <div className="flex items-center justify-between px-6 py-4 border-b border-gray-50">
            <div className="flex items-center gap-2">
              <div className="w-2 h-2 rounded-full bg-blue-500" />
              <h2 className="text-sm font-bold text-gray-700">Top Skills</h2>
            </div>
            <span className="text-xs text-gray-400">{candidates.length} candidates</span>
          </div>
          <div className="p-6 grid grid-cols-1 md:grid-cols-2 gap-4">
            {candidates.length === 0 ? (
              <p className="text-sm text-gray-400 text-center py-4 col-span-2">No skills data available.</p>
            ) : stats.skills.length === 0 ? (
              <p className="text-sm text-gray-400 text-center py-4 col-span-2">No skills recorded.</p>
            ) : (
              stats.skills.map(([skill, count], idx) => {
                const pct = Math.round((count / stats.maxSkill) * 100);
                return (
                  <div key={skill}>
                    <div className="flex items-center justify-between mb-1.5">
                      <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${skillColors[idx % skillColors.length]}`}>
                        {skill}
                      </span>
                      <span className="text-xs text-gray-500">{count} candidates</span>
                    </div>
                    <div className="w-full h-2 bg-gray-100 rounded-full overflow-hidden">
                      <div className="h-full rounded-full bg-gradient-to-r from-[#006B5F] to-[#00A88F] transition-all duration-500" style={{ width: `${pct}%` }} />
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </div>

      </div>

    </div>
  );
}

export default App;
