package com.appbit.backend.modules.company.dto;

/**
 * DTO (Data Transfer Object) de request para solicitar matching de candidatos mediante IA.
 *
 * @see com.appbit.backend.modules.agent.service.MatchingAgentService
 */
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

// Swagger: para documentar la API automáticamente
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        name = "JobMatchRequest",
        description = "Datos de la vacante que se envían al agente de IA para encontrar candidatos compatibles. " +
                "Todos los campos son obligatorios para garantizar un matching preciso."
)
public record JobMatchRequest(

        // ── CAMPO 1: Título de la vacante ─────────────────────────────────────
        // @NotBlank rechaza: null, "", "   " (solo espacios)
        // La validación se aplica solo si el request se bindea usando @Valid/@Validated.
        // En ese caso, Spring devuelve 400 Bad Request con el mensaje definido en "message".
        @NotBlank(message = "El título de la vacante es obligatorio")
        @Schema(
                description = "Título del puesto de trabajo",
                example = "Desarrollador Backend Senior",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String title,

        // ── CAMPO 2: Lista de habilidades requeridas ──────────────────────────
        // @NotEmpty rechaza: null y listas vacías []
        // Si la empresa no especifica habilidades, el matching no tiene sentido.
        // List<String> = lista de textos. Ejemplo: ["Java", "Spring Boot", "SQL"]
        @NotEmpty(message = "La lista de habilidades técnicas no puede estar vacía")
        @ArraySchema(schema = @Schema(type = "string", example = "Java"))
        List<String> skills,

        // ── CAMPO 3: Nivel de experiencia requerido ───────────────────────────
        // Se recibe como String porque el frontend puede enviar "JUNIOR", "MID" o "SENIOR".
        // @NotBlank garantiza que no llegue vacío.
        // Nota: el agente de IA lo interpreta semánticamente, no como enum.
        @NotBlank(message = "El nivel de experiencia es obligatorio")
        @Schema(
                description = "Nivel de experiencia requerido para la vacante",
                example = "SENIOR",
                allowableValues = {"JUNIOR", "MID", "SENIOR"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String experienceLevel,

        // ── CAMPO 4: Región o municipio destino ──────────────────────────────
        // Define la zona geográfica donde se buscan candidatos.
        // Se usa para filtrar por proximidad y cobertura de red (módulo Insights).
        // Ejemplos: "Florianópolis", "São José", "Palhoça"
        @NotBlank(message = "El municipio destino es obligatorio")
        @Schema(
                description = "Municipio o región donde se necesita el candidato",
                example = "Florianópolis",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String region

) {}