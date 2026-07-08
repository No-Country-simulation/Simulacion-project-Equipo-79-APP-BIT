package com.appbit.backend.modules.dashboard.service;

import com.appbit.backend.modules.candidate.Service.CandidateService;
import com.appbit.backend.modules.candidate.entity.Candidate;
import com.appbit.backend.modules.candidate.repository.CandidateRepository;
import com.appbit.backend.modules.company.entity.Job;
import com.appbit.backend.modules.company.repository.CompanyRepository;
import com.appbit.backend.modules.company.repository.JobRepository;
import com.appbit.backend.modules.dashboard.dto.DashboardEsgResponse;
import com.appbit.backend.modules.recruitment.entity.RecruitmentProcess;
import com.appbit.backend.modules.recruitment.entity.RecruitmentStatus;
import com.appbit.backend.modules.recruitment.repository.RecruitmentProcessRepository;
import com.appbit.backend.core.util.TranslationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardEsgService {

        private static final int DIVERSITY_THRESHOLD_MET = 40;
        private static final int DIVERSITY_THRESHOLD_PROGRESS = 20;

        private static final String STATUS_CUMPLIDA = "CUMPLIDA";
        private static final String STATUS_EN_PROGRESO = "EN_PROGRESO";
        private static final String STATUS_NO_ALCANZADA = "NO_ALCANZADA";

        private static final String GENDER_UNDEFINED = "Not Declared";
        private static final String EXPERIENCE_UNDEFINED = "Unclassified";
        private static final String MUNICIPIO_UNDEFINED = "No municipality";

        private static final String GOAL_CONFIGURED_DEFAULT = "Diversity goal configured by the company";
        private static final String GOAL_UNCONFIGURED = "No goal configured";

        private final CandidateRepository candidateRepository;
        private final CandidateService candidateService;
        private final JobRepository jobRepository;
        private final CompanyRepository companyRepository;
        private final RecruitmentProcessRepository recruitmentRepository;

        public DashboardEsgResponse getEsgMetrics() {
                return getEsgMetrics(null);
        }

        public DashboardEsgResponse getEsgMetrics(Long jobId) {
                List<Candidate> scope;
                String goalLabel = companyRepository.count() > 0
                                ? GOAL_CONFIGURED_DEFAULT
                                : GOAL_UNCONFIGURED;

                if (jobId != null) {
                        Job job = jobRepository.findById(jobId)
                                        .orElseThrow(() -> new NoSuchElementException(
                                                        "Vacante no encontrada con ID: " + jobId));

                        // Filtramos en la base de datos en lugar de traer todo y filtrar en memoria
                        scope = candidateRepository.findCandidatesForMatchingWithLimit(
                                        job.getRegion(),
                                        job.getExperienceLevel(),
                                        Pageable.unpaged() // Trae todos los que coincidan sin límite
                        );

                        goalLabel = job.getTargetDiversityPercentage() != null
                                        ? job.getTargetDiversityPercentage() + "% diverse shortlist"
                                        : goalLabel;
                        log.info("Dashboard ESG filtrado por jobId={}: {} candidatos en scope", jobId, scope.size());
                } else {
                        scope = candidateRepository.findAll();
                        log.info("Generando métricas ESG globales...");
                }

                long totalCandidates = scope.size();
                long totalJobs = jobRepository.count();
                long totalDiversity = scope.stream()
                                .filter(c -> c.getDiversityBadge() != null && !c.getDiversityBadge().isEmpty()).count();
                double diversityPct = totalCandidates > 0
                                ? Math.round((totalDiversity * 1000.0 / totalCandidates)) / 10.0
                                : 0.0;

                Map<String, Long> byMunicipio = scope.stream()
                                .collect(Collectors.groupingBy(this::municipioOrUndefined, Collectors.counting()));
                Map<String, Long> divByMunicipio = scope.stream()
                                .filter(c -> c.getDiversityBadge() != null && !c.getDiversityBadge().isEmpty())
                                .collect(Collectors.groupingBy(this::municipioOrUndefined, Collectors.counting()));
                long totalRegions = byMunicipio.size();

                List<DashboardEsgResponse.BadgeBreakdown> badges = buildBadgeBreakdown(scope, totalCandidates);
                List<DashboardEsgResponse.RegionDiversity> regions = buildRegionDiversity(byMunicipio, divByMunicipio);
                DashboardEsgResponse.EsgCompliance compliance = buildEsgCompliance(diversityPct, goalLabel);
                List<DashboardEsgResponse.ExperienceLevelBreakdown> experienceBreakdown = buildExperienceBreakdown(
                                scope, totalCandidates);
                List<DashboardEsgResponse.GenderBreakdown> genderBreakdown = buildGenderBreakdown(scope,
                                totalCandidates);
                DashboardEsgResponse.PipelineBreakdown pipeline = buildPipeline(jobId);

                return new DashboardEsgResponse(
                                totalCandidates, totalJobs, totalRegions,
                                diversityPct, totalDiversity, badges, regions, compliance,
                                experienceBreakdown, genderBreakdown, pipeline);
        }

        private String municipioOrUndefined(Candidate c) {
                return c.getMunicipio() != null ? c.getMunicipio() : MUNICIPIO_UNDEFINED;
        }

        private List<DashboardEsgResponse.BadgeBreakdown> buildBadgeBreakdown(List<Candidate> scope, long total) {
                Map<String, Long> grouped = scope.stream()
                                .filter(c -> c.getDiversityBadge() != null && !c.getDiversityBadge().isEmpty())
                                .collect(Collectors.groupingBy(Candidate::getDiversityBadge, Collectors.counting()));

                return grouped.entrySet().stream()
                                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                                .map(e -> {
                                        double pct = total > 0 ? Math.round((e.getValue() * 1000.0 / total)) / 10.0
                                                        : 0.0;
                                        return new DashboardEsgResponse.BadgeBreakdown(
                                                        TranslationHelper.translateBadge(e.getKey()), e.getValue(),
                                                        pct);
                                })
                                .toList();
        }

        private List<DashboardEsgResponse.RegionDiversity> buildRegionDiversity(
                        Map<String, Long> totalByMunicipio,
                        Map<String, Long> divByMunicipio) {
                return totalByMunicipio.entrySet().stream()
                                .map(e -> {
                                        String m = e.getKey();
                                        long t = e.getValue();
                                        long d = divByMunicipio.getOrDefault(m, 0L);
                                        double p = t > 0 ? Math.round((d * 1000.0 / t)) / 10.0 : 0.0;
                                        return new DashboardEsgResponse.RegionDiversity(m, t, d, p);
                                })
                                .sorted((a, b) -> Long.compare(b.diversity(), a.diversity()))
                                .toList();
        }

        private DashboardEsgResponse.EsgCompliance buildEsgCompliance(double current, String goal) {
                String status = current >= DIVERSITY_THRESHOLD_MET ? STATUS_CUMPLIDA
                                : current >= DIVERSITY_THRESHOLD_PROGRESS ? STATUS_EN_PROGRESO
                                                : STATUS_NO_ALCANZADA;
                return new DashboardEsgResponse.EsgCompliance(goal, current, TranslationHelper.translateStatus(status));
        }

        private List<DashboardEsgResponse.ExperienceLevelBreakdown> buildExperienceBreakdown(List<Candidate> scope,
                        long total) {
                return scope.stream()
                                .collect(Collectors.groupingBy(
                                                c -> c.getExperienceLevel() != null ? c.getExperienceLevel().name()
                                                                : EXPERIENCE_UNDEFINED,
                                                Collectors.counting()))
                                .entrySet().stream()
                                .map(e -> {
                                        double pct = total > 0 ? Math.round((e.getValue() * 1000.0 / total)) / 10.0
                                                        : 0.0;
                                        return new DashboardEsgResponse.ExperienceLevelBreakdown(e.getKey(),
                                                        e.getValue(), pct);
                                })
                                .toList();
        }

        private List<DashboardEsgResponse.GenderBreakdown> buildGenderBreakdown(List<Candidate> scope, long total) {
                return scope.stream()
                                .collect(Collectors.groupingBy(
                                                c -> c.getGenderOptional() != null && !c.getGenderOptional().isBlank()
                                                                ? c.getGenderOptional()
                                                                : GENDER_UNDEFINED,
                                                Collectors.counting()))
                                .entrySet().stream()
                                .map(e -> {
                                        double pct = total > 0 ? Math.round((e.getValue() * 1000.0 / total)) / 10.0
                                                        : 0.0;
                                        return new DashboardEsgResponse.GenderBreakdown(e.getKey(), e.getValue(), pct);
                                })
                                .toList();
        }

        private DashboardEsgResponse.PipelineBreakdown buildPipeline(Long jobId) {
                long contactados = 0, interesados = 0, entrevista = 0, oferta = 0, descartados = 0;
                if (jobId != null) {
                        contactados = recruitmentRepository.countByJobIdAndStatus(jobId, RecruitmentStatus.CONTACTADO);
                        interesados = recruitmentRepository.countByJobIdAndStatus(jobId, RecruitmentStatus.INTERESADO);
                        entrevista = recruitmentRepository.countByJobIdAndStatus(jobId, RecruitmentStatus.ENTREVISTA);
                        oferta = recruitmentRepository.countByJobIdAndStatus(jobId, RecruitmentStatus.OFERTA);
                        descartados = recruitmentRepository.countByJobIdAndStatus(jobId, RecruitmentStatus.DESCARTADO);
                } else {
                        Map<RecruitmentStatus, Long> global = recruitmentRepository.findAll().stream()
                                        .collect(Collectors.groupingBy(
                                                        RecruitmentProcess::getStatus,
                                                        Collectors.counting()));
                        contactados = global.getOrDefault(RecruitmentStatus.CONTACTADO, 0L);
                        interesados = global.getOrDefault(RecruitmentStatus.INTERESADO, 0L);
                        entrevista = global.getOrDefault(RecruitmentStatus.ENTREVISTA, 0L);
                        oferta = global.getOrDefault(RecruitmentStatus.OFERTA, 0L);
                        descartados = global.getOrDefault(RecruitmentStatus.DESCARTADO, 0L);
                }
                long total = contactados + interesados + entrevista + oferta + descartados;
                return new DashboardEsgResponse.PipelineBreakdown(contactados, interesados, entrevista, oferta,
                                descartados, total);
        }
}
