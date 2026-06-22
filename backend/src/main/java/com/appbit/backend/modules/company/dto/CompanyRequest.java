package com.appbit.backend.modules.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO (Data Transfer Object) para la solicitud de registro de una empresa.
 * <p>
 * Contiene los datos necesarios para crear una nueva empresa en el sistema.
 * Se utiliza como cuerpo de la petición en el endpoint POST /companies.
 * </p>
 *
 * @see com.appbit.backend.modules.company.entity.Company
 */
@Schema(
        name = "CompanyRequest",
        description = "Objeto que representa la solicitud de registro de una nueva empresa. " +
                "Contiene el nombre, sector industrial y objetivos ESG de la empresa."
)
public record CompanyRequest(
        @Schema(
                description = "Nombre de la empresa",
                example = "TechSolutions S.A.",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 255
        )
        String name,

        @Schema(
                description = "Sector industrial al que pertenece la empresa",
                example = "Tecnología",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                maxLength = 100
        )
        String industrySector,

        @Schema(
                description = "Objetivos de sostenibilidad ESG (Environmental, Social, Governance) de la empresa",
                example = "Reducir emisiones de CO2 en un 30% para 2030 y promover la diversidad de género en cargos directivos",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                maxLength = 1000
        )
        String esgGoals
) {
}
