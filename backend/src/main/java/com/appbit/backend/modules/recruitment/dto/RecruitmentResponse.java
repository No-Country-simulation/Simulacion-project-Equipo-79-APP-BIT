package com.appbit.backend.modules.recruitment.dto;

import com.appbit.backend.modules.recruitment.entity.RecruitmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Respuesta con los datos de un proceso de reclutamiento")
public record RecruitmentResponse(

        @Schema(description = "ID del proceso", example = "1")
        Long id,

        @Schema(description = "ID de la vacante", example = "1")
        Long jobId,

        @Schema(description = "ID del candidato", example = "42")
        Long candidateId,

        @Schema(description = "Estado actual del proceso")
        RecruitmentStatus status,

        @Schema(description = "Fecha de contacto")
        LocalDateTime contactedAt,

        @Schema(description = "Notas del reclutador")
        String recruiterNotes,

        @Schema(description = "Razón de la decisión")
        String decisionReason,

        @Schema(description = "Fecha de creación")
        LocalDateTime createdAt,

        @Schema(description = "Fecha de última actualización")
        LocalDateTime updatedAt
) {

    public static RecruitmentResponse from(com.appbit.backend.modules.recruitment.entity.RecruitmentProcess entity) {
        return new RecruitmentResponse(
                entity.getId(),
                entity.getJobId(),
                entity.getCandidateId(),
                entity.getStatus(),
                entity.getContactedAt(),
                entity.getRecruiterNotes(),
                entity.getDecisionReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
