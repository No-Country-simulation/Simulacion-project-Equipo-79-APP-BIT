package com.appbit.backend.modules.company.mapper;

import com.appbit.backend.modules.company.dto.JobRequest;
import com.appbit.backend.modules.company.entity.Company;
import com.appbit.backend.modules.company.entity.Job;
import com.appbit.backend.modules.company.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobMapper {

    private final JobRepository jobRepository;

    public Job toEntity (JobRequest dto){
        Company company = (Company) jobRepository.findByCompanyId(dto.companyId());
        return Job.builder()
                .title(dto.title())
                .description(dto.description())
                .region(dto.region())
                .skills(dto.requiredSkills())
                .experienceLevel(dto.experienceLevel())
                .company(company)
                .build();
    }
}
