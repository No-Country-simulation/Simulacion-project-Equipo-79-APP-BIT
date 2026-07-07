package com.appbit.backend.modules.candidate.Service;

import com.appbit.backend.modules.candidate.dto.AnonymousCandidateResponse;
import com.appbit.backend.modules.candidate.entity.Candidate;
import com.appbit.backend.modules.candidate.repository.CandidateRepository;
import com.appbit.backend.modules.company.entity.ExperienceLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
     *
     * @param municipio       región o ciudad del candidato (puede ser null para ignorar filtro)
     * @param experienceLevel nivel de experiencia (JUNIOR, MID, SENIOR) (puede ser null)
     * @return lista de DTOs anonimizados, nunca null (vacía si no hay resultados)
     */
    /**
     * Obtiene una lista de candidatos anonimizados que coinciden con los filtros
     * de municipio y nivel de experiencia, limitado a un máximo de 35 para el LLM.
     */
    @Transactional(readOnly = true)
    public List<AnonymousCandidateResponse> getCandidatesForMatching(String municipio, ExperienceLevel experienceLevel) {

        List<Candidate> candidates = candidateRepository.findCandidatesForMatchingWithLimit(
                municipio,
                experienceLevel
        );


        candidates.forEach(c -> c.getSkills().size());

        return candidates.stream()
                .map(AnonymousCandidateResponse::from)
                .collect(Collectors.toList());
    }

    public Map<String, Long> countByMunicipio() {
        return toMunicipioMap(candidateRepository.countByMunicipio());
    }

    public Map<String, Long> countDiversityByMunicipio() {
        return toMunicipioMap(candidateRepository.countDiversityByMunicipio());
    }

    @Transactional(readOnly = true)
    public List<Candidate> findAll() {
        return candidateRepository.findAllCandidates();
    }

    @Transactional(readOnly = true)
    public List<Candidate> findByMunicipio(String municipio) {
        return candidateRepository.findByMunicipio(municipio);
    }

    @Transactional(readOnly = true)
    public Candidate findById(Long id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Candidato no encontrado con ID: " + id));
    }

    private Map<String, Long> toMunicipioMap(List<Object[]> rows) {
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put((String) row[0], ((Number) row[1]).longValue());
        }
        return map;
    }
}