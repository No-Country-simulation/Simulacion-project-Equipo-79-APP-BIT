package com.appbit.backend.modules.insights.controller;

import com.appbit.backend.modules.insights.dto.RegionInsightResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la consulta de insights (análisis) de regiones.
 * <p>
 * Proporciona información agregada sobre la densidad de candidatos,
 * cobertura de red y perfiles disponibles por región geográfica.
 * Estos datos son utilizados para evaluar la viabilidad de contratación
 * en diferentes zonas de LATAM.
 * </p>
 *
 * @see RegionInsightResponse
 */
@RestController
@RequestMapping("/insights")
@Tag(name = "Insights", description = "API para la consulta de análisis de regiones (densidad de candidatos, cobertura de red, perfiles disponibles)")
public class InsightsController {

    /**
     * Obtiene la lista de insights de regiones.
     * <p>
     * Devuelve datos agregados de consistencia geográfica para la región LATAM,
     * incluyendo densidad de candidatos, calidad de cobertura de red y cantidad
     * de perfiles disponibles por región. Los datos incluyen coordenadas
     * geográficas (latitud y longitud) para su visualización en mapas.
     * </p>
     *
     * @return lista de {@link RegionInsightResponse} con los datos de cada región.
     *         Código HTTP 200 (OK).
     */
    @GetMapping
    @Operation(
            summary = "Obtener insights de regiones",
            description = "Recupera datos agregados de análisis geográfico para la región LATAM. " +
                    "Cada elemento incluye: nombre de la región, densidad de candidatos, " +
                    "calidad de cobertura de red (GOOD, MEDIUM, POOR), cantidad de perfiles disponibles, " +
                    "y coordenadas geográficas (latitud y longitud) para visualización en mapas. " +
                    "Datos de consistencia geográfica según requerimientos de BE2.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lista de insights de regiones obtenida exitosamente",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = RegionInsightResponse.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error interno del servidor",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public ResponseEntity<List<RegionInsightResponse>> getInsights() {
        // Datos mock de consistencia geográfica para LATAM según requerimientos de BE2
        List<RegionInsightResponse> mockInsights = List.of(
                new RegionInsightResponse("Bogotá", 15, RegionInsightResponse.NetworkCoverage.GOOD, 12, -4.5981, -74.0758),
                new RegionInsightResponse("São Paulo", 20, RegionInsightResponse.NetworkCoverage.GOOD, 18, -23.5505, -46.6333),
                new RegionInsightResponse("Buenos Aires", 10, RegionInsightResponse.NetworkCoverage.MEDIUM, 8, -34.6037, -58.3816),
                new RegionInsightResponse("Lima", 3, RegionInsightResponse.NetworkCoverage.POOR, 1, -12.0464, -77.0428),
                new RegionInsightResponse("Ciudad de México", 12, RegionInsightResponse.NetworkCoverage.GOOD, 10, 19.4326, -99.1332)
        );

        return ResponseEntity.ok(mockInsights);
    }
}
