package com.appbit.backend.modules.candidate.Service;

import com.appbit.backend.modules.candidate.dto.AnonymousCandidateResponse;
import com.appbit.backend.modules.candidate.entity.Candidate;
import com.appbit.backend.modules.candidate.repository.CandidateRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CandidateService {

    private final CandidateRepository candidateRepository;

    // Inyección por constructor (recomendada)
    public CandidateService(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    /**
     * Obtiene una lista de candidatos anonimizados que coinciden con los filtros
     * de municipio y nivel de experiencia.
     * <p>
     * Este método es el punto de entrada para el módulo BE3 (agente de matching).
     * Cada candidato se transforma usando el mapper manual {@link AnonymousCandidateResponse#from(Candidate)}.
     *
     * @param municipio       región o ciudad del candidato (puede ser null para ignorar filtro)
     * @param experienceLevel nivel de experiencia (JUNIOR, MID, SENIOR, LEAD) (puede ser null)
     * @return lista de DTOs anonimizados, nunca null (vacía si no hay resultados)
     */
    public List<AnonymousCandidateResponse> getCandidatesForMatching(String municipio, String experienceLevel) {
        // 1. Obtener candidatos del repositorio según filtros
        List<Candidate> candidates;
        if (municipio != null && experienceLevel != null) {
            // Ambos filtros
            candidates = candidateRepository.findByRegionAndExperienceLevel(municipio, experienceLevel);
        } else if (municipio != null) {
            // Solo municipio
            candidates = candidateRepository.findByRegion(municipio);
        } else if (experienceLevel != null) {
            // Solo nivel de experiencia
            candidates = candidateRepository.findByExperienceLevel(experienceLevel);
        } else {
            // Sin filtros (devuelve todos, pero cuidado con el rendimiento)
            candidates = candidateRepository.findAll();
        }

        // 2. Convertir cada Candidate a AnonymousCandidateResponse usando el mapper manual
        return candidates.stream()
                .map(AnonymousCandidateResponse::from)
                .collect(Collectors.toList());
    }
}