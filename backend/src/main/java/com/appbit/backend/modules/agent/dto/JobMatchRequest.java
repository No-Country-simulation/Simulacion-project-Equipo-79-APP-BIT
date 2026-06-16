package com.appbit.backend.modules.agent.dto;

import java.util.List;

public record JobMatchRequest(
        Long jobId,
        String title,
        String description,
        List<String> requiredSkills,
        String experienceLevel
) {}
