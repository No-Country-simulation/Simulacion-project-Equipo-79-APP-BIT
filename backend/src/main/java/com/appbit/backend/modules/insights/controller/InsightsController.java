package com.appbit.backend.modules.insights.controller;

import com.appbit.backend.modules.insights.dto.RegionInsightResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/insights")
public class InsightsController {

    @GetMapping
    public ResponseEntity<List<RegionInsightResponse>> getInsights() {
        // Datos mock de consistencia geográfica para LATAM según requerimientos de BE2
        List<RegionInsightResponse> mockInsights = List.of(
                new RegionInsightResponse("Bogotá", 15, RegionInsightResponse.NetworkCoverage.GOOD, 12, -4.5981, -74.0758),
                new RegionInsightResponse("São Paulo", 20, RegionInsightResponse.NetworkCoverage.GOOD, 18, -23.5505, -46.6333),
                new RegionInsightResponse("Buenos Aires", 10, RegionInsightResponse.NetworkCoverage.MEDIUM, 8, -34.6037, -58.3816),
                new RegionInsightResponse("Lima", 3, RegionInsightResponse.NetworkCoverage.POOR, 1, -12.0464, -77.0428),
                new RegionInsightResponse("Ciudad de México", 12, RegionInsightResponse.NetworkCoverage.GOOD, 10, 19.4326, -99.1332)
        );

        return ResponseEntity.ok(mockInsights);
    }
}
