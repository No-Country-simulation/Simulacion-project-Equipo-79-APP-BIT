package com.appbit.backend.modules.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
@Schema(
        name = "JobMatchRequest",
        description = "Datos de la vacante que se envían al agente de IA para encontrar candidatos compatibles. " +
                "Todos los campos son obligatorios para garantizar un matching preciso."
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
                description = "Nivel de experiencia requerido para la vacante",
                example = "SENIOR",
                allowableValues = {"JUNIOR", "MID", "SENIOR", "LEAD"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String experienceLevel,

        @NotBlank(message = "El municipio destino es obligatorio")
        @Schema(
                description = "Municipio o región donde se necesita el candidato",
                example = "Florianópolis",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 100
        )
        String region

) {}