package com.appbit.backend.modules.insights.model;

public record CoverageZone(
        String ecgi,       // Identificador único de la antena (Obligatorio String para evitar corrupción)
        String cluster,    // Zona geográfica de la antena
        String municipio,  // Municipio de la antena
        double latitude,   // lat
        double longitude   // lon
) {}
