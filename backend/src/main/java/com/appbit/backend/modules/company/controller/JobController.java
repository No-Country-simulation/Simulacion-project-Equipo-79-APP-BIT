package com.appbit.backend.modules.company.controller;

import com.appbit.backend.modules.agent.dto.MatchResultResponse;
import com.appbit.backend.modules.agent.service.MatchingAgentService;
import com.appbit.backend.modules.candidate.Service.CandidateService;
import com.appbit.backend.modules.candidate.dto.AnonymousCandidateResponse;
import com.appbit.backend.modules.company.dto.JobMatchRequest;
import com.appbit.backend.modules.company.dto.JobRequest;
import com.appbit.backend.modules.company.dto.JobResponse;
import com.appbit.backend.modules.company.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de ofertas de trabajo (jobs).
 * <p>
 * Proporciona endpoints para crear nuevas ofertas de trabajo, listar todas las ofertas
 * existentes y consultar una oferta específica por su identificador único.
 * </p>
 *
 * @see JobService
 * @see JobRequest
 * @see Job
 */
@RestController
@RequestMapping("/jobs")
@Tag(name = "Jobs", description = "API para la gestión de ofertas de trabajo publicadas por las empresas")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final MatchingAgentService matchingAgentService;
    private final CandidateService candidateService;


    /**
     * Crea una nueva oferta de trabajo en el sistema.
     * <p>
     * Este endpoint recibe los datos de la oferta (título, descripción, región,
     * habilidades requeridas, nivel de experiencia y la empresa que la publica)
     * y la persiste en la base de datos. Devuelve la entidad {@link Job} creada
     * con su identificador generado y la fecha de publicación.
     * </p>
     *
     * @param JobRequest objeto {@link JobRequest} con los datos de la oferta a crear.
     *                   Debe contener:
     *                   <ul>
     *                   <li><b>title</b>: Título del puesto (obligatorio)</li>
     *                   <li><b>description</b>: Descripción detallada del puesto (opcional)</li>
     *                   <li><b>region</b>: Región geográfica donde se ubica el puesto (opcional)</li>
     *                   <li><b>requiredSkills</b>: Lista de habilidades técnicas requeridas (opcional)</li>
     *                   <li><b>experienceLevel</b>: Nivel de experiencia requerido (obligatorio)</li>
     *                   <li><b>companyId</b>: ID de la empresa que publica la oferta (obligatorio)</li>
     *                   </ul>
     * @return la oferta de trabajo recién creada con código HTTP 201 (Created).
     */
    @PostMapping
    @Operation(
            summary = "Crear una nueva oferta de trabajo",
            description = "Crea una nueva oferta de trabajo (job) en el sistema. " +
                    "La oferta queda asociada a una empresa existente mediante el companyId. " +
                    "Devuelve la oferta con el ID generado automáticamente y la fecha de publicación.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de la oferta de trabajo a crear",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = JobRequest.class)
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Oferta de trabajo creada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = JobResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Solicitud inválida: datos de entrada incorrectos o faltantes",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empresa no encontrada: el companyId proporcionado no existe",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<JobResponse> createJob(
            @Parameter(description = "Datos de la oferta de trabajo a crear", required = true)
            @RequestBody JobRequest job) {
        JobResponse createdJob = jobService.create(job);
        return new ResponseEntity<>(createdJob, HttpStatus.CREATED);
    }

    /**
     * Obtiene la lista de todas las ofertas de trabajo registradas.
     * <p>
     * Devuelve un listado completo de todas las ofertas de trabajo existentes
     * en el sistema, sin filtros de paginación.
     * </p>
     *
     * @return lista de todas las ofertas de trabajo con código HTTP 200 (OK).
     */
    @GetMapping
    @Operation(
            summary = "Listar todas las ofertas de trabajo",
            description = "Recupera un listado completo de todas las ofertas de trabajo (jobs) " +
                    "registradas en el sistema. No aplica paginación ni filtros."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de ofertas de trabajo obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = JobResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<List<JobResponse>> getAllJobs() {
        List<JobResponse> jobs = jobService.findAll();
        return ResponseEntity.ok(jobs);
    }

    /**
     * Obtiene una oferta de trabajo por su identificador único.
     * <p>
     * Busca en la base de datos la oferta correspondiente al ID proporcionado.
     * Si no se encuentra, se devuelve un código HTTP 404 (Not Found).
     * </p>
     *
     * @param id identificador único de la oferta de trabajo a buscar.
     * @return la oferta de trabajo encontrada con código HTTP 200 (OK), o 404 si no existe.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener oferta de trabajo por ID",
            description = "Recupera los datos de una oferta de trabajo específica utilizando su identificador único. " +
                    "Devuelve un código 404 si la oferta no existe en el sistema.",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Identificador único de la oferta de trabajo",
                            required = true,
                            example = "1"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Oferta de trabajo encontrada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = JobResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Oferta de trabajo no encontrada con el ID proporcionado",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<JobResponse> getJobById(
            @Parameter(description = "ID único de la oferta de trabajo", required = true, example = "1")
            @PathVariable Long id) {
        JobResponse job = jobService.findById(id);
        return ResponseEntity.ok(job);
    }

    /**
     * Actualiza una oferta de trabajo existente.
     *
     * @param id  identificador único de la oferta a actualizar.
     * @param job datos actualizados de la oferta.
     * @return la oferta actualizada con código HTTP 200 (OK).
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una oferta de trabajo", description = "Actualiza los datos de una oferta de trabajo existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Oferta actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "404", description = "Oferta no encontrada")
    })
    public ResponseEntity<JobResponse> updateJob(
            @Parameter(description = "ID de la oferta a actualizar", required = true) @PathVariable Long id,
            @RequestBody JobRequest job) {
        JobResponse updated = jobService.updateJob(id, job);
        return ResponseEntity.ok(updated);
    }

    /**
     * Elimina una oferta de trabajo por su ID.
     *
     * @param id identificador único de la oferta a eliminar.
     * @return código HTTP 204 (No Content).
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una oferta de trabajo", description = "Elimina una oferta de trabajo y sus procesos de reclutamiento asociados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Oferta eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Oferta no encontrada")
    })
    public ResponseEntity<Void> deleteJob(
            @Parameter(description = "ID de la oferta a eliminar", required = true) @PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Busca candidatos compatibles con una oferta de trabajo usando el motor de matching con IA.
     *
     * @param request datos de la vacante (título, habilidades, nivel de experiencia y región).
     *                Todos los campos son obligatorios y son validados con {@code @Valid}.
     * @return lista de resultados de compatibilidad con código HTTP 200 (OK).
     */
    @PostMapping("/matches")
    @Operation(
            summary = "Buscar candidatos compatibles con una oferta",
            description = "Ejecuta el motor de matching con inteligencia artificial para encontrar " +
                    "candidatos compatibles con la vacante especificada. " +
                    "Los datos de la vacante se validan antes de ser procesados.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de la vacante para el matching",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = JobMatchRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Ejemplo de Vacante Backend",
                                            description = "Ejemplo de JSON válido para buscar un Desarrollador Backend MID en Florianopolis",
                                            value = "{\n" +
                                                    "  \"title\": \"Desarrollador Backend\",\n" +
                                                    "  \"skills\": [\"Java\", \"Spring Boot\"],\n" +
                                                    "  \"experienceLevel\": \"MID\",\n" +
                                                    "  \"region\": \"Florianopolis\"\n" +
                                                    "}"
                                    )
                            }
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de candidatos compatibles obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MatchResultResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Solicitud inválida: campos obligatorios faltantes o vacíos",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<List<MatchResultResponse>> findMatches(
            @Valid @RequestBody JobMatchRequest request) {

        List<AnonymousCandidateResponse> candidates = candidateService.getCandidatesForMatching(
                request.region(),
                request.experienceLevel()
        );

        List<MatchResultResponse> results = matchingAgentService.executeMatching(request, candidates);

        return ResponseEntity.ok(results);
    }
}
