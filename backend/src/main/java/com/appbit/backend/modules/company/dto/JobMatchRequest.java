package com.appbit.backend.modules.company.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * DTO (Data Transfer Object) para la solicitud de búsqueda de coincidencias (matching) de candidatos.
 * <p>
 * Contiene los datos de una oferta de trabajo para la cual se desea encontrar
 * candidatos compatibles mediante el motor de matching con IA.
 * </p>
 *
 * @see com.appbit.backend.modules.agent.service.MatchingAgentService
 */
@Schema(
        name = "JobMatchRequest",
        description = "Objeto que representa la solicitud de búsqueda de candidatos compatibles para una oferta de trabajo. " +
                "Se utiliza como entrada para el motor de matching con inteligencia artificial."
)
public record JobMatchRequest(
        @Schema(
                description = "Identificador único de la oferta de trabajo (job) para la cual se buscan candidatos",
                example = "1",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                minimum = "1"
        )
        Long jobId,

        @Schema(
                description = "Título del puesto de trabajo",
                example = "Desarrollador Backend Senior",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
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
                description = "Lista de habilidades técnicas requeridas para el puesto",
                example = "[\"Java\", \"Spring Boot\", \"Microservicios\", \"PostgreSQL\", \"Docker\"]",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @ArraySchema(schema = @Schema(type = "string", example = "Java"))
        List<String> requiredSkills,

        @Schema(
                description = "Nivel de experiencia requerido para el puesto",
                example = "SENIOR",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                allowableValues = {"JUNIOR", "MID", "SENIOR", "LEAD"}
        )
        String experienceLevel
) {}
