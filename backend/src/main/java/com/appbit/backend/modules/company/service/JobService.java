package com.appbit.backend.modules.company.service;

import com.appbit.backend.modules.company.entity.Company;
import com.appbit.backend.modules.company.entity.Job;
import com.appbit.backend.modules.company.repository.CompanyRepository;
import com.appbit.backend.modules.company.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;

    public JobService(JobRepository jobRepository, CompanyRepository companyRepository) {
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
    }

    /**
     * Crea una nueva vacante de trabajo (Job) aplicando validaciones de negocio.
     */
    public Job create(Job job) {
        if (job == null) {
            throw new IllegalArgumentException("La vacante de trabajo no puede ser nula.");
        }
        if (job.getTitle() == null || job.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("El título del puesto de trabajo es obligatorio.");
        }
        if (job.getCompany() == null || job.getCompany().getId() == null) {
            throw new IllegalArgumentException("La vacante debe estar asociada a una empresa válida.");
        }

        // Validar si la empresa asociada existe en la base de datos
        Company company = companyRepository.findById(job.getCompany().getId())
                .orElseThrow(() -> new IllegalArgumentException("La empresa asociada con ID " 
                        + job.getCompany().getId() + " no existe."));

        job.setCompany(company);
        return jobRepository.save(job);
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
