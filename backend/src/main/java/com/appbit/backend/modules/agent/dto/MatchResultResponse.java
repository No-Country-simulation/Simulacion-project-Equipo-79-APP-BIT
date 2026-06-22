package com.appbit.backend.modules.agent.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * DTO (Data Transfer Object) para la respuesta del resultado de matching de candidatos.
 * <p>
 * Representa el resultado de la evaluación de un candidato contra una oferta de trabajo
 * por parte del motor de matching con inteligencia artificial. Incluye la puntuación
 * de compatibilidad, las habilidades coincidentes, la justificación técnica y el
 * distintivo de diversidad si aplica.
 * </p>
 *
 * @see com.appbit.backend.modules.agent.service.MatchingAgentService
 */
@Schema(
        name = "MatchResultResponse",
        description = "Objeto que representa el resultado de la evaluación de matching entre un candidato y una oferta de trabajo. " +
                "Incluye la puntuación de compatibilidad (0-100), las habilidades coincidentes, " +
                "la justificación técnica generada por el agente de IA y el distintivo de diversidad si aplica."
)
public record MatchResultResponse(
        @Schema(
                description = "Identificador único del candidato evaluado",
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "1"
        )
        Long candidateId,

        @Schema(
                description = "Puntuación de compatibilidad entre el candidato y la oferta de trabajo (escala de 0 a 100)",
                example = "85",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "0",
                maximum = "100"
        )
        int compatibilityScore,     // Puntuación de 0 a 100

        @Schema(
                description = "Lista de habilidades técnicas en común entre el candidato y la oferta de trabajo",
                example = "[\"Java\", \"Spring Boot\", \"PostgreSQL\", \"Docker\"]",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @ArraySchema(schema = @Schema(type = "string", example = "Java"))
        List<String> matchingSkills, // Tecnologías en común encontradas

        @Schema(
                description = "Justificación técnica generada por el agente de IA explicando por qué el candidato es compatible",
                example = "El candidato cuenta con 5 años de experiencia en Java y Spring Boot, coincidiendo con los requisitos técnicos del puesto. Su experiencia en microservicios y Docker alinea perfectamente con la arquitectura de la plataforma.",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 2000
        )
        String inclusionReason,     // Justificación técnica del agente

        @Schema(
                description = "Distintivo de diversidad si el candidato cumple con cuotas regionales o de inclusión. " +
                        "Valores posibles: 'REGIONAL_DIVERSITY' (cumple cuota regional), 'GENDER_DIVERSITY' (cumple cuota de género), " +
                        "'REGIONAL_AND_GENDER_DIVERSITY' (cumple ambas cuotas), o null si no aplica.",
                example = "REGIONAL_DIVERSITY",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                allowableValues = {"REGIONAL_DIVERSITY", "GENDER_DIVERSITY", "REGIONAL_AND_GENDER_DIVERSITY"}
        )
        String diversityBadge       // Distintivo si cumple con cuota regional/inclusión
) {
    /**
     * Constructor compacto para aplicar validaciones defensivas y sanitización
     * de datos antes de que el objeto sea instanciado.
     */
    public MatchResultResponse {
        // si la IA o el parser devuelven null en la lista,
        // la inicializamos como una lista vacía inmutable.
        if (matchingSkills == null) {
            matchingSkills = List.of();
        }

        // Validación de rango de negocio para el Score de compatibilidad (0 - 100)
        if (compatibilityScore < 0) {
            compatibilityScore = 0;
        } else if (compatibilityScore > 100) {
            compatibilityScore = 100;
        }
    }
}
