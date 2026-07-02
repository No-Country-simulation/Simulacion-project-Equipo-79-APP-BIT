package com.appbit.backend.modules.recruitment.repository;

import com.appbit.backend.modules.recruitment.entity.RecruitmentProcess;
import com.appbit.backend.modules.recruitment.entity.RecruitmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecruitmentProcessRepository extends JpaRepository<RecruitmentProcess, Long> {

    List<RecruitmentProcess> findByJobId(Long jobId);

    List<RecruitmentProcess> findByCandidateId(Long candidateId);

    Optional<RecruitmentProcess> findByJobIdAndCandidateId(Long jobId, Long candidateId);

    long countByJobIdAndStatus(Long jobId, RecruitmentStatus status);
}
