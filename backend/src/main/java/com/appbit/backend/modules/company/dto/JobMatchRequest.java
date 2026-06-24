package com.appbit.backend.modules.company.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * DTO (Data Transfer Object) para la solicitud de búsqueda de coincidencias (matching) de candidatos.
 * <p>
 * Contiene los datos de una oferta de trabajo para la cual se desea encontrar
 * candidatos compatibles mediante el motor de matching con IA.
 * Las anotaciones de validación de Jakarta impiden procesar datos incompletos o basura.
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

        @NotBlank(message = "El título de la vacante es obligatorio")
        @Schema(
                description = "Título del puesto de trabajo",
                example = "Desarrollador Backend Senior",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 255
        )
        String title,

        @NotEmpty(message = "La lista de habilidades técnicas no puede estar vacía")
        @Schema(
                description = "Lista de habilidades técnicas requeridas para el puesto",
                example = "[\"Java\", \"Spring Boot\", \"Microservicios\", \"PostgreSQL\", \"Docker\"]",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @ArraySchema(schema = @Schema(type = "string", example = "Java"))
        List<String> skills,

        @NotBlank(message = "El nivel de experiencia es obligatorio")
        @Schema(
                description = "Nivel de experiencia requerido para el puesto",
                example = "SENIOR",
                requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"JUNIOR", "MID", "SENIOR", "LEAD"}
        )
        String experienceLevel,

        @NotBlank(message = "El municipio destino es obligatorio")
        @Schema(
                description = "Municipio o región geográfica donde se ubica el puesto de trabajo",
                example = "Bogotá",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 100
        )
        String region

) {}