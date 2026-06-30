import { useEffect, useMemo, useState } from 'react';
import { useParams, Link } from 'react-router';
import { sileo } from 'sileo';
import { getJobById, findMatches } from '../api/jobs.js';
import { listCandidates } from '../api/candidates.js';
import CandidateMap from '../components/CandidateMap';
import ChevronIcon from '../components/icons/ChevronIcon';
import PinIcon from '../components/icons/PinIcon';
import 'leaflet/dist/leaflet.css';

const experienceLevels = ['JUNIOR', 'MID', 'SENIOR'];

const mapCandidateForView = (candidate, matchResult) => {
  return {
    candidateId: candidate.candidateId ?? candidate.id,
    skills: candidate.skills ?? [],
    experienceLevel: candidate.experienceLevel ?? 'MID',
    region: candidate.region ?? candidate.municipio ?? 'No region',
    diversityBadge: matchResult?.diversityBadge ?? '',
    latitude: candidate.latitude ?? candidate.lat,
    longitude: candidate.longitude ?? candidate.lng,
    matchingSkills: matchResult?.matchingSkills ?? [],
    compatibilityScore: matchResult?.compatibilityScore ?? 0,
    inclusionReason: matchResult?.inclusionReason ?? '',
  };
};

const ScoreCircle = ({ score }) => {
  const color = score >= 85 ? '#006B5F' : score >= 70 ? '#F59E0B' : '#EF4444';
  return (
    <div className="relative w-14 h-14 flex items-center justify-center">
      <svg className="w-14 h-14 -rotate-90" viewBox="0 0 36 36">
        <circle cx="18" cy="18" r="15.5" fill="none" stroke="#E5E7EB" strokeWidth="2.5" />
        <circle cx="18" cy="18" r="15.5" fill="none" stroke={color} strokeWidth="2.5"
          strokeDasharray={`${(score / 100) * 97.4} 97.4`} strokeLinecap="round" />
      </svg>
      <span className="absolute text-xs font-bold" style={{ color }}>{score}%</span>
    </div>
  );
};

const BadgeTag = ({ badge }) => {
  if (!badge) return null;
  const colors = {
    DIVERSITY_LEADER: 'bg-emerald-100 text-emerald-800 border-emerald-200',
    INCLUSION_CHAMPION: 'bg-blue-100 text-blue-800 border-blue-200',
  };
  const styles = colors[badge] || 'bg-gray-100 text-gray-700 border-gray-200';
  return (
    <span className={`text-[10px] font-bold uppercase tracking-wider px-2.5 py-1 rounded-full border ${styles}`}>
      {badge.replace(/_/g, ' ')}
    </span>
  );
};

const SkillTag = ({ skill, matched }) => (
  <span className={`text-xs px-2.5 py-1 rounded-md font-medium ${matched ? 'bg-[#006B5F]/10 text-[#006B5F] border border-[#006B5F]/20' : 'bg-gray-100 text-gray-500 border border-gray-200'}`}>
    {skill}{matched && ' ✓'}
  </span>
);

const Skeleton = ({ className = '' }) => (
  <div className={`animate-pulse bg-gray-200 rounded-xl ${className}`} />
);

