package com.appbit.backend.modules.insights.dto;

public record RegionInsightResponse(
        String region,
        int candidateDensity,
        NetworkCoverage networkCoverage,
        int availableProfiles,
        double latitude,
        double longitude
) {
    public enum NetworkCoverage {
        GOOD, MEDIUM, POOR
    }
}
