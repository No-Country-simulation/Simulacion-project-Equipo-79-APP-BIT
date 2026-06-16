package com.appbit.backend.modules.agent.dto;

import java.util.List;

public record MatchResultResponse(
        Long candidateId,
        int compatibilityScore,     // Puntuación de 0 a 100
        List<String> matchingSkills, // Tecnologías en común encontradas
        String inclusionReason,     // Justificación técnica del agente
        String diversityBadge       // Distintivo si cumple con cuota regional/inclusión
) {}
