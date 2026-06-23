package com.appbit.backend.modules.insights.dto;

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
@Schema(
        name = "RegionInsightResponse",
        description = "Objeto que representa los datos de análisis de una región geográfica. " +
                "Incluye densidad de candidatos, calidad de cobertura de red, cantidad de perfiles disponibles " +
                "y coordenadas geográficas para visualización en mapas."
)
public record RegionInsightResponse(
        @Schema(
                description = "Nombre de la región geográfica",
                example = "Bogotá",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 100
        )
        String region,

        @Schema(
                description = "Densidad de candidatos en la región (cantidad de candidatos por cada 1000 habitantes)",
                example = "15",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "0"
        )
        int candidateDensity,

        @Schema(
                description = "Calidad de la cobertura de red en la región",
                example = "GOOD",
                requiredMode = Schema.RequiredMode.REQUIRED,
                implementation = NetworkCoverage.class,
                allowableValues = {"GOOD", "MEDIUM", "POOR"}
        )
        NetworkCoverage networkCoverage,

        @Schema(
                description = "Cantidad de perfiles de candidatos disponibles en la región",
                example = "12",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "0"
        )
        int availableProfiles,

        @Schema(
                description = "Latitud de la región (coordenada geográfica)",
                example = "-4.5981",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "-90",
                maximum = "90"
        )
        double latitude,

        @Schema(
                description = "Longitud de la región (coordenada geográfica)",
                example = "-74.0758",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "-180",
                maximum = "180"
        )
        double longitude
) {
    /**
     * Enumeración que representa la calidad de la cobertura de red en una región.
     * <ul>
     * <li><b>GOOD</b>: Cobertura de red excelente, ideal para operaciones remotas y videoconferencias.</li>
     * <li><b>MEDIUM</b>: Cobertura de red aceptable, adecuada para la mayoría de las operaciones.</li>
     * <li><b>POOR</b>: Cobertura de red limitada, puede afectar la productividad en modalidad remota.</li>
     * </ul>
     */
    @Schema(
            name = "NetworkCoverage",
            description = "Enumeración que representa la calidad de la cobertura de red en una región geográfica"
    )
    public enum NetworkCoverage {
        @Schema(description = "Cobertura de red excelente, ideal para operaciones remotas y videoconferencias")
        GOOD,

        @Schema(description = "Cobertura de red aceptable, adecuada para la mayoría de las operaciones")
        MEDIUM,

        @Schema(description = "Cobertura de red limitada, puede afectar la productividad en modalidad remota")
        POOR
    }
}
