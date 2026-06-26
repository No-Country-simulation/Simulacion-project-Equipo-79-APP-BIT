package com.appbit.backend.modules.candidate.Service;

import com.appbit.backend.modules.candidate.dto.AnonymousCandidateResponse;
import com.appbit.backend.modules.candidate.entity.Candidate;
import com.appbit.backend.modules.candidate.repository.CandidateRepository;
import com.appbit.backend.modules.company.entity.ExperienceLevel;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CandidateService {

    private final CandidateRepository candidateRepository;

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
     * @param experienceLevel nivel de experiencia (JUNIOR, MID, SENIOR) (puede ser null)
     * @return lista de DTOs anonimizados, nunca null (vacía si no hay resultados)
     */
    public List<AnonymousCandidateResponse> getCandidatesForMatching(String municipio, ExperienceLevel experienceLevel) {
        // 1. Obtener candidatos del repositorio según filtros
        List<Candidate> candidates;
        if (municipio != null && experienceLevel != null) {
            // Ambos filtros
            candidates = candidateRepository.findByMunicipioAndExperienceLevel(municipio, experienceLevel);
        } else if (municipio != null) {
            // Solo municipio
            candidates = candidateRepository.findByMunicipio(municipio);
        } else if (experienceLevel != null) {
            // Solo nivel de experiencia
            candidates = candidateRepository.findByExperienceLevel(experienceLevel);
        } else {
            candidates = candidateRepository.findAll();
        }
        return candidates.stream()
                .map(AnonymousCandidateResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un conteo de candidatos agrupado por municipio.
     * <p>
     * Este método devuelve un mapa donde la clave es el nombre del municipio
     * y el valor es la cantidad de candidatos en ese municipio.
     *
     * @return mapa con municipio como clave y conteo como valor
     */
    public Map<String, Long> countByMunicipio() {
        List<Object[]> results = candidateRepository.countByMunicipio();
        Map<String, Long> countMap = new HashMap<>();
        for (Object[] row : results) {
            String municipio = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            countMap.put(municipio, count);
        }
        return countMap;
    }
}