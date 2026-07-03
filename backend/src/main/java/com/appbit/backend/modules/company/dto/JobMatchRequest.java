package com.appbit.backend.modules.company.dto;

import com.appbit.backend.modules.company.entity.ExperienceLevel;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
@Schema(
        name = "JobMatchRequest",
        description = "Datos de la vacante que se envían al agente de IA para encontrar candidatos compatibles. " +
                "El título y el nivel de experiencia son obligatorios; skills y region son opcionales " +
                "(una vacante puede no tener skills/region cargadas todavía y aun así buscar candidatos)."
)
public record JobMatchRequest(

        @NotBlank(message = "El título de la vacante es obligatorio")
        @Schema(
                description = "Título del puesto de trabajo",
                example = "Desarrollador Backend Senior",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 255
        )
        String title,

        String description,

        @Schema(
                description = "Lista de habilidades técnicas requeridas para el puesto. " +
                        "Si viene vacía, el matching se basa únicamente en nivel de experiencia y región.",
                example = "[\"Java\", \"Spring Boot\", \"Microservicios\", \"PostgreSQL\", \"Docker\"]",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @ArraySchema(schema = @Schema(type = "string", example = "Java"))
        List<String> skills,

        @NotNull(message = "El nivel de experiencia es obligatorio")
        @Schema(
                description = "Nivel de experiencia requerido para la vacante",
                example = "SENIOR",
                allowableValues = {"JUNIOR", "MID", "SENIOR"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        ExperienceLevel experienceLevel,

        @Schema(
                description = "Municipio o región donde se necesita el candidato. Si viene vacío, no se filtra por región.",
                example = "Florianópolis",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                maxLength = 100
        )
        String region

) {
    public JobMatchRequest {
        if (skills == null) {
            skills = List.of();
        }
        if (region != null && region.isBlank()) {
            region = null;
        }
    }
}