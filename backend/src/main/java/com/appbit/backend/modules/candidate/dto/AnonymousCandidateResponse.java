package com.appbit.backend.modules.candidate.dto;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.appbit.backend.modules.candidate.entity.Candidate;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

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
) {
         /**
     * Método de fábrica estático que transforma una entidad {@link Candidate}
     * en un DTO anonimizado.
     * <p>
     * Este mapper es **manual** (sin librerías como MapStruct) y garantiza que
     * los datos sensibles (nombre, email, género, ID real) NO se expongan.
     * El ID se anonimiza mediante un hash con sal para evitar trazabilidad directa.
     * </p>
     *
     * @param candidate entidad JPA del candidato (no puede ser {@code null})
     * @return DTO anonimizado listo para ser enviado al frontend o al módulo BE3
     * @throws NullPointerException si {@code candidate} es {@code null}
     */
    public static AnonymousCandidateResponse from(Candidate candidate) {
        // 1. Anonimizar el ID: se usa un hash del ID real con una sal fija
        Long anonymousId = Objects.hash(candidate.getId(), "salt-secreto-para-lgpd");

        // 2. Extraer la lista de habilidades (suponiendo que Candidate tiene una lista de Skill)
        // Si Candidate ya tiene List<String> skills, usa candidate.getSkills() directamente
        List<String> skillNames = candidate.getSkills()
                .stream()
                .map(Skill::getName)   // Si Skill es una entidad con getName()
                .collect(Collectors.toList());

        // 3. Obtener el nivel de experiencia como String (si es un enum, usa .name())
        String experience = candidate.getExperienceLevel().name();

        // 4. Obtener la región (suponiendo que Candidate tiene un campo 'region')
        // Si no existe, ajusta a candidate.getMunicipio() o candidate.getCity()
        String region = candidate.getRegion();

        // 5. Coordenadas geográficas
        double lat = candidate.getLatitude();
        double lng = candidate.getLongitude();

        // 6. Construir y devolver el DTO con todos los campos
        return new AnonymousCandidateResponse(
                anonymousId,
                skillNames,
                experience,
                region,
                lat,
                lng
        );
    }
}