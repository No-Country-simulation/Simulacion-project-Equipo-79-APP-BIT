package com.appbit.backend.repository;

import com.appbit.backend.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    
    /**
     * Recupera todos los puestos de trabajo asociados a una empresa específica.
     */
    List<Job> findByCompanyId(Long companyId);
}
