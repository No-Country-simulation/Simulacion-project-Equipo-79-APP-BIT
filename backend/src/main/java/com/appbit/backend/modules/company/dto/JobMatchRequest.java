package com.appbit.backend.modules.company.dto;

import com.appbit.backend.modules.company.entity.ExperienceLevel;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
@Schema(
        name = "JobMatchRequest",
        description = "Datos completos de la vacante que se envían al agente de IA para encontrar candidatos compatibles."
)
public record JobMatchRequest(

        @NotBlank(message = "El título de la vacante es obligatorio")
        @Schema(description = "Título del puesto", example = "Desarrollador Backend Senior", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,

        @Schema(description = "Descripción del puesto", example = "Buscamos un backend developer con experiencia en Java y Spring Boot")
        String description,

        @ArraySchema(schema = @Schema(type = "string", example = "Java"))
        List<String> skills,

        @NotNull(message = "El nivel de experiencia es obligatorio")
        @Schema(description = "Nivel de experiencia requerido", example = "SENIOR", allowableValues = {"JUNIOR", "MID", "SENIOR"}, requiredMode = Schema.RequiredMode.REQUIRED)
        ExperienceLevel experienceLevel,

        @Schema(description = "Región donde se necesita el candidato", example = "Florianópolis")
        String region,

        @ArraySchema(schema = @Schema(type = "string", example = "Comunicación"))
        @Schema(description = "Habilidades blandas requeridas")
        List<String> softSkills,

        @Schema(description = "Modalidad de trabajo", example = "Remoto")
        String modality,

        @Schema(description = "Rango salarial", example = "3.000.000 - 5.000.000 COP")
        String salaryRange,

        @Schema(description = "Tipo de contrato", example = "Término indefinido")
        String contractType,

        @Schema(description = "Años mínimos de experiencia", example = "3")
        Integer experienceYears,

        @Schema(description = "Nivel educativo requerido", example = "Ingeniería en Sistemas")
        String education,

        @Schema(description = "Industria de la empresa", example = "Tecnología")
        String companyIndustry,

        @Schema(description = "Metas ESG de la empresa", example = "Aumentar diversidad de género en posiciones técnicas")
        String companyEsgGoals

) {
    public JobMatchRequest {
        if (skills == null) skills = List.of();
        if (softSkills == null) softSkills = List.of();
        if (region != null && region.isBlank()) region = null;
        if (modality != null && modality.isBlank()) modality = null;
        if (salaryRange != null && salaryRange.isBlank()) salaryRange = null;
        if (contractType != null && contractType.isBlank()) contractType = null;
        if (education != null && education.isBlank()) education = null;
    }
}