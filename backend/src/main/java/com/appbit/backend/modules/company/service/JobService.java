package com.appbit.backend.modules.company.service;

import com.appbit.backend.modules.company.dto.JobRequest;
import com.appbit.backend.modules.company.entity.Company;
import com.appbit.backend.modules.company.entity.Job;
import com.appbit.backend.modules.company.mapper.JobMapper;
import com.appbit.backend.modules.company.repository.CompanyRepository;
import com.appbit.backend.modules.company.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final JobMapper jobMapper;

    /**
     * Crea una nueva vacante de trabajo (Job) aplicando validaciones de negocio.
     */
    public Job create(JobRequest job) {
        if (job == null) {
            throw new IllegalArgumentException("La vacante de trabajo no puede ser nula.");
        }
        if (job.title() == null || job.title().trim().isEmpty()) {
            throw new IllegalArgumentException("El título del puesto de trabajo es obligatorio.");
        }
        if (job.companyId() == null) {
            throw new IllegalArgumentException("La vacante debe estar asociada a una empresa válida.");
        }

        Company company = companyRepository.findById(job.companyId())
                .orElseThrow(() -> new IllegalArgumentException("La empresa no existe."));

        Job jobEntity = jobMapper.toEntity(job, company);

        return jobRepository.save(jobEntity);
    }

    /**
     * Retorna todas las vacantes de trabajo registradas.
     */
    @Transactional(readOnly = true)
    public List<Job> findAll() {
        return jobRepository.findAll();
    }

    /**
     * Busca una vacante de trabajo por su ID con validación.
     */
    @Transactional(readOnly = true)
    public Job findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del puesto de trabajo no puede ser nulo.");
        }
        return jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Puesto de trabajo no encontrado con el ID: " + id));
    }
}
