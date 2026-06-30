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
                description = "Puntuación de compatibilidad técnica (escala de 0 a 100). Mide mérito técnico puro.",
                example = "85",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "0",
                maximum = "100"
        )
        int compatibilityScore,

        @Schema(
                description = "Puntuación de aporte a diversidad (escala de 0 a 100). Independiente del score técnico.",
                example = "70",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "0",
                maximum = "100"
        )
        int diversityScore,

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
                description = "Distintivo de diversidad si el candidato cumple con criterios de inclusión. " +
                        "Valores definidos en el prompt de matching (prompts/matching_prompt.txt).",
                example = "TALENTO_REGIONAL",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                allowableValues = {"TALENTO_REGIONAL", "TALENTO_RURAL", "MUJER_STEM", "TALENTO_JOVEN",
                        "TALENTO_SENIOR", "TALENTO_RECONVERSION", "REGIONAL_DIVERSITY", "GENDER_DIVERSITY", "REGIONAL_AND_GENDER_DIVERSITY"}
        )
        String diversityBadge       // Distintivo si cumple con cuota regional/inclusión
) {
    /**
     * Constructor compacto para aplicar validaciones defensivas y sanitización
     * de datos antes de que el objeto sea instanciado.
     */
    public MatchResultResponse {
        if (matchingSkills == null) {
            matchingSkills = List.of();
        }
        if (compatibilityScore < 0) compatibilityScore = 0;
        if (compatibilityScore > 100) compatibilityScore = 100;
        if (diversityScore < 0) diversityScore = 0;
        if (diversityScore > 100) diversityScore = 100;
    }
}
