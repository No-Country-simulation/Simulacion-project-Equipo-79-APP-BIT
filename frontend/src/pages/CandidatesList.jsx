import { useEffect, useMemo, useState } from 'react';
import { useParams, Link } from 'react-router';
import { sileo } from 'sileo';
import { getJobById, findMatches } from '../api/jobs.js';
import { listCandidates, getFullProfile } from '../api/candidates.js';
import { initiateContact, findByJob } from '../api/recruitment.js';
import CandidateMap from '../components/CandidateMap';
import ChevronIcon from '../components/icons/ChevronIcon';
import PinIcon from '../components/icons/PinIcon';
import 'leaflet/dist/leaflet.css';

const statusLabels = {
  CONTACTADO: { text: 'Contactado', color: 'bg-blue-50 text-blue-700 border-blue-200' },
  INTERESADO: { text: 'Interesado', color: 'bg-cyan-50 text-cyan-700 border-cyan-200' },
  ENTREVISTA: { text: 'En entrevista', color: 'bg-purple-50 text-purple-700 border-purple-200' },
  OFERTA: { text: 'Oferta enviada', color: 'bg-emerald-50 text-emerald-700 border-emerald-200' },
  DESCARTADO: { text: 'Descartado', color: 'bg-gray-100 text-gray-500 border-gray-200' },
};

const experienceLevels = ['JUNIOR', 'MID', 'SENIOR'];

const normalizeSkills = (skills = []) => {
  if (!Array.isArray(skills)) {
    return [];
  }

  return [...new Set(
    skills
      .filter(Boolean)
      .map(skill => String(skill).trim())
      .filter(Boolean)
  )];
};

const mapCandidateForView = (candidate, matchResult) => {
  const matchingSkills = normalizeSkills(matchResult?.matchingSkills ?? []);

  return {
    candidateId: candidate.candidateId ?? candidate.id,
    skills: normalizeSkills(candidate.skills),
    experienceLevel: candidate.experienceLevel ?? 'MID',
    region: candidate.region ?? candidate.municipio ?? 'No region',
    cluster: candidate.cluster ?? '',
    diversityBadge: matchResult?.diversityBadge ?? candidate.diversityBadge ?? '',
    diversityScore: matchResult?.diversityScore ?? 0,
    latitude: candidate.latitude ?? candidate.lat,
    longitude: candidate.longitude ?? candidate.lng,
    matchingSkills,
    compatibilityScore: matchResult?.compatibilityScore ?? 0,
    inclusionReason: matchResult?.inclusionReason ?? '',
  };
};

const scoreLabel = (score) => {
  if (score >= 90) return { text: 'Alta compatibilidad', color: 'text-emerald-600' };
  if (score >= 70) return { text: 'Buena compatibilidad', color: 'text-amber-600' };
  if (score >= 50) return { text: 'Compatibilidad media', color: 'text-orange-600' };
  return { text: 'Baja compatibilidad', color: 'text-red-600' };
};

const ScoreCircle = ({ score }) => {
  const color = score >= 85 ? '#006B5F' : score >= 70 ? '#F59E0B' : '#EF4444';
  const label = scoreLabel(score);
  return (
    <div className="flex flex-col items-center gap-0.5">
      <div className="relative w-14 h-14 flex items-center justify-center">
        <svg className="w-14 h-14 -rotate-90" viewBox="0 0 36 36">
          <circle cx="18" cy="18" r="15.5" fill="none" stroke="#E5E7EB" strokeWidth="2.5" />
          <circle cx="18" cy="18" r="15.5" fill="none" stroke={color} strokeWidth="2.5"
            strokeDasharray={`${(score / 100) * 97.4} 97.4`} strokeLinecap="round" />
        </svg>
        <span className="absolute text-xs font-bold" style={{ color }}>{score}%</span>
      </div>
      <span className={`text-[9px] font-semibold text-center leading-tight ${label.color}`}>
        {label.text}
      </span>
    </div>
  );
};

