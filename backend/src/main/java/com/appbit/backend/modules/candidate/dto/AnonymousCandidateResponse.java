package com.appbit.backend.modules.candidate.dto;

import com.appbit.backend.modules.candidate.entity.Candidate;
import com.appbit.backend.modules.company.entity.ExperienceLevel;
import com.appbit.backend.core.util.TranslationHelper;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        name = "AnonymousCandidateResponse",
        description = "Candidato con datos anonimizados. Cumple con la LGPD y principios anti-sesgo. " +
                "No expone nombre, email, género ni ningún dato personal identificable."
)
public record AnonymousCandidateResponse(

        @Schema(description = "Identificador del candidato en el sistema", example = "42")
        Long candidateId,

        @ArraySchema(schema = @Schema(type = "string", example = "Java"))
        List<String> skills,

        @Schema(
                description = "Nivel de experiencia laboral del candidato",
                example = "SENIOR",
                allowableValues = {"JUNIOR", "MID", "SENIOR"}
        )
        ExperienceLevel experienceLevel,

        @Schema(description = "Latitud geográfica del candidato", example = "-27.413")
        double latitude,

        @Schema(description = "Longitud geográfica del candidato", example = "-48.475")
        double longitude,

        @Schema(description = "Municipio o región del candidato", example = "Florianópolis")
        String municipio,

        @Schema(description = "Distintivo de diversidad existente del candidato", example = "TALENTO_REGIONAL")
        String diversityBadge

) {

    public AnonymousCandidateResponse {
        if (skills == null) {
            skills = List.of();
        }
        if (municipio == null) {
            municipio = "";
        }
        if (diversityBadge == null) {
            diversityBadge = "";
        }
    }

    public static AnonymousCandidateResponse from(Candidate candidate) {
        return new AnonymousCandidateResponse(
                candidate.getId(),
                candidate.getSkills(),
                candidate.getExperienceLevel(),
                candidate.getLatitude(),
                candidate.getLongitude(),
                candidate.getMunicipio(),
                TranslationHelper.translateBadge(candidate.getDiversityBadge())
        );
    }
}