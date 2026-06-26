import { useEffect, useMemo, useState } from 'react';
import { useParams, Link } from 'react-router';
import { getJobById } from '../api/jobs.js';
import { listCandidates } from '../api/candidates.js';
import CandidateMap from '../components/CandidateMap';
import ChevronIcon from '../components/icons/ChevronIcon';
import PinIcon from '../components/icons/PinIcon';
import 'leaflet/dist/leaflet.css';

const JobsIconSm = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <rect x="2" y="7" width="20" height="14" rx="2" ry="2" />
    <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16" />
  </svg>
);

const experienceLevels = ['JUNIOR', 'MID', 'SENIOR'];

const normalizeText = (value) => value?.trim().toLowerCase() ?? '';

const getMatchingSkills = (candidateSkills = [], requiredSkills = []) => {
  const requiredByName = new Map(
    requiredSkills.map(skill => [normalizeText(skill), skill])
  );

  return candidateSkills.filter(skill => requiredByName.has(normalizeText(skill)));
};

const getCompatibilityScore = (candidate, job) => {
  const requiredSkills = job?.requiredSkills ?? job?.skills ?? [];

  if (requiredSkills.length === 0) {
    return candidate.experienceLevel === job?.experienceLevel ? 100 : 0;
  }

  const skillScore = getMatchingSkills(candidate.skills, requiredSkills).length / requiredSkills.length;
  const experienceBonus = candidate.experienceLevel === job?.experienceLevel ? 0.15 : 0;
  const regionBonus = normalizeText(candidate.region) === normalizeText(job?.region) ? 0.1 : 0;

  return Math.min(100, Math.round((skillScore + experienceBonus + regionBonus) * 100));
};

const getInclusionReason = (candidate, job, matchingSkills) => {
  if (matchingSkills.length > 0) {
    return `Coincide con ${matchingSkills.length} habilidad(es) requerida(s): ${matchingSkills.join(', ')}.`;
  }

  if (candidate.experienceLevel === job?.experienceLevel) {
    return 'Coincide con el nivel de experiencia requerido para esta vacante.';
  }

  return 'Perfil disponible en la base de candidatos registrados.';
};

const mapCandidateForView = (candidate, job) => {
  const viewCandidate = {
    candidateId: candidate.candidateId ?? candidate.id,
    skills: candidate.skills ?? [],
    experienceLevel: candidate.experienceLevel ?? 'MID',
    region: candidate.region ?? candidate.municipio ?? 'No region',
    diversityBadge: candidate.diversityBadge ?? '',
    latitude: candidate.latitude ?? candidate.lat,
    longitude: candidate.longitude ?? candidate.lng,
  };
  const matchingSkills = getMatchingSkills(viewCandidate.skills, job?.requiredSkills ?? job?.skills ?? []);

  return {
    ...viewCandidate,
    matchingSkills,
    compatibilityScore: getCompatibilityScore(viewCandidate, job),
    inclusionReason: getInclusionReason(viewCandidate, job, matchingSkills),
  };
};

const ScoreCircle = ({ score }) => {
  const color = score >= 85 ? '#006B5F' : score >= 70 ? '#F59E0B' : '#EF4444';
  return (
    <div className="relative w-16 h-16 flex items-center justify-center">
      <svg className="w-16 h-16 -rotate-90" viewBox="0 0 36 36">
        <circle cx="18" cy="18" r="15.5" fill="none" stroke="#E5E7EB" strokeWidth="2.5" />
        <circle cx="18" cy="18" r="15.5" fill="none" stroke={color} strokeWidth="2.5"
          strokeDasharray={`${(score / 100) * 97.4} 97.4`} strokeLinecap="round" />
      </svg>
      <span className="absolute text-sm font-bold" style={{ color }}>{score}%</span>
    </div>
  );
};

const BadgeTag = ({ badge }) => {
  if (!badge) return null;
  const colors = {
    DIVERSITY_LEADER: 'bg-emerald-100 text-emerald-800 border-emerald-200',
    INCLUSION_CHAMPION: 'bg-blue-100 text-blue-800 border-blue-200',
  };
  return (
    <span className={`text-[10px] font-bold uppercase tracking-wider px-2.5 py-1 rounded-full border ${colors[badge] || 'bg-gray-100 text-gray-700 border-gray-200'}`}>
      {badge.replace(/_/g, ' ')}
    </span>
  );
};

const SkillTag = ({ skill, matched }) => (
  <span className={`text-xs px-2.5 py-1 rounded-md font-medium ${matched ? 'bg-[#006B5F]/10 text-[#006B5F] border border-[#006B5F]/20' : 'bg-gray-100 text-gray-500 border border-gray-200'}`}>
    {skill}{matched && ' ✓'}
  </span>
);