const badgeColors = {
  TALENTO_REGIONAL: 'bg-emerald-100 text-emerald-800 border-emerald-200',
  TALENTO_RURAL: 'bg-green-100 text-green-800 border-green-200',
  MUJER_STEM: 'bg-purple-100 text-purple-800 border-purple-200',
  TALENTO_JOVEN: 'bg-blue-100 text-blue-800 border-blue-200',
  TALENTO_SENIOR: 'bg-amber-100 text-amber-800 border-amber-200',
  TALENTO_RECONVERSION: 'bg-cyan-100 text-cyan-800 border-cyan-200',
  REGIONAL_DIVERSITY: 'bg-teal-100 text-teal-800 border-teal-200',
  GENDER_DIVERSITY: 'bg-pink-100 text-pink-800 border-pink-200',
};

const BadgeTag = ({ badge }) => {
  if (!badge) return null;
  const styles = badgeColors[badge] || 'bg-gray-100 text-gray-700 border-gray-200';
  return (
    <span className={`text-[10px] font-bold uppercase tracking-wider px-2.5 py-1 rounded-full border ${styles}`}>
      {badge.replace(/_/g, ' ')}
    </span>
  );
};

const DiversityChip = ({ label, value }) => {
  if (!value) return null;
  return (
    <span className="text-[10px] font-medium text-gray-500 bg-gray-50 border border-gray-200 px-2 py-0.5 rounded-md">
      {label}: {value}
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
  const [matchLoading, setMatchLoading] = useState(false);
  const [matchError, setMatchError] = useState('');
  const [recruitmentByCandidate, setRecruitmentByCandidate] = useState(new Map());
  const [contacting, setContacting] = useState(false);
  const [fullProfiles, setFullProfiles] = useState(new Map());
  const [regionFilter, setRegionFilter] = useState('');
  const [levelFilter, setLevelFilter] = useState('');
  const [selectedCandidates, setSelectedCandidates] = useState(new Set());
  const [expandedCandidates, setExpandedCandidates] = useState(new Set());
  const [viewMode, setViewMode] = useState('list');

  // Carga crítica: job + candidatos. Un fallo aquí sí bloquea la página.
  useEffect(() => {
    let ignore = false;
    const loadCore = async () => {
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
      } catch (err) {
        if (!ignore) setError(err instanceof Error ? err.message : 'Unexpected error');
      } finally {
        if (!ignore) setLoading(false);
      }
    };
    loadCore();
    return () => { ignore = true; };
  }, [jobId]);

  // Matching con IA: independiente del job/candidatos. Si falla (timeout del LLM,
  // vacante sin skills, etc.) no debe tumbar la página completa — se degrada a
  // mostrar la lista de candidatos sin ranking, con un aviso.
  useEffect(() => {
    if (!job) return;
    let ignore = false;
    const loadMatches = async () => {
      try {
        setMatchLoading(true);
        setMatchError('');
        const results = await findMatches({
          title: job.title,
          description: job.description ?? '',
          skills: job.skills ?? [],
          softSkills: job.softSkills ?? [],
          experienceLevel: job.experienceLevel,
          region: job.region,
          modality: job.modality ?? null,
          salaryRange: job.salaryRange ?? null,
          contractType: job.contractType ?? null,
          experienceYears: job.experienceYears ?? null,
          education: job.education ?? null,
          companyIndustry: job.company?.industrySector ?? null,
          companyEsgGoals: job.company?.esgGoals ?? null,
        });
        if (!ignore) setMatchResults(Array.isArray(results) ? results : []);
      } catch (err) {
        if (!ignore) {
          setMatchResults([]);
          setMatchError(
            err instanceof Error
              ? err.message
              : 'No se pudieron calcular los puntajes de compatibilidad.'
          );
        }
      } finally {
        if (!ignore) setMatchLoading(false);
      }
    };
    loadMatches();
    return () => { ignore = true; };
  }, [job]);

  // Estado de reclutamiento existente (candidatos ya contactados para esta vacante).
  useEffect(() => {
    if (!jobId) return;
    let ignore = false;
    const loadRecruitment = async () => {
      try {
        const records = await findByJob(jobId);
        if (ignore) return;
        const map = new Map();
        (Array.isArray(records) ? records : []).forEach(r => map.set(r.candidateId, r));
        setRecruitmentByCandidate(map);
      } catch {
        // No crítico: si falla, simplemente no se muestran estados previos.
      }
    };
    loadRecruitment();
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

    return viewCandidates
      .filter(c => {
        if (regionFilter && c.region !== regionFilter) return false;
        if (levelFilter && c.experienceLevel !== levelFilter) return false;
        return c.compatibilityScore >= 30;
      })
      .sort((a, b) => b.compatibilityScore - a.compatibilityScore)
      .slice(0, 15);
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
    if (recruitmentByCandidate.has(id)) return; // ya contactado, no se puede reseleccionar
    setSelectedCandidates(prev => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id); else next.add(id);
      return next;
    });
  };

  const toggleExpanded = (id) => {
    setExpandedCandidates(prev => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id); else next.add(id);
      return next;
    });

    if (!fullProfiles.has(id)) {
      setFullProfiles(prev => new Map(prev).set(id, { loading: true }));
      getFullProfile(id)
        .then(data => {
          setFullProfiles(prev => new Map(prev).set(id, { loading: false, data }));
        })
        .catch(err => {
          setFullProfiles(prev => new Map(prev).set(id, {
            loading: false,
            error: err instanceof Error ? err.message : 'No se pudo cargar el perfil completo.',
          }));
        });
    }
  };

  const handleContact = async () => {
    const ids = Array.from(selectedCandidates);
    if (ids.length === 0) return;

    setContacting(true);
    const outcomes = await Promise.allSettled(
      ids.map(candidateId => initiateContact({ jobId: Number(jobId), candidateId }))
    );

    const succeeded = [];
    const failed = [];
    outcomes.forEach((outcome, idx) => {
      if (outcome.status === 'fulfilled') {
        succeeded.push(outcome.value);
      } else {
        failed.push({ candidateId: ids[idx], error: outcome.reason });
      }
    });

    if (succeeded.length > 0) {
      setRecruitmentByCandidate(prev => {
        const next = new Map(prev);
        succeeded.forEach(record => next.set(record.candidateId, record));
        return next;
      });
      sileo.success({
        title: 'Contact initiated',
        description: `Contacto iniciado con ${succeeded.length} candidato(s).`,
      });
    }

    if (failed.length > 0) {
      sileo.error({
        title: `No se pudo contactar a ${failed.length} candidato(s)`,
        description: failed
          .map(f => (f.error instanceof Error ? f.error.message : 'Error desconocido'))
          .join(' · '),
      });
    }

    setSelectedCandidates(new Set());
    setContacting(false);
  };

  const clearFilters = () => {
    setRegionFilter('');
    setLevelFilter('');
    setSelectedCandidates(new Set());
    setExpandedCandidates(new Set());
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

      {/* Matching status banner */}
      {matchLoading && (
        <div className="flex items-center gap-2 bg-blue-50 border border-blue-100 text-blue-700 text-sm rounded-xl px-4 py-3">
          <svg className="w-4 h-4 animate-spin flex-shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21 12a9 9 0 1 1-6.219-8.56" /></svg>
          Calculando compatibilidad con IA...
        </div>
      )}
      {!matchLoading && matchError && (
        <div className="flex items-start gap-2 bg-amber-50 border border-amber-200 text-amber-800 text-sm rounded-xl px-4 py-3">
          <svg className="w-4 h-4 mt-0.5 flex-shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
          <span>No se pudieron calcular los puntajes de compatibilidad ({matchError}). Se muestran los candidatos sin ranking de IA.</span>
        </div>
      )}

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
          <button onClick={handleContact} disabled={contacting}
            className="bg-[#006B5F] hover:bg-[#005a50] active:scale-95 disabled:opacity-70 disabled:cursor-not-allowed text-white text-sm font-semibold px-5 py-2 rounded-lg transition-all shadow-sm cursor-pointer flex items-center gap-2">
            {contacting ? (
              <svg className="w-4 h-4 animate-spin" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21 12a9 9 0 1 1-6.219-8.56" /></svg>
            ) : (
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="2" y="7" width="20" height="14" rx="2" ry="2" /><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16" /></svg>
            )}
            {contacting ? 'Contacting...' : `Contact Selected (${selectedCandidates.size})`}
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
                <div className="flex-shrink-0 pt-1 flex flex-col items-center gap-1">
                  <ScoreCircle score={candidate.compatibilityScore} />
                  {candidate.diversityScore > 0 && (
                    <span className="text-[9px] font-bold text-purple-600 bg-purple-50 px-1.5 py-0.5 rounded">
                      D:{candidate.diversityScore}%
                    </span>
                  )}
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

                  {job.skills && job.skills.length > 0 && candidate.matchingSkills.length > 0 && (
                    <p className="text-[10px] text-gray-400 mt-1.5">
                      <span className="font-semibold text-[#006B5F]">{candidate.matchingSkills.length}</span>
                      <span className="text-gray-300">/{job.skills.length}</span> skills requeridas cubiertas
                    </p>
                  )}

                  {/* Expandable: Why this candidate */}
                  <div className="mt-3">
                    <button
                      onClick={() => toggleExpanded(candidate.candidateId)}
                      className="flex items-center gap-1.5 text-xs font-semibold text-[#006B5F] hover:text-[#005a50] transition-colors cursor-pointer"
                    >
                      <svg
                        className={`w-3.5 h-3.5 transition-transform ${expandedCandidates.has(candidate.candidateId) ? 'rotate-180' : ''}`}
                        viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5"
                      >
                        <polyline points="6 9 12 15 18 9" />
                      </svg>
                      ¿Por qué este candidato?
                    </button>

                    {expandedCandidates.has(candidate.candidateId) && (
                      <div className="mt-3 p-4 bg-gray-50 rounded-xl border border-gray-100 space-y-4">

                        {/* Match Overview Bar */}
                        <div className="flex items-center gap-4 p-3 bg-white rounded-lg border border-gray-200">
                          <div className="flex-1">
                            <div className="flex justify-between items-center mb-1">
                              <span className="text-[10px] font-bold uppercase tracking-wider text-gray-400">Compatibilidad general</span>
                              <span className="text-xs font-bold" style={{ color: candidate.compatibilityScore >= 85 ? '#006B5F' : candidate.compatibilityScore >= 70 ? '#F59E0B' : '#EF4444' }}>
                                {candidate.compatibilityScore}%
                              </span>
                            </div>
                            <div className="w-full h-2 bg-gray-100 rounded-full overflow-hidden">
                              <div className="h-full rounded-full transition-all" style={{
                                width: `${candidate.compatibilityScore}%`,
                                backgroundColor: candidate.compatibilityScore >= 85 ? '#006B5F' : candidate.compatibilityScore >= 70 ? '#F59E0B' : '#EF4444'
                              }} />
                            </div>
                          </div>
                          {candidate.diversityScore > 0 && (
                            <div className="text-center border-l border-gray-200 pl-4">
                              <span className="text-[10px] font-bold uppercase tracking-wider text-purple-500">Diversidad</span>
                              <p className="text-sm font-bold text-purple-700">{candidate.diversityScore}%</p>
                            </div>
                          )}
                        </div>

                        {/* Skills buscadas del Job */}
                        <div>
                          <p className="text-[10px] font-bold uppercase tracking-wider text-gray-400 mb-2 flex items-center gap-1">
                            <svg className="w-3 h-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="10" /><path d="M12 16v-4M12 8h.01" /></svg>
                            Skills buscadas en esta vacante
                          </p>
                          <div className="flex flex-wrap gap-1">
                            {job.skills && job.skills.length > 0
                              ? job.skills.map(skill => (
                                  <SkillTag key={skill} skill={skill} matched={candidate.matchingSkills.includes(skill)} />
                                ))
                              : <span className="text-xs text-gray-400">No se especificaron skills</span>
                            }
                          </div>
                          {candidate.matchingSkills.length > 0 && candidate.matchingSkills.length < (job.skills?.length ?? 0) && (
                            <p className="text-[10px] text-gray-400 mt-1.5">
                              {candidate.matchingSkills.length} de {job.skills.length} skills requeridas
                            </p>
                          )}
                        </div>

                        {/* Skills coincidentes */}
                        <div>
                          <p className="text-[10px] font-bold uppercase tracking-wider text-gray-400 mb-1.5 flex items-center gap-1">
                            <svg className="w-3 h-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="20 6 9 17 4 12" /></svg>
                            Skills coincidentes con el candidato
                          </p>
                          <div className="flex flex-wrap gap-1">
                            {candidate.matchingSkills.length > 0
                              ? candidate.matchingSkills.map(skill => (
                                  <SkillTag key={skill} skill={skill} matched />
                                ))
                              : <span className="text-xs text-gray-400">No hay skills coincidentes</span>
                            }
                          </div>
                        </div>

                        <hr className="border-gray-200" />

                        {/* Nivel de experiencia */}
                        <div className="flex items-center gap-2">
                          <svg className="w-3.5 h-3.5 text-gray-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="2" y="7" width="20" height="14" rx="2" ry="2" /><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16" /></svg>
                          <div>
                            <p className="text-[10px] font-bold uppercase tracking-wider text-gray-400">Nivel de experiencia</p>
                            <p className="text-xs font-semibold text-gray-700">{candidate.experienceLevel.charAt(0) + candidate.experienceLevel.slice(1).toLowerCase()}</p>
                          </div>
                        </div>

                        {/* Región */}
                        <div className="flex items-center gap-2">
                          <svg className="w-3.5 h-3.5 text-gray-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" /><circle cx="12" cy="10" r="3" /></svg>
                          <div>
                            <p className="text-[10px] font-bold uppercase tracking-wider text-gray-400">Región</p>
                            <p className="text-xs font-semibold text-gray-700">{candidate.region}</p>
                          </div>
                        </div>

                        {/* Badge de diversidad */}
                        {candidate.diversityBadge && (
                          <div className="flex items-center gap-2">
                            <svg className="w-3.5 h-3.5 text-gray-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" /></svg>
                            <div>
                              <p className="text-[10px] font-bold uppercase tracking-wider text-gray-400">Badge de diversidad</p>
                              <BadgeTag badge={candidate.diversityBadge} />
                            </div>
                          </div>
                        )}

                        {/* Explicación del score */}
                        <div>
                          <p className="text-[10px] font-bold uppercase tracking-wider text-gray-400 mb-1 flex items-center gap-1">
                            <svg className="w-3 h-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="10" /><line x1="12" y1="16" x2="12" y2="12" /><line x1="12" y1="8" x2="12.01" y2="8" /></svg>
                            Explicación del score
                          </p>
                          <p className="text-xs text-gray-600 leading-relaxed">{candidate.inclusionReason}</p>
                        </div>

                        {/* Perfil completo con datos de diversidad (consent-gated, cargado bajo demanda) */}
                        {(() => {
                          const profile = fullProfiles.get(candidate.candidateId);
                          if (!profile || profile.loading) {
                            return <p className="text-[10px] text-gray-400 pt-1">Cargando perfil completo...</p>;
                          }
                          if (profile.error) {
                            return <p className="text-[10px] text-red-400 pt-1">{profile.error}</p>;
                          }
                          if (!profile.data?.consentStatus) return null;
                          return (
                            <div className="flex flex-wrap gap-1.5 pt-1">
                              {profile.data.genderOptional && <DiversityChip label="Género" value={profile.data.genderOptional} />}
                              {profile.data.ethnicityOptional && <DiversityChip label="Etnia" value={profile.data.ethnicityOptional} />}
                              {profile.data.disabilityOptional && <DiversityChip label="Discapacidad" value={profile.data.disabilityOptional} />}
                              {profile.data.ruralOptional === true && (
                                <span className="text-[10px] font-medium text-green-600 bg-green-50 border border-green-200 px-2 py-0.5 rounded-md">
                                  Zona rural
                                </span>
                              )}
                            </div>
                          );
                        })()}
                      </div>
                    )}
                  </div>

                  <div className="mt-3 flex items-center gap-3">
                    {recruitmentByCandidate.has(candidate.candidateId) ? (
                      <span className={`text-[10px] font-bold uppercase tracking-wider px-2.5 py-1 rounded-full border ${statusLabels[recruitmentByCandidate.get(candidate.candidateId).status]?.color ?? 'bg-gray-100 text-gray-500 border-gray-200'}`}>
                        {statusLabels[recruitmentByCandidate.get(candidate.candidateId).status]?.text
                          ?? recruitmentByCandidate.get(candidate.candidateId).status}
                      </span>
                    ) : (
                      <label className="flex items-center gap-2 cursor-pointer group">
                        <input type="checkbox" checked={selectedCandidates.has(candidate.candidateId)}
                          onChange={() => toggleCandidate(candidate.candidateId)}
                          className="w-4 h-4 rounded border-gray-300 text-[#006B5F] focus:ring-[#006B5F] cursor-pointer accent-[#006B5F]" />
                        <span className="text-xs font-medium text-gray-500 group-hover:text-gray-700 select-none transition-colors">
                          {selectedCandidates.has(candidate.candidateId) ? 'Selected' : 'Select to contact'}
                        </span>
                      </label>
                    )}
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
