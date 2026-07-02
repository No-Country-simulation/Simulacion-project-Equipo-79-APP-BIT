package com.appbit.backend.modules.dashboard.controller;

import com.appbit.backend.modules.dashboard.dto.DashboardEsgResponse;
import com.appbit.backend.modules.dashboard.service.DashboardEsgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard ESG", description = "API de métricas de diversidad e inclusión para el dashboard ESG")
public class DashboardEsgController {

    private final DashboardEsgService dashboardEsgService;

    @Operation(
            summary = "Obtener métricas ESG del dashboard",
            description = "Retorna métricas agregadas de diversidad e inclusión. " +
                    "Opcionalmente filtradas por jobId para mostrar métricas del scope de una vacante."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Métricas ESG obtenidas exitosamente",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = DashboardEsgResponse.class))
    )
    @GetMapping("/esg")
    public DashboardEsgResponse getEsgMetrics(
            @RequestParam(required = false) Long jobId) {
        return dashboardEsgService.getEsgMetrics(jobId);
    }
}
