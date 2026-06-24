package com.appbit.backend.modules.company.dto;

import com.appbit.backend.modules.company.entity.ExperienceLevel;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * DTO (Data Transfer Object) para la solicitud de creación de una oferta de trabajo.
 * <p>
 * Contiene los datos necesarios para crear una nueva oferta de trabajo (job)
 * en el sistema. Se utiliza como cuerpo de la petición en el endpoint POST /jobs.
 * </p>
 *
 * @see com.appbit.backend.modules.company.entity.Job
 * @see com.appbit.backend.modules.company.entity.ExperienceLevel
 */
@Schema(
        name = "JobRequest",
        description = "Objeto que representa la solicitud de creación de una nueva oferta de trabajo. " +
                "Contiene el título, descripción, región, habilidades requeridas, nivel de experiencia y la empresa que publica la oferta."
)
public record JobRequest (
        @Schema(
                description = "Título del puesto de trabajo",
                example = "Desarrollador Backend Senior",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 255
        )
        String title,

        @Schema(
                description = "Descripción detallada del puesto, responsabilidades y requisitos",
                example = "Buscamos un desarrollador backend con experiencia en Java Spring Boot y microservicios para liderar la arquitectura de nuestra plataforma.",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                maxLength = 2000
        )
        String description,

        @Schema(
                description = "Región geográfica donde se ubica el puesto de trabajo",
                example = "Bogotá",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                maxLength = 100
        )
        String region,

        @Schema(
                description = "Lista de habilidades técnicas requeridas para el puesto",
                example = "[\"Java\", \"Spring Boot\", \"Microservicios\", \"PostgreSQL\", \"Docker\"]",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @ArraySchema(schema = @Schema(type = "string", example = "Java"))
        List<String> requiredSkills,

        @Schema(
                description = "Nivel de experiencia requerido para el puesto",
                example = "SENIOR",
                requiredMode = Schema.RequiredMode.REQUIRED,
                implementation = ExperienceLevel.class,
                allowableValues = {"JUNIOR", "MID", "SENIOR"}
        )
        ExperienceLevel experienceLevel,

        @Schema(
                description = "Identificador único de la empresa que publica la oferta de trabajo",
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "1"
        )
        Long companyId
){
}
