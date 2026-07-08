import { useEffect, useMemo, useState } from 'react';
import { listCandidates } from '../api/candidates';
import { getRegionInsights } from '../api/insights';
import CandidateMap from '../components/CandidateMap';
import 'leaflet/dist/leaflet.css';

const coverageConfig = {
  GOOD: { label: 'Good', bg: 'bg-emerald-500', light: 'bg-emerald-50', text: 'text-emerald-700', border: 'border-emerald-200', badge: 'bg-emerald-100 text-emerald-700 border border-emerald-200' },
  MEDIUM: { label: 'Medium', bg: 'bg-amber-400', light: 'bg-amber-50', text: 'text-amber-700', border: 'border-amber-200', badge: 'bg-amber-100 text-amber-700 border border-amber-200' },
  POOR: { label: 'Poor', bg: 'bg-red-500', light: 'bg-red-50', text: 'text-red-700', border: 'border-red-200', badge: 'bg-red-100 text-red-700 border border-red-200' },
};

const skillColors = ['bg-sky-100 text-sky-700', 'bg-indigo-100 text-indigo-700', 'bg-violet-100 text-violet-700', 'bg-pink-100 text-pink-700', 'bg-teal-100 text-teal-700'];

const Skeleton = ({ className = '' }) => (
  <div className={`animate-pulse bg-gray-200 rounded-xl ${className}`} />
);

