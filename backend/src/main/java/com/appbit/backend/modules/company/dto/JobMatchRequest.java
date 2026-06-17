package com.appbit.backend.modules.company.dto;

import java.util.List;

public record JobMatchRequest(
        Long jobId,
        String title,
        String description,
        List<String> requiredSkills,
        String experienceLevel
) {}
