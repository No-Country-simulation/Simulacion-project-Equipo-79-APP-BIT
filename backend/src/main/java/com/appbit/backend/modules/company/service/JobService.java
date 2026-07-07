package com.appbit.backend.modules.company.service;

import com.appbit.backend.modules.company.dto.JobRequest;
import com.appbit.backend.modules.company.dto.JobResponse;
import com.appbit.backend.modules.company.entity.Company;
import com.appbit.backend.modules.company.entity.Job;
import com.appbit.backend.modules.company.mapper.JobMapper;
import com.appbit.backend.modules.company.repository.CompanyRepository;
import com.appbit.backend.modules.company.repository.JobRepository;
import com.appbit.backend.modules.recruitment.repository.RecruitmentProcessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final RecruitmentProcessRepository recruitmentProcessRepository;
    private final JobMapper jobMapper;

    /**
     * Crea una nueva vacante de trabajo (Job) aplicando validaciones de negocio.
     */
    public JobResponse create(JobRequest job) {
        if (job == null) {
            throw new IllegalArgumentException("La vacante de trabajo no puede ser nula.");
        }
        if (job.title() == null || job.title().trim().isEmpty()) {
            throw new IllegalArgumentException("El título del puesto de trabajo es obligatorio.");
        }
        if (job.companyId() == null) {
            throw new IllegalArgumentException("La vacante debe estar asociada a una empresa válida.");
        }
        if (job.experienceLevel() == null) {
            throw new IllegalArgumentException("El nivel de experiencia es obligatorio.");
        }

        Company company = companyRepository.findById(job.companyId())
                .orElseThrow(() -> new IllegalArgumentException("La empresa no existe."));

        Job jobEntity = jobMapper.toEntity(job, company);
        Job saved = jobRepository.save(jobEntity);

        return jobMapper.toResponse(saved);
    }

    /**
     * Retorna todas las vacantes de trabajo registradas.
     */
    @Transactional(readOnly = true)
    public List<JobResponse> findAll() {
        List<Job> jobs = jobRepository.findAllJobs();
        return jobMapper.toResponseList(jobs);
    }

    /**
     * Busca una vacante de trabajo por su ID con validación.
     */
    @Transactional(readOnly = true)
    public JobResponse findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del puesto de trabajo no puede ser nulo.");
        }
        Job job = jobRepository.findJobById(id)
                .orElseThrow(() -> new NoSuchElementException("Puesto de trabajo no encontrado con el ID: " + id));
        return jobMapper.toResponse(job);
    }

    public JobResponse updateJob(Long id, JobRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del puesto de trabajo no puede ser nulo.");
        }
        if (request == null) {
            throw new IllegalArgumentException("Los datos de la vacante no pueden ser nulos.");
        }
        if (request.title() == null || request.title().trim().isEmpty()) {
            throw new IllegalArgumentException("El título del puesto de trabajo es obligatorio.");
        }
        if (request.companyId() == null) {
            throw new IllegalArgumentException("La vacante debe estar asociada a una empresa válida.");
        }
        if (request.experienceLevel() == null) {
            throw new IllegalArgumentException("El nivel de experiencia es obligatorio.");
        }

        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Puesto de trabajo no encontrado con el ID: " + id));

        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new IllegalArgumentException("La empresa no existe."));

        jobMapper.updateEntity(job, request, company);
        Job saved = jobRepository.save(job);

        return jobMapper.toResponse(saved);
    }

    public void deleteJob(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del puesto de trabajo no puede ser nulo.");
        }
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Puesto de trabajo no encontrado con el ID: " + id));

        List<com.appbit.backend.modules.recruitment.entity.RecruitmentProcess> processes =
                recruitmentProcessRepository.findByJobId(id);
        if (!processes.isEmpty()) {
            recruitmentProcessRepository.deleteAll(processes);
        }

        jobRepository.delete(job);
    }
}