const Insights = () => {
  const [insights, setInsights] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedRegion, setSelectedRegion] = useState(null);
  const [regionCandidates, setRegionCandidates] = useState({});
  const [loadingRegionCandidates, setLoadingRegionCandidates] = useState({});
  const [selectedCandidate, setSelectedCandidate] = useState(null);

  const formatCandidateValue = (value) => {
    if (value == null || value === '') return 'Not provided';
    if (Array.isArray(value)) return value.filter(Boolean).join(', ');
    if (typeof value === 'object') return JSON.stringify(value, null, 2);
    return String(value);
  };

  const getCandidateDisplayName = (candidate) => {
    const directName = candidate?.fullName || candidate?.name || candidate?.candidateName || candidate?.candidateFullName;
    if (directName) return directName;

    const firstName = candidate?.firstName ?? '';
    const lastName = candidate?.lastName ?? '';
    if (firstName || lastName) return [firstName, lastName].filter(Boolean).join(' ');

    return 'Candidate';
  };

  const getCandidateDedication = (candidate) => {
    const directValue = candidate?.dedication
      || candidate?.occupation
      || candidate?.profession
      || candidate?.jobTitle
      || candidate?.position
      || candidate?.role
      || candidate?.specialty
      || candidate?.currentRole
      || candidate?.professionalRole
      || candidate?.title;

    if (directValue) return String(directValue);

    const profileValue = candidate?.professionalProfile || candidate?.profile;
    if (profileValue) return String(profileValue);

    return 'Sin información';
  };

  const formatDiversityBadge = (value) => {
    if (!value) return 'Not provided';
    return String(value).replace(/_/g, ' ').replace(/\b\w/g, (char) => char.toUpperCase());
  };

  const getCandidateSummaryFields = (candidate) => {
    const skills = Array.isArray(candidate?.skills) ? candidate.skills : [];
    const uniqueSkills = [...new Set(skills.map((skill) => String(skill).trim()).filter(Boolean))];

    return [
      {
        key: 'dedication',
        label: 'A qué se dedica',
        value: getCandidateDedication(candidate),
        type: 'text',
      },
      {
        key: 'skills',
        label: 'Skills',
        value: uniqueSkills.length > 0 ? uniqueSkills : 'Not provided',
        type: 'list',
      },
      {
        key: 'experienceLevel',
        label: 'Experience',
        value: candidate?.experienceLevel || 'Not provided',
        type: 'text',
      },
      {
        key: 'municipio',
        label: 'Municipality',
        value: candidate?.municipio || candidate?.region || 'Not provided',
        type: 'text',
      },
      {
        key: 'diversityBadge',
        label: 'Diversity',
        value: candidate?.diversityBadge ? formatDiversityBadge(candidate.diversityBadge) : 'Not provided',
        type: 'text',
      },
    ];
  };

  useEffect(() => {
    let ignore = false;
    const load = async () => {
      try {
        setLoading(true);
        setError('');
        const data = await getRegionInsights();
        if (!ignore) setInsights(Array.isArray(data) ? data : []);
      } catch (err) {
        if (!ignore) setError(err instanceof Error ? err.message : 'Unexpected error');
      } finally {
        if (!ignore) setLoading(false);
      }
    };
    load();
    return () => { ignore = true; };
  }, []);

  const stats = useMemo(() => {
    if (!insights.length) return null;
    const totalCandidates = insights.reduce((s, r) => s + r.candidateDensity, 0);
    const totalDiversity = insights.reduce((s, r) => s + r.diversityCount, 0);
    const coverageCounts = { GOOD: 0, MEDIUM: 0, POOR: 0 };
    insights.forEach(r => { coverageCounts[r.networkCoverage]++; });
    return { totalRegions: insights.length, totalCandidates, totalDiversity, avgDiversityPct: totalCandidates > 0 ? Math.round((totalDiversity / totalCandidates) * 100) : 0, ...coverageCounts };
  }, [insights]);

  const loadRegionCandidates = async (municipio) => {
    if (!municipio || regionCandidates[municipio] || loadingRegionCandidates[municipio]) {
      return;
    }

    setLoadingRegionCandidates(prev => ({ ...prev, [municipio]: true }));

    try {
      const data = await listCandidates({ municipio });
      const candidates = Array.isArray(data) ? data : [];
      setRegionCandidates(prev => ({ ...prev, [municipio]: candidates }));
    } catch (err) {
      console.error(err);
      setRegionCandidates(prev => ({ ...prev, [municipio]: [] }));
    } finally {
      setLoadingRegionCandidates(prev => ({ ...prev, [municipio]: false }));
    }
  };

  const toggleRegion = (municipio) => {
    const nextSelection = selectedRegion === municipio ? null : municipio;
    setSelectedRegion(nextSelection);

    if (nextSelection) {
      loadRegionCandidates(municipio);
    }
  };

  if (loading) {
    return (
      <div className="p-6 max-w-7xl mx-auto space-y-6">
        <Skeleton className="h-36 w-full" />
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <Skeleton className="h-24" /><Skeleton className="h-24" /><Skeleton className="h-24" />
        </div>
        <Skeleton className="h-96 w-full" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-6 max-w-7xl mx-auto">
        <div className="bg-white rounded-3xl border border-red-200 shadow-sm p-16 text-center">
          <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-red-50 flex items-center justify-center">
            <svg className="w-8 h-8 text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2"><path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
          </div>
          <p className="text-gray-600 font-medium">Failed to load insights</p>
          <p className="text-gray-400 text-sm mt-1">{error}</p>
        </div>
      </div>
    );
  }

  const sortedByDensity = [...insights].sort((a, b) => b.candidateDensity - a.candidateDensity);
  const topRegion = sortedByDensity[0];

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">

      {/* Hero */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-[#006B5F] via-[#008575] to-[#00A88F] p-8 text-white">
        <div className="absolute top-0 right-0 w-64 h-64 bg-white/5 rounded-full -translate-y-1/2 translate-x-1/2" />
        <div className="absolute bottom-0 left-1/3 w-48 h-48 bg-white/5 rounded-full translate-y-1/3" />
        <div className="relative z-10">
          <div className="flex items-center gap-2 text-white/70 text-sm font-medium mb-2">
            <svg className="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" /></svg>
            Geographic Intelligence
          </div>
          <h1 className="text-3xl font-bold tracking-tight">Region Insights</h1>
          <p className="text-white/80 text-sm mt-2 max-w-xl">
            Explore candidate distribution and network coverage across regions to make data-driven hiring decisions.
          </p>
        </div>
      </div>

      {/* Stats Cards */}
      {stats && (
        <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5 hover:shadow-md transition-shadow">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-emerald-50 flex items-center justify-center flex-shrink-0">
                <svg className="w-5 h-5 text-emerald-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2" /><circle cx="9" cy="7" r="4" /><path d="M23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75" /></svg>
              </div>
              <div>
                <p className="text-xs font-medium text-gray-400 uppercase tracking-wider">Total Candidates</p>
                <p className="text-2xl font-bold text-gray-800">{stats.totalCandidates}</p>
              </div>
            </div>
          </div>
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5 hover:shadow-md transition-shadow">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-blue-50 flex items-center justify-center flex-shrink-0">
                <svg className="w-5 h-5 text-blue-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z" /><circle cx="12" cy="10" r="3" /></svg>
              </div>
              <div>
                <p className="text-xs font-medium text-gray-400 uppercase tracking-wider">Regions</p>
                <p className="text-2xl font-bold text-gray-800">{stats.totalRegions}</p>
              </div>
            </div>
          </div>
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5 hover:shadow-md transition-shadow">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-amber-50 flex items-center justify-center flex-shrink-0">
                <svg className="w-5 h-5 text-amber-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M12 20V10M18 20V4M6 20v-4" /></svg>
              </div>
              <div>
                <p className="text-xs font-medium text-gray-400 uppercase tracking-wider">Good Coverage</p>
                <p className="text-2xl font-bold text-gray-800">{stats.GOOD}<span className="text-sm font-medium text-gray-400 ml-1">/ {stats.totalRegions}</span></p>
              </div>
            </div>
          </div>
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5 hover:shadow-md transition-shadow">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-emerald-50 flex items-center justify-center flex-shrink-0">
                <svg className="w-5 h-5 text-emerald-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" /></svg>
              </div>
              <div>
                <p className="text-xs font-medium text-gray-400 uppercase tracking-wider">Top Region</p>
                <p className="text-lg font-bold text-gray-800 truncate max-w-28">{topRegion?.municipio}</p>
              </div>
            </div>
          </div>
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5 hover:shadow-md transition-shadow">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-purple-50 flex items-center justify-center flex-shrink-0">
                <svg className="w-5 h-5 text-purple-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" /></svg>
              </div>
              <div>
                <p className="text-xs font-medium text-gray-400 uppercase tracking-wider">Diversity</p>
                <p className="text-2xl font-bold text-gray-800">{stats.totalDiversity}<span className="text-sm font-medium text-gray-400 ml-1">/ {stats.totalCandidates}</span></p>
                <p className="text-xs text-purple-600 font-medium">{stats.avgDiversityPct}% avg</p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Map */}
      <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-50">
          <div className="flex items-center gap-2">
            <div className="w-2 h-2 rounded-full bg-[#006B5F]" />
            <h2 className="text-sm font-bold text-gray-700">Coverage Map</h2>
          </div>
          <div className="flex items-center gap-3 text-xs">
            <span className="flex items-center gap-1.5"><span className="w-2.5 h-2.5 rounded-full bg-emerald-500" /> Good</span>
            <span className="flex items-center gap-1.5"><span className="w-2.5 h-2.5 rounded-full bg-amber-400" /> Medium</span>
            <span className="flex items-center gap-1.5"><span className="w-2.5 h-2.5 rounded-full bg-red-500" /> Poor</span>
          </div>
        </div>
        <div>
          <CandidateMap regionInsights={insights} height="480px" />
        </div>
      </div>

      {/* Region Cards */}
      <div>
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <div className="w-2 h-2 rounded-full bg-[#006B5F]" />
            <h2 className="text-sm font-bold text-gray-700">Regions Overview</h2>
          </div>
          <span className="text-xs text-gray-400">{insights.length} regions</span>
        </div>
        <div className="max-h-[480px] overflow-y-auto pr-2 space-y-3">
          {insights.map((region, idx) => {
            const cov = coverageConfig[region.networkCoverage] ?? coverageConfig.POOR;
            const isSelected = selectedRegion === region.municipio;
            const densityPercent = stats ? Math.round((region.candidateDensity / stats.totalCandidates) * 100) : 0;
            const barWidth = topRegion ? Math.round((region.candidateDensity / topRegion.candidateDensity) * 100) : 0;
            const candidatesForRegion = regionCandidates[region.municipio] ?? [];
            const isLoadingCandidates = loadingRegionCandidates[region.municipio];

            return (
              <div
                key={region.municipio}
                onClick={() => toggleRegion(region.municipio)}
                className="group bg-white rounded-2xl border border-gray-100 shadow-sm hover:shadow-md hover:border-gray-200 transition-all cursor-pointer overflow-hidden"
              >
                <div className="p-4 sm:p-5">
                  <div className="flex items-center justify-between gap-3">
                    <div className="flex items-center gap-3 min-w-0">
                      <div className={`w-9 h-9 rounded-xl flex items-center justify-center text-sm font-bold flex-shrink-0 ${cov.light} ${cov.text}`}>
                        {String(idx + 1).padStart(2, '0')}
                      </div>
                      <div className="min-w-0">
                        <h3 className="text-sm font-bold text-gray-800 truncate">{region.municipio}</h3>
                        <div className="flex flex-wrap items-center gap-2 mt-0.5 text-xs text-gray-500">
                          <span>{region.candidateDensity} candidates</span>
                          <span className="text-gray-300">·</span>
                          <span>{region.diversityCount} diverse</span>
                          <span className="text-gray-300">·</span>
                          <span className={`font-semibold px-2 py-0.5 rounded-full ${cov.badge}`}>
                            {region.networkCoverage}
                          </span>
                        </div>
                      </div>
                    </div>

                    <div className="hidden sm:flex items-center gap-4 flex-shrink-0">
                      <div className="w-24">
                        <div className="flex justify-between text-[10px] text-gray-400 mb-1">
                          <span>Density</span>
                          <span>{densityPercent}%</span>
                        </div>
                        <div className="w-full h-1.5 bg-gray-100 rounded-full overflow-hidden">
                          <div
                            className="h-full rounded-full transition-all duration-500"
                            style={{ width: `${barWidth}%`, backgroundColor: cov === coverageConfig.GOOD ? '#10B981' : cov === coverageConfig.MEDIUM ? '#F59E0B' : '#EF4444' }}
                          />
                        </div>
                      </div>
                      <svg className={`w-4 h-4 text-gray-300 transition-transform ${isSelected ? 'rotate-180' : ''}`} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="6 9 12 15 18 9" /></svg>
                    </div>
                  </div>

                  {isSelected && (
                    <div className="mt-4 pt-4 border-t border-gray-100 space-y-4">
                      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-sm">
                        <div>
                          <p className="text-[10px] font-bold uppercase tracking-wider text-gray-400 mb-1">Density</p>
                          <p className="font-semibold text-gray-800">{region.candidateDensity}</p>
                        </div>
                        <div>
                          <p className="text-[10px] font-bold uppercase tracking-wider text-gray-400 mb-1">Diversity</p>
                          <p className="font-semibold text-gray-800">{region.diversityCount} <span className="text-xs font-normal text-gray-400">({region.diversityPercentage}%)</span></p>
                        </div>
                        <div>
                          <p className="text-[10px] font-bold uppercase tracking-wider text-gray-400 mb-1">Profiles</p>
                          <p className="font-semibold text-gray-800">{region.availableProfiles}</p>
                        </div>
                        <div>
                          <p className="text-[10px] font-bold uppercase tracking-wider text-gray-400 mb-1">Coordinates</p>
                          <p className="text-[11px] text-gray-500 font-mono">{region.latitude.toFixed(4)}, {region.longitude.toFixed(4)}</p>
                        </div>
                      </div>

                      {region.topSkills?.length > 0 && (
                        <div>
                          <p className="text-[10px] font-bold uppercase tracking-wider text-gray-400 mb-2">Top Skills</p>
                          <div className="flex flex-wrap gap-1.5">
                            {region.topSkills.map((skill, i) => (
                              <span key={skill} className={`text-xs px-2 py-0.5 rounded-full font-medium ${skillColors[i % skillColors.length]}`}>
                                {skill}
                              </span>
                            ))}
                          </div>
                        </div>
                      )}

                      <div>
                        <div className="flex items-center justify-between mb-2">
                          <p className="text-[10px] font-bold uppercase tracking-wider text-gray-400">Candidates in this region</p>
                          <span className="text-[11px] text-gray-400">{candidatesForRegion.length} found</span>
                        </div>

                        {isLoadingCandidates ? (
                          <div className="space-y-2">
                            <div className="h-12 rounded-xl bg-gray-100 animate-pulse" />
                            <div className="h-12 rounded-xl bg-gray-100 animate-pulse" />
                          </div>
                        ) : candidatesForRegion.length > 0 ? (
                          <div className="max-h-48 overflow-y-auto space-y-2 pr-1">
                            {candidatesForRegion.map((candidate, index) => {
                              const candidateId = candidate.candidateId ?? candidate.id ?? `candidate-${index + 1}`;
                              const skills = Array.isArray(candidate.skills) ? candidate.skills : [];
                              const candidateName = getCandidateDisplayName(candidate) || `Candidate ${candidateId}`;
                              return (
                                <div key={candidateId} className="rounded-xl border border-gray-100 bg-gray-50 px-3 py-2.5">
                                  <div className="flex items-start gap-2">
                                    <div className="flex h-6 w-6 items-center justify-center rounded-full bg-[#006B5F] text-[11px] font-semibold text-white">
                                      {index + 1}
                                    </div>
                                    <div className="min-w-0 flex-1">
                                      <div className="flex items-start justify-between gap-2">
                                        <div className="min-w-0">
                                          <p className="text-sm font-semibold text-gray-800">{candidateName}</p>
                                          {getCandidateDedication(candidate) !== 'Sin información' && (
                                            <p className="text-xs text-[#006B5F] font-medium">Se dedica a: {getCandidateDedication(candidate)}</p>
                                          )}
                                          <p className="text-xs text-gray-500">{candidate.experienceLevel ?? 'Experience not listed'}</p>
                                        </div>
                                        <div className="flex items-center gap-2 flex-shrink-0">
                                          {candidate.compatibilityScore != null && (
                                            <span className="text-xs font-semibold text-[#006B5F]">{candidate.compatibilityScore}%</span>
                                          )}
                                          <button
                                            type="button"
                                            onClick={(event) => {
                                              event.stopPropagation();
                                              setSelectedCandidate(candidate);
                                            }}
                                            className="text-xs font-semibold text-[#006B5F] hover:text-[#004D45] transition-colors"
                                          >
                                            Mostrar más
                                          </button>
                                        </div>
                                      </div>
                                      {skills.length > 0 && (
                                        <div className="mt-2 flex flex-wrap gap-1.5">
                                          {skills.slice(0, 3).map((skill) => (
                                            <span key={`${candidateId}-${skill}`} className="text-[11px] px-2 py-0.5 rounded-full bg-white text-gray-600 border border-gray-200">
                                              {skill}
                                            </span>
                                          ))}
                                        </div>
                                      )}
                                    </div>
                                  </div>
                                </div>
                              );
                            })}
                          </div>
                        ) : (
                          <div className="rounded-xl border border-dashed border-gray-200 bg-gray-50 px-3 py-4 text-sm text-gray-500">
                            No candidates available for this region yet.
                          </div>
                        )}
                      </div>
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {selectedCandidate && (
        <div
          className="fixed inset-0 z-[2000] flex items-center justify-center bg-black/50 px-4 py-6"
          onClick={() => setSelectedCandidate(null)}
        >
          <div
            className="relative z-[2001] w-full max-w-3xl max-h-[80vh] overflow-y-auto rounded-3xl bg-white shadow-2xl"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="flex items-start justify-between gap-3 border-b border-gray-100 px-6 py-4">
              <div>
                <p className="text-[11px] font-semibold uppercase tracking-[0.25em] text-gray-400">Candidate details</p>
                <h3 className="text-lg font-bold text-gray-800">{getCandidateDisplayName(selectedCandidate)}</h3>
              </div>
              <button
                type="button"
                onClick={() => setSelectedCandidate(null)}
                className="rounded-full p-2 text-gray-400 hover:bg-gray-100 hover:text-gray-700"
                aria-label="Close candidate details"
              >
                <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <div className="space-y-4 p-6">
              {getCandidateSummaryFields(selectedCandidate).map(({ key, label, value, type }) => (
                <div key={key} className="rounded-2xl border border-gray-100 bg-gray-50 p-4">
                  <p className="text-[11px] font-bold uppercase tracking-wider text-gray-400 mb-2">{label}</p>
                  {type === 'list' && Array.isArray(value) ? (
                    <div className="flex flex-wrap gap-2">
                      {value.map((skill) => (
                        <span key={`${key}-${skill}`} className="rounded-full border border-gray-200 bg-white px-3 py-1 text-sm text-gray-700">
                          {skill}
                        </span>
                      ))}
                    </div>
                  ) : (
                    <p className="whitespace-pre-wrap text-sm text-gray-700">{formatCandidateValue(value)}</p>
                  )}
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Insights;
