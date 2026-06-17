package com.appbit.backend.modules.candidate.dto;

import java.util.List;

public record AnonymousCandidateResponse(
        Long candidateId,
        List<String> skills,
        String experienceLevel,
        String region,
        double latitude,
        double longitude
) {}