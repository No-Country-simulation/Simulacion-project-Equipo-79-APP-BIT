package com.appbit.backend.modules.recruitment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Solicitud para crear o actualizar un proceso de reclutamiento")
public record RecruitmentRequest(

                @NotNull(message = "El ID de la vacante es obligatorio") @Schema(description = "ID de la vacante", example = "1", requiredMode = Schema.RequiredMode.REQUIRED) Long jobId,

                @NotNull(message = "El ID del candidato es obligatorio") @Schema(description = "ID del candidato", example = "42", requiredMode = Schema.RequiredMode.REQUIRED) Long candidateId,

                @Schema(description = "Notas del reclutador", example = "Candidato con buen perfil técnico") String recruiterNotes,

                @Schema(description = "Razón de la decisión (requerido al descartar)", example = "No cumple experiencia mínima") String decisionReason) {
}
