package com.appbit.backend.modules.agent.service;

import com.appbit.backend.modules.candidate.Service.CandidateService;
import com.appbit.backend.modules.candidate.dto.AnonymousCandidateResponse;
import com.appbit.backend.modules.company.dto.JobMatchRequest;
import com.appbit.backend.modules.agent.dto.MatchResultResponse;
import com.appbit.backend.modules.company.entity.ExperienceLevel;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MatchingAgentService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final String promptTemplate;

    public MatchingAgentService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.promptTemplate = loadPromptTemplate();
    }

    private String loadPromptTemplate() {
        try (InputStream is = new ClassPathResource("prompts/matching_prompt.txt").getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error crítico: No se pudo cargar el prompt.", e);
            throw new IllegalStateException("No se encontró el archivo de prompt", e);
        }
    }

    public List<MatchResultResponse> executeMatching(JobMatchRequest job, List<AnonymousCandidateResponse> candidates) {
        if (candidates.isEmpty()) {
            log.warn("⚠️ [MATCHING] La lista de candidatos llegó VACÍA.");
            return List.of();
        }

        log.info("🤖 [MATCHING] Procesando {} candidatos. Calculando scores en Java...", candidates.size());

        try {
            List<Map<String, Object>> payloadParaIA = new ArrayList<>();

            for (AnonymousCandidateResponse c : candidates) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("candidateId", c.candidateId());

                Set<String> candidateSkills = c.skills() != null ?
                        c.skills().stream().map(String::toLowerCase).collect(Collectors.toSet()) : Set.of();

                List<String> matchingSkills = job.skills().stream()
                        .filter(s -> candidateSkills.contains(s.toLowerCase()))
                        .collect(Collectors.toList());
                item.put("matchingSkills", matchingSkills);

                double skillRatio = job.skills().isEmpty() ? 0 : (double) matchingSkills.size() / job.skills().size();
                double levelFactor = calculateLevelFactor(job.experienceLevel(), c.experienceLevel());
                int techScore = (int) Math.round(skillRatio * 100 * levelFactor);
                item.put("compatibilityScore", Math.min(100, Math.max(0, techScore)));

                int divScore = calculateDiversityScore(c.diversityBadge());
                item.put("diversityScore", divScore);
                item.put("diversityBadge", c.diversityBadge() != null ? c.diversityBadge() : null);
                item.put("municipio", c.municipio());

                payloadParaIA.add(item);
            }

            String candidatesJson = objectMapper.writeValueAsString(payloadParaIA);
            String jobSkills = String.join(", ", job.skills());
            String jobDescription = job.description() != null ? job.description() : "N/A";

            // 2. Formatear el prompt
            String finalPrompt = promptTemplate
                    .replace("{jobTitle}", job.title())
                    .replace("{jobDescription}", jobDescription)
                    .replace("{jobSkills}", jobSkills)
                    .replace("{jobExperienceLevel}", job.experienceLevel().toString())
                    .replace("{candidatesJson}", candidatesJson);

            log.info("⚙️ Enviando datos pre-calculados al LLM para generar justificaciones...");
            String llmResponse = chatClient.prompt()
                    .user(finalPrompt)
                    .call()
                    .content();

            String cleanJson = extractJsonArray(llmResponse);

            List<MatchResultResponse> results = objectMapper.readValue(
                    cleanJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, MatchResultResponse.class)
            );

            log.info("✅ Matching completado con éxito. {} candidatos mapeados.", results.size());
            return results;

        } catch (Exception e) {
            log.error("💥 [ERROR IA] Error durante la comunicación o parseo del LLM: {}", e.getMessage(), e);
            throw new RuntimeException("El agente de IA falló: " + e.getMessage());
        }
    }

    private double calculateLevelFactor(ExperienceLevel jobLevel, ExperienceLevel candidateLevel) {
        if (jobLevel == null || candidateLevel == null) return 1.0;
        if (jobLevel == ExperienceLevel.SENIOR) {
            return candidateLevel == ExperienceLevel.SENIOR ? 1.0 : (candidateLevel == ExperienceLevel.MID ? 0.8 : 0.5);
        }
        if (jobLevel == ExperienceLevel.MID) {
            return candidateLevel == ExperienceLevel.SENIOR ? 1.0 : (candidateLevel == ExperienceLevel.MID ? 1.0 : 0.7);
        }
        return 1.0;
    }

    private int calculateDiversityScore(String badge) {
        if (badge == null || badge.isBlank()) return 0;
        return switch (badge) {
            case "TALENTO_REGIONAL", "TALENTO_RURAL", "MUJER_STEM" -> 90;
            case "REGIONAL_DIVERSITY", "GENDER_DIVERSITY" -> 75;
            case "TALENTO_RECONVERSION", "TALENTO_JOVEN", "TALENTO_SENIOR" -> 60;
            default -> 50;
        };
    }

    private String extractJsonArray(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) return "[]";

        String cleaned = rawResponse.trim();
        if (cleaned.startsWith("```json")) cleaned = cleaned.substring(7);
        else if (cleaned.startsWith("```")) cleaned = cleaned.substring(3);
        if (cleaned.endsWith("```")) cleaned = cleaned.substring(0, cleaned.length() - 3);

        cleaned = cleaned.trim();
        int startIndex = cleaned.indexOf('[');
        int endIndex = cleaned.lastIndexOf(']');

        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return cleaned.substring(startIndex, endIndex + 1);
        }
        return cleaned;
    }
}