package com.appbit.backend.modules.company.repository;

import com.appbit.backend.modules.company.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    @Query("SELECT j FROM Job j " +
            "JOIN FETCH j.company " +
            "LEFT JOIN FETCH j.skills " +
            "LEFT JOIN FETCH j.softSkills " +
            "WHERE j.id = :id")
    Optional<Job> findJobById(@Param("id") Long id);

    @Query("SELECT DISTINCT j FROM Job j " +
            "JOIN FETCH j.company " +
            "LEFT JOIN FETCH j.skills")
    List<Job> findAllJobs();
}
