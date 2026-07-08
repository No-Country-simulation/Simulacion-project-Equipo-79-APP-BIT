package com.appbit.backend.modules.company.controller;

import com.appbit.backend.modules.company.dto.CompanyRequest;
import com.appbit.backend.modules.company.entity.Company;
import com.appbit.backend.modules.company.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de empresas (companies).
 * <p>
 * Proporciona endpoints para registrar nuevas empresas y consultar
 * empresas existentes por su identificador único.
 * </p>
 *
 * @see CompanyService
 * @see CompanyRequest
 * @see Company
 */
@RestController
@RequestMapping("/companies")
@Tag(name = "Companies", description = "API para la gestión de empresas registradas en la plataforma")
public class CompanyController {

        private final CompanyService companyService;

        @Autowired
        public CompanyController(CompanyService companyService) {
                this.companyService = companyService;
        }

        /**
         * Registra una nueva empresa en el sistema.
         * <p>
         * Este endpoint recibe los datos básicos de la empresa (nombre, sector
         * industrial
         * y objetivos ESG) y los persiste en la base de datos. Devuelve la entidad
         * {@link Company} creada con su identificador generado y la fecha de creación.
         * </p>
         *
         * @param companyRequest objeto {@link CompanyRequest} con los datos de la
         *                       empresa a registrar.
         *                       Debe contener:
         *                       <ul>
         *                       <li><b>name</b>: Nombre de la empresa
         *                       (obligatorio)</li>
         *                       <li><b>industrySector</b>: Sector industrial al que
         *                       pertenece (opcional)</li>
         *                       <li><b>esgGoals</b>: Objetivos de sostenibilidad ESG
         *                       (opcional)</li>
         *                       </ul>
         * @return la empresa recién creada con código HTTP 201 (Created).
         */
        @PostMapping
        @Operation(summary = "Registrar una nueva empresa", description = "Crea una nueva empresa en el sistema con la información proporcionada. "
                        +
                        "Devuelve la entidad Company con el ID generado automáticamente y la fecha de creación.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos de la empresa a registrar", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = CompanyRequest.class))))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Empresa creada exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Company.class))),
                        @ApiResponse(responseCode = "400", description = "Solicitud inválida: datos de entrada incorrectos o faltantes", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json"))
        })
        public ResponseEntity<Company> createCompany(
                        @Parameter(description = "Datos de la empresa a registrar", required = true) @Valid @RequestBody CompanyRequest company) {
                Company createdCompany = companyService.register(company);
                return new ResponseEntity<>(createdCompany, HttpStatus.CREATED);
        }

        /**
         * Obtiene una empresa por su identificador único.
         * <p>
         * Busca en la base de datos la empresa correspondiente al ID proporcionado.
         * Si no se encuentra, se devuelve un código HTTP 404 (Not Found).
         * </p>
         *
         * @param id identificador único de la empresa a buscar.
         * @return la empresa encontrada con código HTTP 200 (OK), o 404 si no existe.
         */
        @GetMapping("/{id}")
        @Operation(summary = "Obtener empresa por ID", description = "Recupera los datos de una empresa específica utilizando su identificador único. "
                        +
                        "Devuelve un código 404 si la empresa no existe en el sistema.", parameters = {
                                        @Parameter(name = "id", description = "Identificador único de la empresa", required = true, example = "1")
                        })
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Empresa encontrada exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Company.class))),
                        @ApiResponse(responseCode = "404", description = "Empresa no encontrada con el ID proporcionado", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json"))
        })
        public ResponseEntity<Company> getCompanyById(
                        @Parameter(description = "ID único de la empresa", required = true, example = "1") @PathVariable Long id) {
                Company company = companyService.findById(id);
                return ResponseEntity.ok(company);
        }

        @GetMapping
        @Operation(summary = "Listar todas las empresas")
        public ResponseEntity<List<Company>> getAllCompanies() {
                return ResponseEntity.ok(companyService.findAll());
        }
}
