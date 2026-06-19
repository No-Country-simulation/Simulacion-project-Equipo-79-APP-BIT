package com.appbit.backend.modules.candidate.repository;

import com.appbit.backend.modules.candidate.entity.Candidate;
import com.appbit.backend.modules.company.entity.ExperienceLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    List<Candidate> findByMunicipioAndExperienceLevel(String municipio, ExperienceLevel experienceLevel);
    List<Candidate> findByMunicipio(String municipio);
}
