package com.appbit.backend.modules.company.mapper;

import com.appbit.backend.modules.company.dto.JobRequest;
import com.appbit.backend.modules.company.dto.JobResponse;
import com.appbit.backend.modules.company.entity.Company;
import com.appbit.backend.modules.company.entity.Job;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JobMapper {

    public Job toEntity(JobRequest dto, Company company) {
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
                .education(dto.education())
                .build();
    }

    public JobResponse toResponse(Job job) {
        Company c = job.getCompany();
        return new JobResponse(
                job.getId(),
                job.getTitle(),
                job.getDescription(),
                job.getExperienceLevel(),
                job.getRegion(),
                job.getSkills(),
                new JobResponse.CompanySummary(c.getId(), c.getName(), c.getIndustrySector(), c.getEsgGoals()),
                job.getPublishedAt()
        );
    }

    public List<JobResponse> toResponseList(List<Job> jobs) {
        return jobs.stream().map(this::toResponse).toList();
    }
}
