package com.appbit.backend.modules.candidate.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * DTO (Data Transfer Object) para la respuesta de un candidato anonimizado.
 * <p>
 * Representa la información de un candidato que se devuelve en las respuestas
 * de la API, protegiendo su identidad personal (no incluye nombre, email, etc.)
 * pero manteniendo los datos relevantes para el proceso de matching:
 * habilidades, experiencia, ubicación y distintivos de diversidad.
 * </p>
 *
 * @see com.appbit.backend.modules.candidate.entity.Candidate
 */
@Schema(
        name = "AnonymousCandidateResponse",
        description = "Objeto que representa la respuesta de un candidato con datos anonimizados. " +
                "Protege la identidad personal del candidato mientras mantiene la información relevante para el matching: " +
                "habilidades, nivel de experiencia, ubicación geográfica y distintivos de diversidad."
)
public record AnonymousCandidateResponse(
        @Schema(
                description = "Identificador único del candidato en el sistema",
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "1"
        )
        Long candidateId,

        @Schema(
                description = "Lista de habilidades técnicas y blandas del candidato",
                example = "[\"Java\", \"Spring Boot\", \"Liderazgo\", \"Comunicación efectiva\"]",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @ArraySchema(schema = @Schema(type = "string", example = "Java"))
        List<String> skills,

        @Schema(
                description = "Nivel de experiencia laboral del candidato",
                example = "SENIOR",
                requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"JUNIOR", "MID", "SENIOR", "LEAD"}
        )
        String experienceLevel,

        @Schema(
                description = "Región o municipio de residencia del candidato",
                example = "Bogotá",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 100
        )
        String region,

        @Schema(
                description = "Latitud de la ubicación del candidato (coordenada geográfica)",
                example = "4.5981",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "-90",
                maximum = "90"
        )
        double latitude,

        @Schema(
                description = "Longitud de la ubicación del candidato (coordenada geográfica)",
                example = "-74.0758",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "-180",
                maximum = "180"
        )
        double longitude
) {}
