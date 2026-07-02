package com.appbit.backend.modules.candidate.controller;

import com.appbit.backend.modules.candidate.Service.CandidateService;
import com.appbit.backend.modules.candidate.dto.CandidateFullProfileResponse;
import com.appbit.backend.modules.candidate.entity.Candidate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de candidatos.
 * <p>
 * Proporciona endpoints para consultar candidatos registrados en el sistema.
 * Soporta la obtención de todos los candidatos o el filtrado por municipio.
 * </p>
 *
 * @see CandidateService
 * @see Candidate
 */
@RestController
@RequestMapping("/candidates")
@RequiredArgsConstructor
@Tag(name = "Candidates", description = "API para la consulta de candidatos registrados en la plataforma")
public class CandidateController {

    private final CandidateService candidateService;

    /**
     * Obtiene la lista de candidatos, con la opción de filtrar por municipio.
     * <p>
     * Si se proporciona el parámetro {@code municipio}, se devuelven únicamente
     * los candidatos pertenecientes a ese municipio. Si no se proporciona,
     * se devuelve la lista completa de candidatos.
     * </p>
     *
     * @param municipio (opcional) nombre del municipio para filtrar los candidatos.
     *                  Ejemplo: "Bogotá", "Medellín", "São Paulo".
     * @return lista de candidatos que coinciden con el filtro, o la lista completa
     * si no se especifica municipio. Código HTTP 200 (OK).
     */
    @GetMapping
    @Operation(
            summary = "Listar candidatos",
            description = "Recupera un listado de candidatos registrados en el sistema. " +
                    "Si se proporciona el parámetro 'municipio', filtra los resultados por ese municipio. " +
                    "Si no se proporciona, devuelve todos los candidatos.",
            parameters = {
                    @Parameter(
                            name = "municipio",
                            description = "Filtro opcional por municipio. Ejemplo: 'Bogotá', 'Medellín'",
                            required = false,
                            example = "Bogotá"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de candidatos obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Candidate.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Solicitud inválida: parámetros de consulta incorrectos",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Page<Candidate>> findAll(
            @Parameter(description = "Filtro opcional por municipio", required = false, example = "Bogotá")
            @RequestParam(required = false) String municipio,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (municipio != null && !municipio.isEmpty()) {
            return ResponseEntity.ok(candidateService.findByMunicipio(municipio, page,size));
        }
        return ResponseEntity.ok(candidateService.findAllPaginated(page,size));
    }

    /**
     * Obtiene un candidato por su identificador único.
     * <p>
     * Busca en la base de datos el candidato correspondiente al ID proporcionado.
     * Si no se encuentra, se devuelve un código HTTP 404 (Not Found).
     * </p>
     *
     * @param id identificador único del candidato a buscar.
     * @return el candidato encontrado con código HTTP 200 (OK), o 404 si no existe.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener candidato por ID",
            description = "Recupera los datos de un candidato específico utilizando su identificador único. " +
                    "Devuelve un código 404 si el candidato no existe en el sistema.",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Identificador único del candidato",
                            required = true,
                            example = "1"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Candidato encontrado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Candidate.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Candidato no encontrado con el ID proporcionado",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Candidate> findById(
            @Parameter(description = "ID único del candidato", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(candidateService.findById(id));
    }

    @GetMapping("/{id}/full-profile")
    @Operation(
            summary = "Obtener perfil completo del candidato",
            description = "Retorna todos los datos del candidato incluyendo información de diversidad autodeclarada."
    )
    public ResponseEntity<CandidateFullProfileResponse> getFullProfile(@PathVariable Long id) {
        Candidate candidate = candidateService.findById(id);
        return ResponseEntity.ok(CandidateFullProfileResponse.from(candidate));
    }
}
