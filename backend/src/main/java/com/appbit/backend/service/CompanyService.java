package com.appbit.backend.service;

import com.appbit.backend.entity.Company;
import com.appbit.backend.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    /**
     * Registra una nueva empresa aplicando validaciones básicas.
     */
    public Company register(Company company) {
        if (company == null) {
            throw new IllegalArgumentException("La empresa no puede ser nula.");
        }
        if (company.getName() == null || company.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la empresa es obligatorio.");
        }
        // Validar si ya existe una empresa con ese nombre
        boolean exists = companyRepository.findAll().stream()
                .anyMatch(c -> c.getName().equalsIgnoreCase(company.getName()));
        if (exists) {
            throw new IllegalArgumentException("Ya existe una empresa registrada con ese nombre.");
        }

        return companyRepository.save(company);
    }

    /**
     * Busca una empresa por su ID con validación.
     */
    @Transactional(readOnly = true)
    public Company findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la empresa no puede ser nulo.");
        }
        return companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada con el ID: " + id));
    }
}
