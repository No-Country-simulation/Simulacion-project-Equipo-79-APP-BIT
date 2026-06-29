package com.appbit.backend.modules.company.dto;

import com.appbit.backend.modules.company.entity.ExperienceLevel;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(name = "JobResponse", description = "Respuesta con los datos de una oferta de trabajo")
public record JobResponse(
    Long id,
    String title,
    String description,
    ExperienceLevel experienceLevel,
    String region,
    List<String> skills,
    CompanySummary company,
    LocalDateTime publishedAt
) {
    @Schema(name = "CompanySummary", description = "Resumen de la empresa asociada a la oferta")
    public record CompanySummary(
        Long id,
        String name,
        String industrySector,
        String esgGoals
    ) {}
}
