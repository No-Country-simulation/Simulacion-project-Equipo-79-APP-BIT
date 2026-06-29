package com.appbit.backend.modules.company.service;

import com.appbit.backend.modules.company.dto.CompanyRequest;
import com.appbit.backend.modules.company.entity.Company;
import com.appbit.backend.modules.company.mapper.CompanyMapper;
import com.appbit.backend.modules.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    /**
     * Registra una nueva empresa aplicando validaciones básicas.
     */
    public Company register(CompanyRequest company) {
        if (company == null) {
            throw new IllegalArgumentException("La empresa no puede ser nula.");
        }
        if (company.name() == null || company.name().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la empresa es obligatorio.");
        }
        if (companyRepository.existsByNameIgnoreCase(company.name())) {
            throw new IllegalArgumentException("Ya existe una empresa registrada con ese nombre.");
        }

        return companyRepository.save(companyMapper.toEntity(company));
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
                .orElseThrow(() -> new NoSuchElementException("Empresa no encontrada con el ID: " + id));
    }
}
