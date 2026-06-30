package com.appbit.backend.modules.recruitment.service;

import com.appbit.backend.modules.recruitment.dto.RecruitmentRequest;
import com.appbit.backend.modules.recruitment.dto.RecruitmentResponse;
import com.appbit.backend.modules.recruitment.entity.RecruitmentProcess;
import com.appbit.backend.modules.recruitment.entity.RecruitmentStatus;
import com.appbit.backend.modules.recruitment.repository.RecruitmentProcessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class RecruitmentService {

    private final RecruitmentProcessRepository repository;

    public RecruitmentResponse initiateContact(RecruitmentRequest request) {
        if (request.jobId() == null || request.candidateId() == null) {
            throw new IllegalArgumentException("jobId y candidateId son obligatorios.");
        }

        repository.findByJobIdAndCandidateId(request.jobId(), request.candidateId())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Ya existe un proceso para este candidato en esta vacante.");
                });

        RecruitmentProcess process = RecruitmentProcess.builder()
                .jobId(request.jobId())
                .candidateId(request.candidateId())
                .status(RecruitmentStatus.CONTACTADO)
                .contactedAt(LocalDateTime.now())
                .recruiterNotes(request.recruiterNotes())
                .build();

        RecruitmentProcess saved = repository.save(process);
        log.info("Proceso de reclutamiento iniciado: job={}, candidate={}", saved.getJobId(), saved.getCandidateId());
        return RecruitmentResponse.from(saved);
    }

    public RecruitmentResponse updateStatus(Long id, RecruitmentStatus newStatus, String decisionReason) {
        RecruitmentProcess process = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Proceso no encontrado con ID: " + id));

        if (newStatus == RecruitmentStatus.DESCARTADO && (decisionReason == null || decisionReason.isBlank())) {
            throw new IllegalArgumentException("Se requiere una razón objetiva para descartar al candidato.");
        }

        process.setStatus(newStatus);
        if (decisionReason != null && !decisionReason.isBlank()) {
            process.setDecisionReason(decisionReason);
        }

        RecruitmentProcess updated = repository.save(process);
        log.info("Estado actualizado: proceso={}, status={}", id, newStatus);
        return RecruitmentResponse.from(updated);
    }

    public RecruitmentResponse updateNotes(Long id, String notes) {
        RecruitmentProcess process = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Proceso no encontrado con ID: " + id));
        process.setRecruiterNotes(notes);
        return RecruitmentResponse.from(repository.save(process));
    }

    @Transactional(readOnly = true)
    public List<RecruitmentResponse> findByJob(Long jobId) {
        return repository.findByJobId(jobId).stream()
                .map(RecruitmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public RecruitmentResponse findById(Long id) {
        return repository.findById(id)
                .map(RecruitmentResponse::from)
                .orElseThrow(() -> new NoSuchElementException("Proceso no encontrado con ID: " + id));
    }
}
