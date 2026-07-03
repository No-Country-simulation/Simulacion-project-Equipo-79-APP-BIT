package com.appbit.backend.modules.insights.service;

import com.appbit.backend.modules.candidate.Service.CandidateService;
import com.appbit.backend.modules.candidate.entity.Candidate;
import com.appbit.backend.modules.candidate.repository.CandidateRepository;
import com.appbit.backend.modules.company.entity.Job;
import com.appbit.backend.modules.company.repository.JobRepository;
import com.appbit.backend.modules.insights.dto.RegionInsightResponse;
import com.appbit.backend.modules.insights.model.CoverageZone;
import com.appbit.backend.modules.insights.model.NetworkCoverage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class InsightsService {


    private static final String ANTENAS_CSV = "data/antenas_flp.csv";
    private static final String TENSOR_CSV = "data/tensor_concentracao.csv";
    private static final String CSV_SEPARATOR = ",";

    private static final double COVERAGE_GOOD_CONGESTION_MAX = 1.0;
    private static final double COVERAGE_GOOD_DROP_MAX = 0.5;
    private static final double COVERAGE_MEDIUM_CONGESTION_MAX = 3.0;
    private static final double COVERAGE_MEDIUM_DROP_MAX = 1.5;
    private static final int TOP_SKILLS_LIMIT = 3;

    private final CandidateService candidateService;
    private final CandidateRepository candidateRepository;
    private final JobRepository jobRepository;

    // Estructuras en memoria
    private final Map<String, CoverageZone> antenasByEcgi = new ConcurrentHashMap<>();
    private final Map<String, NetworkCoverage> coverageByEcgi = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        loadAntenas();
        loadTensorAndCalculateCoverage();
    }

    /**
     * PASO 1: Carga estática de antenas
     */
    private void loadAntenas() {
        log.info("📡 Iniciando carga de antenas desde {}", ANTENAS_CSV);
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(ANTENAS_CSV);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            if (is == null) throw new IllegalStateException("No se encontró " + ANTENAS_CSV);

            reader.readLine(); // Saltar cabecera
            String line;
            int totalLeidas = 0;

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                try {
                    String[] parts = line.split(CSV_SEPARATOR);
                    if (parts.length < 5) continue;

                    String ecgi = parts[0].trim();
                    String cluster = parts[1].trim();
                    String municipio = parts[2].trim();
                    double latitude = Double.parseDouble(parts[3].trim());
                    double longitude = Double.parseDouble(parts[4].trim());

                    if (!ecgi.isEmpty()) {
                        antenasByEcgi.put(ecgi, new CoverageZone(ecgi, cluster, municipio, latitude, longitude));
                        totalLeidas++;
                    }
                } catch (Exception ex) {
                    log.warn("Línea inválida en antenas: '{}' - {}", line, ex.getMessage());
                }
            }
            log.info("✅ Carga de antenas completada: {} registradas", totalLeidas);
        } catch (Exception ex) {
            log.error("💥 Error fatal cargando antenas: {}", ex.getMessage(), ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * PASO 2: Procesamiento de telemetría y cálculo de NetworkCoverage
     * Basado en la estructura real: ecgi(0), cluster(1), municipio(2), day_date(3),
     * periodo(4), n_usuarios(5), n_sessoes(6), download_bytes(7), upload_bytes(8),
     * dur_media_s(9), drop_pct_medio(10), congestionamento_medio(11)...
     */
    private void loadTensorAndCalculateCoverage() {
        log.info("📊 Iniciando procesamiento de telemetría desde {}", TENSOR_CSV);

        // Mapas temporales para acumular sumas y conteos por ecgi
        Map<String, Double> congestionSum = new HashMap<>();
        Map<String, Double> dropSum = new HashMap<>();
        Map<String, Integer> count = new HashMap<>();

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(TENSOR_CSV);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            if (is == null) {
                log.warn("⚠️ No se encontró {}. Todas las antenas se marcarán como POOR.", TENSOR_CSV);
                antenasByEcgi.keySet().forEach(ecgi -> coverageByEcgi.put(ecgi, NetworkCoverage.POOR));
                return;
            }

            reader.readLine(); // Saltar cabecera
            String line;
            int rowsProcessed = 0;

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                try {
                    String[] parts = line.split(CSV_SEPARATOR);
                    // Necesitamos al menos 12 columnas para leer congestionamento_medio (índice 11)
                    if (parts.length < 12) continue;

                    String ecgi = parts[0].trim();
                    double drop = Double.parseDouble(parts[10].trim());
                    double congestion = Double.parseDouble(parts[11].trim());

                    congestionSum.merge(ecgi, congestion, Double::sum);
                    dropSum.merge(ecgi, drop, Double::sum);
                    count.merge(ecgi, 1, Integer::sum);
                    rowsProcessed++;
                } catch (Exception ex) {
                    log.warn("Línea inválida en tensor: '{}' - {}", line, ex.getMessage());
                }
            }

            log.info("✅ {} filas de telemetría leídas. Calculando promedios...", rowsProcessed);

            // Calcular promedios y asignar Enum
            for (String ecgi : antenasByEcgi.keySet()) {
                if (count.containsKey(ecgi)) {
                    double avgCongestion = congestionSum.get(ecgi) / count.get(ecgi);
                    double avgDrop = dropSum.get(ecgi) / count.get(ecgi);

                    NetworkCoverage coverage = calculateCoverage(avgCongestion, avgDrop);
                    coverageByEcgi.put(ecgi, coverage);
                } else {
                    coverageByEcgi.put(ecgi, NetworkCoverage.POOR);
                }
            }
            log.info("✅ Cálculo de cobertura completado para {} antenas.", coverageByEcgi.size());

        } catch (Exception ex) {
            log.error("💥 Error fatal procesando tensor: {}", ex.getMessage(), ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Algoritmo de clasificación de red ajustado a la escala del CSV.
     */
    private NetworkCoverage calculateCoverage(double avgCongestion, double avgDrop) {
        if (avgCongestion <= COVERAGE_GOOD_CONGESTION_MAX && avgDrop <= COVERAGE_GOOD_DROP_MAX) {
            return NetworkCoverage.GOOD;
        }
        if (avgCongestion <= COVERAGE_MEDIUM_CONGESTION_MAX && avgDrop <= COVERAGE_MEDIUM_DROP_MAX) {
            return NetworkCoverage.MEDIUM;
        }
        return NetworkCoverage.POOR;
    }

    /**
     * Genera la lista final de insights por municipio, combinando infraestructura
     * y candidatos.
     */
    public List<RegionInsightResponse> getRegionInsights() {
        return getRegionInsights(null);
    }

    public List<RegionInsightResponse> getRegionInsights(Long jobId) {
        log.info("🛠️ Generando RegionInsights" + (jobId != null ? " para jobId=" + jobId : "") + "...");

        List<Candidate> allCandidates;
        Map<String, Long> candidateCounts;
        Map<String, Long> diversityCounts;

        if (jobId != null) {
            Job job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new NoSuchElementException("Vacante no encontrada con ID: " + jobId));
            allCandidates = candidateRepository.findAll().stream()
                    .filter(c -> job.getRegion() == null || job.getRegion().equals(c.getMunicipio()))
                    .filter(c -> job.getExperienceLevel() == null || job.getExperienceLevel() == c.getExperienceLevel())
                    .toList();

            candidateCounts = allCandidates.stream()
                    .collect(Collectors.groupingBy(Candidate::getMunicipio, Collectors.counting()));
            diversityCounts = allCandidates.stream()
                    .filter(c -> c.getDiversityBadge() != null && !c.getDiversityBadge().isEmpty())
                    .collect(Collectors.groupingBy(Candidate::getMunicipio, Collectors.counting()));
        } else {
            allCandidates = candidateRepository.findAll();
            candidateCounts = candidateService.countByMunicipio();
            diversityCounts = candidateService.countDiversityByMunicipio();
        }

        if (candidateCounts == null) candidateCounts = Collections.emptyMap();
        if (diversityCounts == null) diversityCounts = Collections.emptyMap();

        Map<String, List<String>> skillsByMunicipio = allCandidates.stream()
                .collect(Collectors.groupingBy(Candidate::getMunicipio,
                        Collectors.flatMapping(c -> c.getSkills() != null ? c.getSkills().stream() : Stream.empty(),
                                Collectors.toList())));

        // 2. Agrupar antenas por municipio
        Map<String, List<CoverageZone>> antenasByMunicipio = antenasByEcgi.values().stream()
                .collect(Collectors.groupingBy(CoverageZone::municipio));

        // Coordenadas promedio por municipio a partir de los candidatos, para los
        // municipios que no tienen antena en antenas_flp.csv (ver punto 3).
        Map<String, double[]> avgCoordsByMunicipio = allCandidates.stream()
                .collect(Collectors.groupingBy(Candidate::getMunicipio,
                        Collectors.teeing(
                                Collectors.averagingDouble(Candidate::getLatitude),
                                Collectors.averagingDouble(Candidate::getLongitude),
                                (lat, lon) -> new double[]{lat, lon}
                        )));

        // 3. Unión de municipios con antena Y municipios con candidatos: antes solo
        // se iteraba antenasByMunicipio, así que los municipios sembrados en data.sql
        // sin antena en antenas_flp.csv (la mayoría) nunca aparecían en /insights.
        Set<String> allMunicipios = new HashSet<>(antenasByMunicipio.keySet());
        allMunicipios.addAll(candidateCounts.keySet());

        List<RegionInsightResponse> response = new ArrayList<>();

        for (String municipio : allMunicipios) {
            List<CoverageZone> antenas = antenasByMunicipio.get(municipio);

            // Calcular cobertura del municipio (regla: la mejor antena define la cobertura).
            // Sin antena registrada no hay dato de red real, así que se reporta POOR.
            NetworkCoverage municipioCoverage = NetworkCoverage.POOR;
            double lat;
            double lon;

            if (antenas != null && !antenas.isEmpty()) {
                for (CoverageZone antena : antenas) {
                    NetworkCoverage antenaCoverage = coverageByEcgi.getOrDefault(antena.ecgi(), NetworkCoverage.POOR);
                    if (antenaCoverage == NetworkCoverage.GOOD) {
                        municipioCoverage = NetworkCoverage.GOOD;
                        break;
                    } else if (antenaCoverage == NetworkCoverage.MEDIUM) {
                        municipioCoverage = NetworkCoverage.MEDIUM;
                    }
                }
                lat = antenas.get(0).latitude();
                lon = antenas.get(0).longitude();
            } else {
                double[] avgCoords = avgCoordsByMunicipio.getOrDefault(municipio, new double[]{0.0, 0.0});
                lat = avgCoords[0];
                lon = avgCoords[1];
            }

            long density = candidateCounts.getOrDefault(municipio, 0L);
            long divCount = diversityCounts.getOrDefault(municipio, 0L);
            double divPct = density > 0 ? (divCount * 100.0 / density) : 0.0;

            List<String> topSkills = skillsByMunicipio.getOrDefault(municipio, List.of()).stream()
                    .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                    .entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(TOP_SKILLS_LIMIT)
                    .map(Map.Entry::getKey)
                    .toList();

            response.add(new RegionInsightResponse(
                    municipio,
                    (int) density,
                    municipioCoverage,
                    (int) density,
                    (int) divCount,
                    Math.round(divPct * 10.0) / 10.0,
                    topSkills,
                    lat,
                    lon
            ));
        }

        log.info("✅ {} municipios procesados para el mapa.", response.size());
        return response;
    }
}
