package com.appbit.backend.modules.candidate.dto;

import com.appbit.backend.modules.candidate.entity.Candidate;
import com.appbit.backend.modules.company.entity.ExperienceLevel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Perfil completo de un candidato con datos de diversidad autodeclarados")
public record CandidateFullProfileResponse(

        @Schema(description = "ID del candidato", example = "42")
        Long id,

        @Schema(description = "Habilidades técnicas", example = "[\"Java\", \"Spring Boot\", \"React\"]")
        List<String> skills,

        @Schema(description = "Nivel de experiencia")
        ExperienceLevel experienceLevel,

        @Schema(description = "Municipio de residencia", example = "Bogotá")
        String municipio,

        @Schema(description = "Región/cluster geográfico", example = "Centro")
        String cluster,

        @Schema(description = "Latitud", example = "4.6097")
        double latitude,

        @Schema(description = "Longitud", example = "-74.0817")
        double longitude,

        @Schema(description = "Badge de diversidad asignado")
        String diversityBadge,

        @Schema(description = "Género autodeclarado (opcional)", example = "Femenino")
        String genderOptional,

        @Schema(description = "Discapacidad autodeclarada (opcional)")
        String disabilityOptional,

        @Schema(description = "Pertenencia étnica autodeclarada (opcional)")
        String ethnicityOptional,

        @Schema(description = "Proviene de zona rural (opcional)", example = "true")
        Boolean ruralOptional,

        @Schema(description = "Estado de consentimiento para uso de datos de diversidad")
        Boolean consentStatus

) {
    public static CandidateFullProfileResponse from(Candidate c) {
        boolean hasConsent = Boolean.TRUE.equals(c.getConsentStatus());
        return new CandidateFullProfileResponse(
                c.getId(),
                c.getSkills(),
                c.getExperienceLevel(),
                c.getMunicipio(),
                c.getCluster(),
                c.getLatitude(),
                c.getLongitude(),
                c.getDiversityBadge(),
                hasConsent ? c.getGenderOptional() : null,
                hasConsent ? c.getDisabilityOptional() : null,
                hasConsent ? c.getEthnicityOptional() : null,
                hasConsent ? c.getRuralOptional() : null,
                c.getConsentStatus()
        );
    }
}
