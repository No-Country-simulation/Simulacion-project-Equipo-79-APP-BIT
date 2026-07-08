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
        Company company = companyRepository.findById(job.companyId())
                .orElseThrow(() -> new NoSuchElementException("La empresa no existe."));

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
        Job job = jobRepository.findJobById(id)
                .orElseThrow(() -> new NoSuchElementException("Puesto de trabajo no encontrado con el ID: " + id));
        return jobMapper.toResponse(job);
    }

    public JobResponse updateJob(Long id, JobRequest request) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Puesto de trabajo no encontrado con el ID: " + id));

        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new NoSuchElementException("La empresa no existe."));

        jobMapper.updateEntity(job, request, company);
        Job saved = jobRepository.save(job);

        return jobMapper.toResponse(saved);
    }

    public void deleteJob(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Puesto de trabajo no encontrado con el ID: " + id));

        List<com.appbit.backend.modules.recruitment.entity.RecruitmentProcess> processes = recruitmentProcessRepository
                .findByJobId(id);
        if (!processes.isEmpty()) {
            recruitmentProcessRepository.deleteAll(processes);
        }

        jobRepository.delete(job);
    }
}
