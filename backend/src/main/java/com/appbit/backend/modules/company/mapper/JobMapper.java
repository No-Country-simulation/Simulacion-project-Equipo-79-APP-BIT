package com.appbit.backend.modules.company.mapper;

import com.appbit.backend.modules.company.dto.JobRequest;
import com.appbit.backend.modules.company.entity.Company;
import com.appbit.backend.modules.company.entity.Job;
import com.appbit.backend.modules.company.repository.CompanyRepository;
import com.appbit.backend.modules.company.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobMapper {


    public Job toEntity (JobRequest dto, Company company){
        return Job.builder()
                .title(dto.title())
                .description(dto.description())
                .region(dto.region())
                .skills(dto.requiredSkills())
                .experienceLevel(dto.experienceLevel())
                .company(company)
                .diversityFocusEnabled(dto.diversityFocusEnabled() != null && dto.diversityFocusEnabled())
                .targetDiversityPercentage(dto.targetDiversityPercentage())
                .modality(dto.modality())
                .salaryRange(dto.salaryRange())
                .contractType(dto.contractType())
                .softSkills(dto.softSkills())
                .experienceYears(dto.experienceYears())
                .build();
    }
}
