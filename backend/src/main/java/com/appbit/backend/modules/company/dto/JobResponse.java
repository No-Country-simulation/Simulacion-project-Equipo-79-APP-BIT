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
    LocalDateTime publishedAt,
    @Schema(description = "Activa el enfoque de diversidad inclusiva", example = "true")
    boolean diversityFocusEnabled,
    @Schema(description = "Porcentaje objetivo de shortlist diversa", example = "40")
    Integer targetDiversityPercentage,
    @Schema(description = "Modalidad de trabajo", example = "Remoto")
    String modality,
    @Schema(description = "Rango salarial", example = "80000-120000 MXN")
    String salaryRange,
    @Schema(description = "Tipo de contrato", example = "Término indefinido")
    String contractType,
    @Schema(description = "Habilidades blandas requeridas")
    List<String> softSkills,
    @Schema(description = "Años mínimos de experiencia", example = "3")
    Integer experienceYears,
    @Schema(description = "Nivel educativo requerido", example = "Ingeniería en Sistemas")
    String education
) {
    @Schema(name = "CompanySummary", description = "Resumen de la empresa asociada a la oferta")
    public record CompanySummary(
        Long id,
        String name,
        String industrySector,
        String esgGoals
    ) {}
}
