package com.appbit.backend.modules.agent.dto;

import java.util.List;

public record MatchResultResponse(
        Long candidateId,
        int compatibilityScore,     // Puntuación de 0 a 100
        List<String> matchingSkills, // Tecnologías en común encontradas
        String inclusionReason,     // Justificación técnica del agente
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
