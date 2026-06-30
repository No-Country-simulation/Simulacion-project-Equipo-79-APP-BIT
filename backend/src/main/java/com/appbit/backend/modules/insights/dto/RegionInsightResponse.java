package com.appbit.backend.modules.insights.dto;

import com.appbit.backend.modules.insights.model.NetworkCoverage;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * DTO (Data Transfer Object) para la respuesta de insights de una región.
 * <p>
 * Contiene datos agregados de análisis geográfico para una región específica,
 * incluyendo densidad de candidatos, calidad de cobertura de red y cantidad
 * de perfiles disponibles. Se utiliza como respuesta en el endpoint GET /insights.
 * </p>
 *
 * @see com.appbit.backend.modules.insights.controller.InsightsController
 */
/**
 * Contrato final que consumirá el Frontend para pintar el mapa.
 * Refleja la aggregación de antenas por municipio y el cruce con candidatos.
 */
public record RegionInsightResponse(
        String municipio,
        int candidateDensity,
        NetworkCoverage networkCoverage,
        int availableProfiles,
        @Schema(description = "Cantidad de candidatos con badge de diversidad en el municipio", example = "18")
        int diversityCount,
        @Schema(description = "Porcentaje de candidatos con badge de diversidad respecto al total del municipio", example = "52.5")
        double diversityPercentage,
        @Schema(description = "Top 3 habilidades más frecuentes entre los candidatos del municipio", example = "[\"React\", \"SQL\", \"Docker\"]")
        List<String> topSkills,
        double latitude,
        double longitude
) {}