package com.appbit.backend.modules.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Respuesta agregada del dashboard ESG con métricas de diversidad e inclusión")
public record DashboardEsgResponse(

        @Schema(description = "Total de candidatos en la plataforma", example = "500")
        long totalCandidates,

        @Schema(description = "Total de vacantes publicadas", example = "12")
        long totalJobs,

        @Schema(description = "Total de regiones/municipios con candidatos", example = "20")
        long totalRegions,

        @Schema(description = "Porcentaje de candidatos con badge de diversidad", example = "65.5")
        double diversityPercentage,

        @Schema(description = "Total de candidatos con badge de diversidad", example = "327")
        long totalDiversity,

        @Schema(description = "Desglose de badges de diversidad")
        List<BadgeBreakdown> badgeBreakdown,

        @Schema(description = "Diversidad por región/municipio")
        List<RegionDiversity> diversityByRegion,

        @Schema(description = "Estado de cumplimiento de meta ESG")
        EsgCompliance esgCompliance,

        @Schema(description = "Distribución por nivel de experiencia")
        List<ExperienceLevelBreakdown> experienceBreakdown,

        @Schema(description = "Distribución por género autodeclarado")
        List<GenderBreakdown> genderBreakdown,

        @Schema(description = "Pipeline del proceso de reclutamiento por estado")
        PipelineBreakdown pipeline

) {

    @Schema(description = "Desglose de un badge de diversidad")
    public record BadgeBreakdown(
            @Schema(description = "Nombre del badge", example = "TALENTO_REGIONAL")
            String badge,
            @Schema(description = "Cantidad de candidatos con este badge", example = "120")
            long count,
            @Schema(description = "Porcentaje respecto al total de candidatos", example = "24.0")
            double percentage
    ) {}

    @Schema(description = "Diversidad agregada por región")
    public record RegionDiversity(
            @Schema(description = "Nombre del municipio", example = "Bogotá")
            String municipio,
            @Schema(description = "Total de candidatos en el municipio", example = "45")
            long total,
            @Schema(description = "Candidatos con badge de diversidad", example = "28")
            long diversity,
            @Schema(description = "Porcentaje de diversidad en el municipio", example = "62.2")
            double percentage
    ) {}

    @Schema(description = "Estado de cumplimiento de la meta ESG")
    public record EsgCompliance(
            @Schema(description = "Meta de diversidad definida por la empresa", example = "30% de shortlist con talento diverso")
            String goal,
            @Schema(description = "Porcentaje actual alcanzado", example = "65.5")
            double current,
            @Schema(description = "Estado: CUMPLIDA, EN_PROGRESO, NO_ALCANZADA", example = "CUMPLIDA")
            String status
    ) {}

    @Schema(description = "Distribución por nivel de experiencia")
    public record ExperienceLevelBreakdown(
            @Schema(description = "Nivel de experiencia", example = "JUNIOR")
            String level,
            @Schema(description = "Cantidad de candidatos", example = "200")
            long count,
            @Schema(description = "Porcentaje del total", example = "40.0")
            double percentage
    ) {}

    @Schema(description = "Distribución por género autodeclarado")
    public record GenderBreakdown(
            @Schema(description = "Género autodeclarado o 'No declarado'", example = "Femenino")
            String gender,
            @Schema(description = "Cantidad de candidatos", example = "150")
            long count,
            @Schema(description = "Porcentaje del total", example = "30.0")
            double percentage
    ) {}

    @Schema(description = "Pipeline del proceso de reclutamiento")
    public record PipelineBreakdown(
            @Schema(description = "Candidatos contactados", example = "15")
            long contactados,
            @Schema(description = "Candidatos interesados", example = "8")
            long interesados,
            @Schema(description = "Candidatos en entrevista", example = "5")
            long entrevista,
            @Schema(description = "Candidatos con oferta", example = "2")
            long oferta,
            @Schema(description = "Candidatos descartados", example = "3")
            long descartados,
            @Schema(description = "Total de procesos activos", example = "33")
            long total
    ) {}
}
