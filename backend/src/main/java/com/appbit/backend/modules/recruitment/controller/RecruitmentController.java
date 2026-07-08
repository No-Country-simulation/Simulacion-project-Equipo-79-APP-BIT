package com.appbit.backend.modules.recruitment.controller;

import com.appbit.backend.modules.recruitment.dto.RecruitmentRequest;
import com.appbit.backend.modules.recruitment.dto.RecruitmentResponse;
import com.appbit.backend.modules.recruitment.entity.RecruitmentStatus;
import com.appbit.backend.modules.recruitment.service.RecruitmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recruitment")
@RequiredArgsConstructor
@Tag(name = "Recruitment Process", description = "API de seguimiento y contacto de candidatos en procesos de selección")
public class RecruitmentController {

    private final RecruitmentService recruitmentService;

    @Operation(summary = "Iniciar contacto con un candidato")
    @ApiResponse(responseCode = "201", description = "Proceso creado")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecruitmentResponse initiateContact(@Valid @RequestBody RecruitmentRequest request) {
        return recruitmentService.initiateContact(request);
    }

    @Operation(summary = "Actualizar estado de un proceso de reclutamiento")
    @PutMapping("/{id}/status")
    public RecruitmentResponse updateStatus(
            @PathVariable Long id,
            @RequestParam RecruitmentStatus status,
            @RequestParam(required = false) String decisionReason) {
        return recruitmentService.updateStatus(id, status, decisionReason);
    }

    @Operation(summary = "Actualizar notas del reclutador")
    @PutMapping("/{id}/notes")
    public RecruitmentResponse updateNotes(
            @PathVariable Long id,
            @RequestBody @Schema(description = "Notas del reclutador", example = "{\"notes\": \"Perfil prometedor\"}") java.util.Map<String, String> body) {
        return recruitmentService.updateNotes(id, body.get("notes"));
    }

    @Operation(summary = "Listar procesos por vacante")
    @GetMapping
    public List<RecruitmentResponse> findByJob(@RequestParam Long jobId) {
        return recruitmentService.findByJob(jobId);
    }

    @Operation(summary = "Obtener proceso por ID")
    @GetMapping("/{id}")
    public RecruitmentResponse findById(@PathVariable Long id) {
        return recruitmentService.findById(id);
    }
}
