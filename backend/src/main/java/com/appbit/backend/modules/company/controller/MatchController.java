package com.appbit.backend.modules.company.controller;

import com.appbit.backend.modules.agent.dto.MatchResultResponse;
import com.appbit.backend.modules.agent.service.MatchingAgentService;
import com.appbit.backend.modules.candidate.Service.CandidateService;
import com.appbit.backend.modules.candidate.dto.AnonymousCandidateResponse;
import com.appbit.backend.modules.company.dto.JobMatchRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Alias de {@code POST /jobs/matches} en la ruta raíz {@code /match}, tal como lo
 * pide el enunciado del MVP. Reutiliza los mismos servicios que {@link JobController}
 * para no duplicar la lógica de matching.
 */
@RestController
@RequestMapping("/match")
@Tag(name = "Match", description = "Alias de /jobs/matches en la ruta que exige el enunciado del MVP")
@RequiredArgsConstructor
public class MatchController {

    private final CandidateService candidateService;
    private final MatchingAgentService matchingAgentService;

    @PostMapping
    @Operation(
            summary = "Buscar candidatos compatibles con una oferta (alias de /jobs/matches)",
            description = "Mismo comportamiento que POST /jobs/matches: ejecuta el motor de matching " +
                    "con inteligencia artificial para encontrar candidatos compatibles con la vacante especificada."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lista de candidatos compatibles obtenida exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchResultResponse.class))
    )
    public ResponseEntity<List<MatchResultResponse>> match(@Valid @RequestBody JobMatchRequest request) {
        List<AnonymousCandidateResponse> candidates = candidateService.getCandidatesForMatching(
                request.region(),
                request.experienceLevel()
        );

        List<MatchResultResponse> results = matchingAgentService.executeMatching(request, candidates);

        return ResponseEntity.ok(results);
    }
}
