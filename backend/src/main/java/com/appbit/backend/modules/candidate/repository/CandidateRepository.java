package com.appbit.backend.modules.candidate.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.appbit.backend.modules.candidate.entity.Candidate;
import com.appbit.backend.modules.company.entity.ExperienceLevel;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
        @EntityGraph(attributePaths = { "skills" })
        List<Candidate> findByMunicipio(String municipio);

        @Query(value = "SELECT municipio, COUNT(*) FROM candidate GROUP BY municipio", nativeQuery = true)
        List<Object[]> countByMunicipio();

        @Query(value = "SELECT municipio, COUNT(*) FROM candidate WHERE diversity_badge IS NOT NULL AND diversity_badge != '' GROUP BY municipio", nativeQuery = true)
        List<Object[]> countDiversityByMunicipio();

        @EntityGraph(attributePaths = { "skills" })
        @Query("SELECT c FROM Candidate c WHERE " +
                        "(:municipio IS NULL OR c.municipio = :municipio) AND " +
                        "(:experienceLevel IS NULL OR c.experienceLevel = :experienceLevel)")
        List<Candidate> findCandidatesForMatchingWithLimit(
                        @Param("municipio") String municipio,
                        @Param("experienceLevel") ExperienceLevel experienceLevel,
                        Pageable pageable);

        @Query("SELECT DISTINCT c FROM Candidate c LEFT JOIN FETCH c.skills")
        List<Candidate> findAllCandidates();

}
