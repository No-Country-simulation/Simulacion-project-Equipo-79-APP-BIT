import { useEffect, useMemo } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';

const getScoreColor = (score) => {
  if (score >= 85) return '#006B5F';
  if (score >= 70) return '#F59E0B';
  return '#EF4444';
};

const createColoredIcon = (color) => L.divIcon({
  className: 'custom-marker',
  html: `<svg width="28" height="28" viewBox="0 0 24 24" fill="${color}" stroke="white" stroke-width="2"><circle cx="12" cy="12" r="10"/></svg>`,
  iconSize: [28, 28],
  iconAnchor: [14, 14],
  popupAnchor: [0, -14],
});

const getCoverageColor = (coverage) => {
  if (coverage === 'GOOD') return '#22C55E';
  if (coverage === 'MEDIUM') return '#F59E0B';
  return '#EF4444';
};

const FitBounds = ({ points }) => {
  const map = useMap();
  useEffect(() => {
    if (points.length > 0) {
      const bounds = L.latLngBounds(points.map(p => [p.lat, p.lng]));
      map.fitBounds(bounds, { padding: [50, 50] });
    }
  }, [map, points]);
  return null;
};

const DefaultCenter = () => {
  const map = useMap();
  useEffect(() => {
    map.setView([-27.5, -48.5], 10);
  }, [map]);
  return null;
};

const CandidateMap = ({ candidates = [], regionInsights = [], height = '400px' }) => {
  const hasData = candidates.length > 0 || regionInsights.length > 0;

  const markers = useMemo(() => {
    if (candidates.length > 0) {
      return candidates.map(c => ({
        type: 'candidate',
        id: c.candidateId,
        lat: c.latitude ?? c.lat,
        lng: c.longitude ?? c.lng,
        label: `Candidate #${c.candidateId ?? c.id}`,
        score: c.compatibilityScore ?? 0,
        region: c.region ?? c.municipio,
        skills: c.skills ?? [],
        diversityBadge: c.diversityBadge,
        experienceLevel: c.experienceLevel,
      })).filter(m => m.lat && m.lng);
    }
    if (regionInsights.length > 0) {
      return regionInsights.map(r => ({
        type: 'insight',
        id: r.municipio,
        lat: r.latitude,
        lng: r.longitude,
        label: r.municipio,
        candidateDensity: r.candidateDensity,
        networkCoverage: r.networkCoverage,
        availableProfiles: r.availableProfiles,
      })).filter(m => m.lat && m.lng);
    }
    return [];
  }, [candidates, regionInsights]);

  const points = markers.map(m => ({ lat: m.lat, lng: m.lng }));

  return (
    <div className="rounded-2xl overflow-hidden border border-gray-200 shadow-sm" style={{ height }}>
      <MapContainer
        center={[-27.5, -48.5]}
        zoom={10}
        className="w-full h-full"
        scrollWheelZoom={true}
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        {hasData ? <FitBounds points={points} /> : <DefaultCenter />}
        {markers.map(m => (
          <Marker
            key={`${m.type}-${m.id}`}
            position={[m.lat, m.lng]}
            icon={m.type === 'candidate'
              ? createColoredIcon(getScoreColor(m.score))
              : createColoredIcon(getCoverageColor(m.networkCoverage))
            }
          >
            <Popup>
              {m.type === 'candidate' ? (
                <div className="text-sm">
                  <strong className="text-gray-800">{m.label}</strong>
                  <div className="text-gray-500 text-xs mt-1">{m.region}</div>
                  <div className="text-gray-500 text-xs">{m.experienceLevel}</div>
                  <div className="mt-1">
                    <span className="font-semibold" style={{ color: getScoreColor(m.score) }}>{m.score}%</span> match
                  </div>
                  {m.diversityBadge && (
                    <div className="text-xs text-purple-600 font-medium mt-1">{m.diversityBadge}</div>
                  )}
                  {m.skills.length > 0 && (
                    <div className="flex flex-wrap gap-1 mt-1">
                      {m.skills.slice(0, 4).map(s => (
                        <span key={s} className="text-[10px] bg-gray-100 px-1.5 py-0.5 rounded">{s}</span>
                      ))}
                      {m.skills.length > 4 && <span className="text-[10px] text-gray-400">+{m.skills.length - 4}</span>}
                    </div>
                  )}
                </div>
              ) : (
                <div className="text-sm">
                  <strong className="text-gray-800">{m.label}</strong>
                  <div className="text-gray-500 text-xs mt-1">
                    <span className="font-semibold">{m.candidateDensity}</span> candidates
                  </div>
                  <div className="text-xs mt-0.5">
                    Coverage: <span className="font-semibold" style={{ color: getCoverageColor(m.networkCoverage) }}>{m.networkCoverage}</span>
                  </div>
                  <div className="text-xs text-gray-500">{m.availableProfiles} profiles available</div>
                </div>
              )}
            </Popup>
          </Marker>
        ))}
      </MapContainer>
    </div>
  );
};

export default CandidateMap;