const CandidatesList = () => {
  const { jobId } = useParams();

  const [job, setJob] = useState(null);
  const [candidates, setCandidates] = useState([]);
  const [matchResults, setMatchResults] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [regionFilter, setRegionFilter] = useState('');
  const [levelFilter, setLevelFilter] = useState('');
  const [selectedCandidates, setSelectedCandidates] = useState(new Set());
  const [viewMode, setViewMode] = useState('list');

  useEffect(() => {
    let ignore = false;
    const loadData = async () => {
      try {
        setLoading(true);
        setError('');
        const [jobData, candidatesData] = await Promise.all([
          getJobById(jobId),
          listCandidates(),
        ]);
        if (ignore) return;

        setJob(jobData);
        setCandidates(Array.isArray(candidatesData) ? candidatesData : []);

        const results = await findMatches({
          title: jobData.title,
          description: jobData.description ?? '',
          skills: jobData.skills ?? [],
          experienceLevel: jobData.experienceLevel,
          region: jobData.region,
        });

        if (!ignore) {
          setMatchResults(Array.isArray(results) ? results : []);
        }
      } catch (err) {
        if (!ignore) setError(err instanceof Error ? err.message : 'Unexpected error');
      } finally {
        if (!ignore) setLoading(false);
      }
    };
    loadData();
    return () => { ignore = true; };
  }, [jobId]);

  const regions = useMemo(() => {
    const regionNames = candidates.map(c => c.region ?? c.municipio).filter(Boolean);
    return [...new Set(regionNames)].sort((a, b) => a.localeCompare(b));
  }, [candidates]);

  const matchResultsMap = useMemo(() => {
    const map = new Map();
    matchResults.forEach(r => map.set(r.candidateId, r));
    return map;
  }, [matchResults]);

  const viewCandidates = useMemo(
    () => candidates.map(c => {
      const matchResult = matchResultsMap.get(c.candidateId ?? c.id);
      return mapCandidateForView(c, matchResult);
    }),
    [candidates, matchResultsMap]
  );

  const filteredCandidates = useMemo(() => {
    if (!job) return [];
    return viewCandidates.filter(c => {
      if (regionFilter && c.region !== regionFilter) return false;
      if (levelFilter && c.experienceLevel !== levelFilter) return false;
      return true;
    }).sort((a, b) => b.compatibilityScore - a.compatibilityScore);
  }, [job, regionFilter, levelFilter, viewCandidates]);

  const geoLocatedCandidates = useMemo(
    () => filteredCandidates.filter(c => c.latitude && c.longitude),
    [filteredCandidates]
  );

  const stats = useMemo(() => {
    if (!filteredCandidates.length) return null;
    const avg = Math.round(filteredCandidates.reduce((s, c) => s + c.compatibilityScore, 0) / filteredCandidates.length);
    const diversity = filteredCandidates.filter(c => c.diversityBadge).length;
    const uniqueRegions = new Set(filteredCandidates.map(c => c.region)).size;
    return { avg, diversity, uniqueRegions, total: filteredCandidates.length, geoLocated: geoLocatedCandidates.length };
  }, [filteredCandidates, geoLocatedCandidates]);

  const toggleCandidate = (id) => {
    setSelectedCandidates(prev => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id); else next.add(id);
      return next;
    });
  };

  const handleContact = () => {
    sileo.success({
      title: 'Contact initiated',
      description: `Contacto iniciado con ${selectedCandidates.size} candidato(s) seleccionado(s).`,
    });
    setSelectedCandidates(new Set());
  };

  const clearFilters = () => {
    setRegionFilter('');
    setLevelFilter('');
    setSelectedCandidates(new Set());
  };

  const inputClass = 'w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm text-gray-700 focus:outline-none focus:ring-2 focus:ring-[#2B6952]/20 focus:border-[#2B6952] bg-white transition-all';

  if (loading) {
    return (
      <div className="p-6 max-w-7xl mx-auto space-y-6">
        <Skeleton className="h-44 w-full" />
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <Skeleton className="h-20" /><Skeleton className="h-20" /><Skeleton className="h-20" /><Skeleton className="h-20" />
        </div>
        <Skeleton className="h-20 w-full" />
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          <Skeleton className="h-40" /><Skeleton className="h-40" /><Skeleton className="h-40" /><Skeleton className="h-40" />
        </div>
      </div>
    );
  }

  if (error || !job) {
    return (
      <div className="p-6 max-w-4xl mx-auto">
        <div className="bg-white rounded-3xl border border-red-200 shadow-sm p-16 text-center">
          <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-red-50 flex items-center justify-center">
            <svg className="w-8 h-8 text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2"><path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
          </div>
          <p className="text-gray-600 font-medium">{error || 'Job not found.'}</p>
          <Link to="/job" className="text-[#006B5F] hover:underline text-sm mt-3 inline-block">Back to Jobs</Link>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">

      {/* Hero */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-[#006B5F] via-[#008575] to-[#00A88F] p-8 text-white">
        <div className="absolute top-0 right-0 w-64 h-64 bg-white/5 rounded-full -translate-y-1/2 translate-x-1/2" />
        <div className="absolute bottom-0 left-1/3 w-48 h-48 bg-white/5 rounded-full translate-y-1/3" />
        <div className="relative z-10">
          <Link to="/job" className="inline-flex items-center gap-1.5 text-white/70 hover:text-white text-xs font-medium mb-3 transition-colors">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="15 18 9 12 15 6" /></svg>
            Back to Jobs
          </Link>
          <h1 className="text-3xl font-bold tracking-tight">{job.title}</h1>
          <div className="flex flex-wrap items-center gap-3 mt-2 text-white/80 text-sm">
            {job.department && (
              <span className="inline-flex items-center gap-1.5 bg-white/10 px-3 py-1 rounded-full">
                <svg className="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="2" y="7" width="20" height="14" rx="2" ry="2" /><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16" /></svg>
                {job.department}
              </span>
            )}
            {job.region && (
              <span className="inline-flex items-center gap-1.5 bg-white/10 px-3 py-1 rounded-full">
                <svg className="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" /><circle cx="12" cy="10" r="3" /></svg>
                {job.region}
              </span>
            )}
            <span className="inline-flex items-center gap-1.5 bg-white/10 px-3 py-1 rounded-full">
              <svg className="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M12 20V10M18 20V4M6 20v-4" /></svg>
              {job.experienceLevel.charAt(0) + job.experienceLevel.slice(1).toLowerCase()}
            </span>
          </div>
          {job.description && (
            <p className="text-white/70 text-sm mt-3 max-w-2xl leading-relaxed">{job.description}</p>
          )}
        </div>
      </div>

      {/* Stats Cards */}
      {stats && (
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-4 hover:shadow-md transition-shadow">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-emerald-50 flex items-center justify-center flex-shrink-0">
                <svg className="w-5 h-5 text-emerald-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" /><circle cx="9" cy="7" r="4" /><path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" /></svg>
              </div>
              <div>
                <p className="text-[10px] font-bold uppercase tracking-wider text-gray-400">Candidates</p>
                <p className="text-xl font-bold text-gray-800">{stats.total}</p>
              </div>
            </div>
          </div>
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-4 hover:shadow-md transition-shadow">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-blue-50 flex items-center justify-center flex-shrink-0">
                <svg className="w-5 h-5 text-blue-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" /></svg>
              </div>
              <div>
                <p className="text-[10px] font-bold uppercase tracking-wider text-gray-400">Avg Score</p>
                <p className="text-xl font-bold text-gray-800">{stats.avg}<span className="text-sm font-normal text-gray-400">%</span></p>
              </div>
            </div>
          </div>
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-4 hover:shadow-md transition-shadow">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-purple-50 flex items-center justify-center flex-shrink-0">
                <svg className="w-5 h-5 text-purple-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M12 20h9M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z" /></svg>
              </div>
              <div>
                <p className="text-[10px] font-bold uppercase tracking-wider text-gray-400">Diversity</p>
                <p className="text-xl font-bold text-gray-800">{stats.diversity}<span className="text-sm font-normal text-gray-400">/{stats.total}</span></p>
              </div>
            </div>
          </div>
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-4 hover:shadow-md transition-shadow">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-amber-50 flex items-center justify-center flex-shrink-0">
                <svg className="w-5 h-5 text-amber-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" /><circle cx="12" cy="10" r="3" /></svg>
              </div>
              <div>
                <p className="text-[10px] font-bold uppercase tracking-wider text-gray-400">Regions</p>
                <p className="text-xl font-bold text-gray-800">{stats.uniqueRegions}</p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Filters */}
      <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div className="flex flex-col">
            <label className="text-[10px] font-bold uppercase tracking-wider text-gray-400 mb-1.5">Region</label>
            <div className="relative">
              <select value={regionFilter} onChange={e => setRegionFilter(e.target.value)} className={`${inputClass} appearance-none pr-9 cursor-pointer`}>
                <option value="">All Regions</option>
                {regions.map(r => <option key={r} value={r}>{r}</option>)}
              </select>
              <span className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-gray-400"><ChevronIcon /></span>
            </div>
          </div>
          <div className="flex flex-col">
            <label className="text-[10px] font-bold uppercase tracking-wider text-gray-400 mb-1.5">Experience Level</label>
            <div className="relative">
              <select value={levelFilter} onChange={e => setLevelFilter(e.target.value)} className={`${inputClass} appearance-none pr-9 cursor-pointer`}>
                <option value="">All Levels</option>
                {experienceLevels.map(l => <option key={l} value={l}>{l.charAt(0) + l.slice(1).toLowerCase()}</option>)}
              </select>
              <span className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-gray-400"><ChevronIcon /></span>
            </div>
          </div>
          <div className="flex flex-col">
            <label className="text-[10px] font-bold uppercase tracking-wider text-gray-400 mb-1.5">View</label>
            <div className="bg-gray-100 rounded-lg p-0.5 flex h-[42px]">
              <button onClick={() => setViewMode('list')}
                className={`flex-1 text-xs font-semibold px-3 rounded-md transition-all cursor-pointer ${viewMode === 'list' ? 'bg-white text-gray-800 shadow-sm' : 'text-gray-500 hover:text-gray-700'}`}>
                List
              </button>
              <button onClick={() => setViewMode('map')}
                className={`flex-1 text-xs font-semibold px-3 rounded-md transition-all cursor-pointer flex items-center justify-center gap-1 ${viewMode === 'map' ? 'bg-white text-gray-800 shadow-sm' : 'text-gray-500 hover:text-gray-700'}`}>
                <PinIcon /> Map
              </button>
            </div>
          </div>
          <div className="flex flex-col justify-end">
            <button onClick={clearFilters}
              className="w-full border border-gray-200 text-gray-500 hover:bg-gray-50 hover:text-gray-700 text-sm font-semibold px-6 py-2.5 rounded-lg transition-all cursor-pointer">
              Clear Filters
            </button>
          </div>
        </div>
      </div>

      {/* Toolbar */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3">
        <div className="flex items-center gap-2 text-sm text-gray-500">
          <span className="font-semibold text-gray-700">{filteredCandidates.length} candidates</span>
          {geoLocatedCandidates.length > 0 && geoLocatedCandidates.length < filteredCandidates.length && (
            <>
              <span className="text-gray-300">·</span>
              <span>{geoLocatedCandidates.length} with location</span>
            </>
          )}
        </div>
        {selectedCandidates.size > 0 && (
          <button onClick={handleContact}
            className="bg-[#006B5F] hover:bg-[#005a50] active:scale-95 text-white text-sm font-semibold px-5 py-2 rounded-lg transition-all shadow-sm cursor-pointer flex items-center gap-2">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="2" y="7" width="20" height="14" rx="2" ry="2" /><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16" /></svg>
            Contact Selected ({selectedCandidates.size})
          </button>
        )}
      </div>

      {/* Map View */}
      {viewMode === 'map' && (
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
          <div className="flex items-center justify-between px-6 py-4 border-b border-gray-50">
            <div className="flex items-center gap-2">
              <div className="w-2 h-2 rounded-full bg-[#006B5F]" />
              <h2 className="text-sm font-bold text-gray-700">Candidate Locations</h2>
            </div>
            <div className="flex items-center gap-3 text-xs">
              <span className="flex items-center gap-1.5"><span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: '#006B5F' }} /> High match</span>
              <span className="flex items-center gap-1.5"><span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: '#F59E0B' }} /> Medium match</span>
              <span className="flex items-center gap-1.5"><span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: '#EF4444' }} /> Low match</span>
            </div>
          </div>
          {geoLocatedCandidates.length === 0 ? (
            <div className="p-16 text-center text-sm text-gray-400">
              No candidates with location data match the current filters.
            </div>
          ) : (
            <CandidateMap candidates={filteredCandidates} height="480px" />
          )}
        </div>
      )}

      {/* List View - Empty */}
      {viewMode === 'list' && filteredCandidates.length === 0 && (
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-16 text-center">
          <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-gray-50 flex items-center justify-center">
            <svg className="w-8 h-8 text-gray-300" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" /><circle cx="9" cy="7" r="4" /></svg>
          </div>
          <p className="text-gray-500 font-medium">No candidates match the selected filters</p>
          <p className="text-gray-400 text-sm mt-1">Try adjusting the region or experience level criteria.</p>
        </div>
      )}

      {/* List View - Grid */}
      {viewMode === 'list' && filteredCandidates.length > 0 && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          {filteredCandidates.map(candidate => (
            <div key={candidate.candidateId}
              className={`group bg-white rounded-2xl border shadow-sm p-5 transition-all hover:shadow-md ${selectedCandidates.has(candidate.candidateId) ? 'border-[#006B5F] ring-2 ring-[#006B5F]/20' : 'border-gray-100 hover:border-gray-200'}`}>
              <div className="flex items-start gap-4">
                <div className="flex-shrink-0 pt-1">
                  <ScoreCircle score={candidate.compatibilityScore} />
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-start justify-between gap-2">
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-[10px] font-bold text-gray-300">#{String(candidate.candidateId).padStart(2, '0')}</span>
                        <h3 className="text-sm font-bold text-gray-800">Candidate {candidate.candidateId}</h3>
                      </div>
                      <p className="text-xs text-gray-500 mt-0.5">
                        {candidate.region}
                        <span className="mx-1.5 text-gray-300">·</span>
                        {candidate.experienceLevel.charAt(0) + candidate.experienceLevel.slice(1).toLowerCase()}
                        {candidate.latitude && (
                          <>
                            <span className="mx-1.5 text-gray-300">·</span>
                            <span className="text-gray-400">{candidate.latitude.toFixed(2)}, {candidate.longitude.toFixed(2)}</span>
                          </>
                        )}
                      </p>
                    </div>
                    <BadgeTag badge={candidate.diversityBadge} />
                  </div>

                  <div className="mt-3 flex flex-wrap gap-1.5">
                    {candidate.skills.map(skill => (
                      <SkillTag key={skill} skill={skill} matched={candidate.matchingSkills.includes(skill)} />
                    ))}
                  </div>

                  <div className="mt-3 flex items-center justify-between">
                    <p className="text-xs text-gray-500 leading-relaxed flex-1 min-w-0 mr-4">
                      {candidate.inclusionReason}
                    </p>
                  </div>

                  <div className="mt-3 flex items-center gap-3">
                    <label className="flex items-center gap-2 cursor-pointer group">
                      <input type="checkbox" checked={selectedCandidates.has(candidate.candidateId)}
                        onChange={() => toggleCandidate(candidate.candidateId)}
                        className="w-4 h-4 rounded border-gray-300 text-[#006B5F] focus:ring-[#006B5F] cursor-pointer accent-[#006B5F]" />
                      <span className="text-xs font-medium text-gray-500 group-hover:text-gray-700 select-none transition-colors">
                        {selectedCandidates.has(candidate.candidateId) ? 'Selected' : 'Select to contact'}
                      </span>
                    </label>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default CandidatesList;
