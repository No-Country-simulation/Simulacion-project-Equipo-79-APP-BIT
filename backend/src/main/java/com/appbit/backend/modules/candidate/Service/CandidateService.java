package com.appbit.backend.modules.candidate.Service;

import com.appbit.backend.modules.candidate.dto.AnonymousCandidateResponse;
import com.appbit.backend.modules.candidate.entity.Candidate;
import com.appbit.backend.modules.candidate.repository.CandidateRepository;
import com.appbit.backend.modules.company.entity.ExperienceLevel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CandidateService {

    private final CandidateRepository candidateRepository;

    public CandidateService(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    public List<AnonymousCandidateResponse> getCandidatesForMatching(String municipio, String experienceLevel) {
        List<Candidate> candidates;
        ExperienceLevel level = experienceLevel != null ? ExperienceLevel.valueOf(experienceLevel) : null;
        if (municipio != null && level != null) {
            candidates = candidateRepository.findByMunicipioAndExperienceLevel(municipio, level);
        } else if (municipio != null) {
            candidates = candidateRepository.findByMunicipio(municipio);
        } else if (level != null) {
            candidates = candidateRepository.findByExperienceLevel(level);
        } else {
            candidates = candidateRepository.findAll();
        }
        return candidates.stream()
                .map(AnonymousCandidateResponse::from)
                .collect(Collectors.toList());
    }
}