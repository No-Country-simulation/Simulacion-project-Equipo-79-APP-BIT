package com.appbit.backend.modules.candidate.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.appbit.backend.modules.candidate.entity.Candidate;
import com.appbit.backend.modules.company.entity.ExperienceLevel;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    List<Candidate> findByMunicipio(String municipio);
    List<Candidate> findByExperienceLevel(ExperienceLevel experienceLevel);
    List<Candidate> findByMunicipioAndExperienceLevel(String municipio, ExperienceLevel experienceLevel);

    @Query(value = "SELECT municipio, COUNT(*) FROM candidate GROUP BY municipio", nativeQuery = true)
    List<Object[]> countByMunicipio();

}