const CandidatesList = () => {
  const { jobId } = useParams();

  const [job, setJob] = useState(null);
  const [candidates, setCandidates] = useState([]);
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

        if (!ignore) {
          setJob(jobData);
          setCandidates(Array.isArray(candidatesData) ? candidatesData : []);
        }
      } catch (err) {
        if (!ignore) {
          setError(err instanceof Error ? err.message : 'Unexpected error');
        }
      } finally {
        if (!ignore) {
          setLoading(false);
        }
      }
    };

    loadData();

    return () => {
      ignore = true;
    };
  }, [jobId]);

  const regions = useMemo(() => {
    const regionNames = candidates
      .map(candidate => candidate.region ?? candidate.municipio)
      .filter(Boolean);

    return [...new Set(regionNames)].sort((a, b) => a.localeCompare(b));
  }, [candidates]);

  const viewCandidates = useMemo(
    () => candidates.map(candidate => mapCandidateForView(candidate, job)),
    [candidates, job]
  );

  const filteredCandidates = useMemo(() => {
    if (!job) return [];
    return viewCandidates.filter(c => {
      if (regionFilter && c.region !== regionFilter) return false;
      if (levelFilter && c.experienceLevel !== levelFilter) return false;
      return true;
    }).sort((a, b) => b.compatibilityScore - a.compatibilityScore);
  }, [job, regionFilter, levelFilter, viewCandidates]);

  const toggleCandidate = (id) => {
    setSelectedCandidates(prev => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const handleContact = () => {
    alert(`Contacto iniciado con ${selectedCandidates.size} candidato(s) seleccionado(s).`);
  };

  const clearFilters = () => {
    setRegionFilter('');
    setLevelFilter('');
    setSelectedCandidates(new Set());
  };

  const inputClass = 'w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm text-gray-700 focus:outline-none focus:ring-2 focus:ring-[#2B6952]/20 focus:border-[#2B6952] bg-white transition-all';

  if (loading) {
    return (
      <div className="p-8 max-w-6xl mx-auto">
        <div className="bg-white rounded-2xl border border-gray-200 shadow-sm p-8 text-sm text-gray-500">
          Loading candidates...
        </div>
      </div>
    );
  }

  if (error || !job) {
    return (
      <div className="p-8 max-w-4xl mx-auto">
        <div className="bg-white rounded-2xl border border-gray-200 shadow-sm p-16 text-center">
          <p className="text-gray-400 text-lg">{error || 'Job not found.'}</p>
          <Link to="/job" className="text-[#006B5F] hover:underline text-sm mt-2 inline-block">Back to Jobs</Link>
        </div>
      </div>
    );
  }

  return (
    <div className="p-8 max-w-6xl mx-auto">
      <div className="mb-8">
        <Link to="/job" className="text-xs text-gray-400 hover:text-[#006B5F] transition-colors inline-flex items-center gap-1 mb-2">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="15 18 9 12 15 6" /></svg>
          Back to Jobs
        </Link>
        <h1 className="text-2xl font-bold text-gray-800">{job.title}</h1>
        <p className="text-sm text-gray-500 mt-1">
          {job.department} · {job.region} · {job.experienceLevel.charAt(0) + job.experienceLevel.slice(1).toLowerCase()}
        </p>
        <p className="text-xs text-gray-400 mt-1">{job.description}</p>
      </div>

      <div className="bg-white rounded-2xl border border-gray-200 shadow-sm p-6 mb-6">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="flex flex-col">
            <label className="text-[11px] font-bold uppercase tracking-wider text-gray-500 mb-1.5">Region</label>
            <div className="relative">
              <select value={regionFilter} onChange={e => setRegionFilter(e.target.value)} className={`${inputClass} appearance-none pr-9 cursor-pointer`}>
                <option value="">All Regions</option>
                {regions.map(r => <option key={r} value={r}>{r}</option>)}
              </select>
              <span className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-gray-400"><ChevronIcon /></span>
            </div>
          </div>
          <div className="flex flex-col">
            <label className="text-[11px] font-bold uppercase tracking-wider text-gray-500 mb-1.5">Experience Level</label>
            <div className="relative">
              <select value={levelFilter} onChange={e => setLevelFilter(e.target.value)} className={`${inputClass} appearance-none pr-9 cursor-pointer`}>
                <option value="">All Levels</option>
                {experienceLevels.map(l => <option key={l} value={l}>{l.charAt(0) + l.slice(1).toLowerCase()}</option>)}
              </select>
              <span className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-gray-400"><ChevronIcon /></span>
            </div>
          </div>
          <div className="flex items-end">
            <button onClick={clearFilters}
              className="w-full border border-gray-300 text-gray-600 hover:bg-gray-50 text-sm font-semibold px-6 py-2.5 rounded-lg transition-all cursor-pointer">
              Clear Filters
            </button>
          </div>
        </div>
      </div>

      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6">
        <div className="flex gap-4 text-sm text-gray-500">
          <span className="font-semibold text-gray-700">{filteredCandidates.length} candidates found</span>
          <span className="hidden sm:inline">|</span>
          <span>Avg Score: {filteredCandidates.length > 0 ? Math.round(filteredCandidates.reduce((a, c) => a + c.compatibilityScore, 0) / filteredCandidates.length) : 0}%</span>
          <span className="hidden sm:inline">|</span>
          <span>Diversity: {filteredCandidates.filter(c => c.diversityBadge).length}/{filteredCandidates.length}</span>
        </div>
        <div className="flex items-center gap-3">
          <div className="bg-gray-100 rounded-lg p-0.5 flex">
            <button onClick={() => setViewMode('list')}
              className={`text-xs font-semibold px-3 py-1.5 rounded-md transition-all cursor-pointer ${viewMode === 'list' ? 'bg-white text-gray-800 shadow-sm' : 'text-gray-500 hover:text-gray-700'}`}>
              List
            </button>
            <button onClick={() => setViewMode('map')}
              className={`text-xs font-semibold px-3 py-1.5 rounded-md transition-all cursor-pointer flex items-center gap-1 ${viewMode === 'map' ? 'bg-white text-gray-800 shadow-sm' : 'text-gray-500 hover:text-gray-700'}`}>
              <PinIcon />
              Map
            </button>
          </div>
          {selectedCandidates.size > 0 && (
            <button onClick={handleContact}
              className="bg-[#006B5F] hover:bg-[#005a50] active:scale-95 text-white text-sm font-semibold px-5 py-2 rounded-lg transition-all shadow-sm cursor-pointer flex items-center gap-2">
              <JobsIconSm />
              Contact Selected ({selectedCandidates.size})
            </button>
          )}
        </div>
      </div>

      {viewMode === 'map' && filteredCandidates.length > 0 && (
        <div className="mb-6">
          <CandidateMap candidates={filteredCandidates} height="500px" />
        </div>
      )}

      {viewMode === 'list' && (filteredCandidates.length === 0 ? (
        <div className="bg-white rounded-2xl border border-gray-200 shadow-sm p-12 text-center">
          <p className="text-gray-400 text-lg">No candidates match the selected filters.</p>
          <p className="text-gray-400 text-sm mt-1">Try adjusting the region or experience level criteria.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          {filteredCandidates.map(candidate => (
            <div key={candidate.candidateId}
              className={`bg-white rounded-2xl border shadow-sm p-5 transition-all hover:shadow-md ${selectedCandidates.has(candidate.candidateId) ? 'border-[#006B5F] ring-2 ring-[#006B5F]/20' : 'border-gray-200'}`}>
              <div className="flex items-start gap-4">
                <div className="flex-shrink-0 pt-1">
                  <ScoreCircle score={candidate.compatibilityScore} />
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-start justify-between gap-2">
                    <div>
                      <h3 className="text-sm font-bold text-gray-800">Candidate #{candidate.candidateId}</h3>
                      <p className="text-xs text-gray-500 mt-0.5">
                        {candidate.region} · {candidate.experienceLevel.charAt(0) + candidate.experienceLevel.slice(1).toLowerCase()}
                      </p>
                    </div>
                    <BadgeTag badge={candidate.diversityBadge} />
                  </div>

                  <div className="mt-3 flex flex-wrap gap-1.5">
                    {candidate.skills.map(skill => (
                      <SkillTag key={skill} skill={skill} matched={candidate.matchingSkills.includes(skill)} />
                    ))}
                  </div>

                  <p className="text-xs text-gray-500 mt-3 leading-relaxed border-t border-gray-100 pt-3">
                    {candidate.inclusionReason}
                  </p>

                  <div className="mt-3 flex items-center gap-3">
                    <label className="flex items-center gap-2 cursor-pointer group">
                      <input type="checkbox" checked={selectedCandidates.has(candidate.candidateId)}
                        onChange={() => toggleCandidate(candidate.candidateId)}
                        className="w-4 h-4 rounded border-gray-300 text-[#006B5F] focus:ring-[#006B5F] cursor-pointer accent-[#006B5F]" />
                      <span className="text-xs font-medium text-gray-600 group-hover:text-gray-800 select-none">
                        {selectedCandidates.has(candidate.candidateId) ? 'Selected' : 'Select to contact'}
                      </span>
                    </label>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      ))}
    </div>
  );
};

export default CandidatesList;
