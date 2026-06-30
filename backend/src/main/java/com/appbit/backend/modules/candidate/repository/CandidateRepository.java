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

    @Query(value = "SELECT municipio, COUNT(*) FROM candidate WHERE diversity_badge IS NOT NULL AND diversity_badge != '' GROUP BY municipio", nativeQuery = true)
    List<Object[]> countDiversityByMunicipio();

    @Query(value = "SELECT COUNT(*) FROM candidate WHERE diversity_badge IS NOT NULL AND diversity_badge != ''", nativeQuery = true)
    long countWithDiversityBadge();

    @Query(value = "SELECT diversity_badge, COUNT(*) FROM candidate WHERE diversity_badge IS NOT NULL GROUP BY diversity_badge ORDER BY COUNT(*) DESC", nativeQuery = true)
    List<Object[]> badgeBreakdown();

}
