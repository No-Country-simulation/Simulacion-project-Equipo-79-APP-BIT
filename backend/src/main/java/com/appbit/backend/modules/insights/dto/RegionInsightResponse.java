package com.appbit.backend.modules.insights.dto;

import com.appbit.backend.modules.insights.model.NetworkCoverage;
import io.swagger.v3.oas.annotations.media.Schema;

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
        int candidateDensity,          // Cuántos candidatos hay ahí (de BE2)
        NetworkCoverage networkCoverage, // Calidad de red (calculada en BE4)
        int availableProfiles,         // Mismo que candidateDensity por ahora
        double latitude,               // Latitud representativa del municipio
        double longitude               // Longitud representativa del municipio
) {}