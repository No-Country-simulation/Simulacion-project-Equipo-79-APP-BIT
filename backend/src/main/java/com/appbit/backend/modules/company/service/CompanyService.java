package com.appbit.backend.modules.company.service;

import com.appbit.backend.modules.company.dto.CompanyRequest;
import com.appbit.backend.modules.company.entity.Company;
import com.appbit.backend.modules.company.mapper.CompanyMapper;
import com.appbit.backend.modules.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    /**
     * Registra una nueva empresa aplicando validaciones de negocio.
     */
    public Company register(CompanyRequest company) {

        if (companyRepository.existsByNameIgnoreCase(company.name())) {
            throw new IllegalArgumentException("Ya existe una empresa registrada con ese nombre.");
        }

        try {
            return companyRepository.save(companyMapper.toEntity(company));
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Ya existe una empresa registrada con ese nombre.");
        }
    }

    /**
     * Busca una empresa por su ID con validación.
     */
    @Transactional(readOnly = true)
    public Company findById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Empresa no encontrada con el ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Company> findAll() {
        return companyRepository.findAll();
    }
}
