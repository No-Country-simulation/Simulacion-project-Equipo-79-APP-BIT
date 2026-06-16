package com.appbit.backend.modules.agent.service;

import com.appbit.backend.modules.agent.dto.AnonymousCandidateResponse;
import com.appbit.backend.modules.agent.dto.JobMatchRequest;
import com.appbit.backend.modules.agent.dto.MatchResultResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatchingAgentService {

    /**
     * Ejecuta el proceso de evaluación y emparejamiento usando los DTOs oficiales.
     */
    public List<MatchResultResponse> executeMatching(JobMatchRequest job, List<AnonymousCandidateResponse> candidates) {

        // Simulación de latencia mínima del LLM
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Mock oficial alineado a la estructura de la Semana 1
        return List.of(
                new MatchResultResponse(
                        101L,
                        92,
                        List.of("Java", "Spring Boot", "SQL"),
                        "El candidato cubre el núcleo del stack requerido y cuenta con sólida experiencia en desarrollo backend.",
                        "DIVERSITY_LEADER"
                ),
                new MatchResultResponse(
                        102L,
                        75,
                        List.of("Java", "Hibernate"),
                        "Posee las bases de persistencia solicitadas, pero requiere reforzar conocimientos en sistemas distribuidos.",
                        ""
                )
        );
    }
}