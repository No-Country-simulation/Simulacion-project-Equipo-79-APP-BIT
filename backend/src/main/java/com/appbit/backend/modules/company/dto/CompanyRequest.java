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
                "Contiene el nombre, sector industrial, objetivos ESG, meta de diversidad, regiones prioritarias y grupos de interés."
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
        String esgGoals,

        @Schema(
                description = "Meta de diversidad de la empresa (porcentaje de shortlist con talento diverso)",
                example = "30% de shortlist con talento diverso",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                maxLength = 500
        )
        String diversityGoal,

        @Schema(
                description = "Regiones prioritarias para la búsqueda de talento diverso",
                example = "Caribe, Pacífico, Amazonía",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                maxLength = 500
        )
        String priorityRegions,

        @Schema(
                description = "Grupos de interés para la estrategia de diversidad e inclusión",
                example = "Mujeres, jóvenes, personas con discapacidad, población rural",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                maxLength = 500
        )
        String interestGroups,

        @Schema(
                description = "Enfoque de reporte ESG (género, territorio, discapacidad, edad, etc.)",
                example = "Género, territorio, discapacidad, edad, pertenencia étnica",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                maxLength = 500
        )
        String reportFocus,

        @Schema(
                description = "NIT (Número de Identificación Tributaria) de la empresa",
                example = "900123456-7",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                maxLength = 20
        )
        String nit,

        @Schema(
                description = "Tamaño de la empresa por número de empleados",
                example = "50-200 empleados",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                maxLength = 50
        )
        String size,

        @Schema(
                description = "Ciudad principal de operación de la empresa",
                example = "Florianopolis",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                maxLength = 100
        )
        String city
) {
}
