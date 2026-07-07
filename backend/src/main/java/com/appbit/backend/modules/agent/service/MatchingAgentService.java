package com.appbit.backend.modules.agent.service;

import com.appbit.backend.modules.candidate.Service.CandidateService;
import com.appbit.backend.modules.candidate.dto.AnonymousCandidateResponse;
import com.appbit.backend.modules.company.dto.JobMatchRequest;
import com.appbit.backend.modules.company.entity.ExperienceLevel;
import com.appbit.backend.modules.agent.dto.MatchResultResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

        log.info("🤖 [MATCHING] Enviando {} candidatos al LLM...", candidates.size());

        try {
            // 1. Preparar datos
            String jobSkills = String.join(", ", job.skills());
            String jobSoftSkills = String.join(", ", job.softSkills() != null ? job.softSkills() : List.of());
            String candidatesJson = objectMapper.writeValueAsString(candidates);
            String jobDescription = job.description() != null ? job.description() : "N/A";

            // 2. Formatear el prompt con todos los datos disponibles
            String finalPrompt = promptTemplate
                    .replace("{jobTitle}", job.title())
                    .replace("{jobDescription}", jobDescription)
                    .replace("{jobSkills}", jobSkills)
                    .replace("{jobSoftSkills}", jobSoftSkills)
                    .replace("{jobExperienceLevel}", String.valueOf(job.experienceLevel()))
                    .replace("{jobExperienceYears}", job.experienceYears() != null ? String.valueOf(job.experienceYears()) : "No especificado")
                    .replace("{jobEducation}", job.education() != null ? job.education() : "No especificado")
                    .replace("{jobModality}", job.modality() != null ? job.modality() : "No especificado")
                    .replace("{jobSalaryRange}", job.salaryRange() != null ? job.salaryRange() : "No especificado")
                    .replace("{jobContractType}", job.contractType() != null ? job.contractType() : "No especificado")
                    .replace("{companyIndustry}", job.companyIndustry() != null ? job.companyIndustry() : "No especificado")
                    .replace("{companyEsgGoals}", job.companyEsgGoals() != null ? job.companyEsgGoals() : "No especificado")
                    .replace("{candidatesJson}", candidatesJson);

            long startTime = System.currentTimeMillis();

            // 3. Llamada al LLM con timeout acotado: sin esto, una respuesta colgada del
            // proveedor bloquea el hilo indefinidamente (no hay read-timeout configurado
            // para spring.ai.openai en application.properties).
            log.info("⚙️ Esperando respuesta del proveedor de IA...");

            CompletableFuture<String> llmCallFuture = CompletableFuture.supplyAsync(() ->
                    chatClient.prompt()
                            .user(finalPrompt)
                            .call()
                            .content()
            );

            String llmResponse = llmCallFuture.get(60, TimeUnit.SECONDS);

            long duration = System.currentTimeMillis() - startTime;
            log.info("📥 ¡Respuesta recibida en {} ms! Longitud: {} chars", duration, llmResponse.length());

            // 4. Limpiar y extraer el JSON puro
            String cleanJson = extractJsonArray(llmResponse);

            // 5. Parseo seguro
            List<MatchResultResponse> results = objectMapper.readValue(
                    cleanJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, MatchResultResponse.class)
            );

            verifyAntiBias(results);

            List<MatchResultResponse> rankedResults = rankAndLimitMatches(results, 15, 30);
            log.info("✅ Matching completado con éxito. {} candidatos mapeados, {} mostrados.", results.size(), rankedResults.size());
            return rankedResults;

        } catch (TimeoutException e) {
            log.error("⏳ TIMEOUT: El LLM tardó más de 35 segundos. Usando matching de respaldo.");
            return buildFallbackResults(job, candidates);
        } catch (ExecutionException | InterruptedException e) {
            log.error("💥 [ERROR IA] Error en la llamada al LLM: {}. Usando matching de respaldo.", e.getMessage(), e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return buildFallbackResults(job, candidates);
        } catch (Exception e) {
            log.error("💥 [ERROR IA] Error durante la comunicación o parseo del LLM: {}. Usando matching de respaldo.", e.getMessage(), e);
            return buildFallbackResults(job, candidates);
        }
    }

    /**
     * Matching de respaldo cuando el agente de IA no responde a tiempo o falla.
     * Fórmula precisa (máx 100):
     *   - Skills: (matchingSkills / jobSkills) * 60 (60% peso)
     *   - Experiencia: exact = 15, 1 nivel = 8, 2 niveles = 0 (15% peso)
     *   - Región: mismo municipio = 10 (10% peso)
     *   - Base: +10 solo si matchea al menos 1 skill (10% peso)
     *   - Extra diversidad: si candidate tiene badge +5 (5% peso)
     *   - Si job no tiene skills: skillScore base 35
     */
    private List<MatchResultResponse> buildFallbackResults(JobMatchRequest job, List<AnonymousCandidateResponse> candidates) {
        List<String> jobSkillsLower = job.skills().stream().map(s -> s.toLowerCase(Locale.ROOT)).toList();

        List<ExperienceLevel> levels = List.of(ExperienceLevel.JUNIOR, ExperienceLevel.MID, ExperienceLevel.SENIOR);
        int jobExpIdx = levels.indexOf(job.experienceLevel());

        List<MatchResultResponse> results = new ArrayList<>();
        for (AnonymousCandidateResponse candidate : candidates) {
            List<String> matchingSkills = candidate.skills().stream()
                    .filter(skill -> jobSkillsLower.contains(skill.toLowerCase(Locale.ROOT)))
                    .toList();

            int skillScore;
            if (!jobSkillsLower.isEmpty()) {
                skillScore = (int) Math.round((matchingSkills.size() * 60.0) / jobSkillsLower.size());
            } else {
                skillScore = 35;
            }

            int candExpIdx = levels.indexOf(candidate.experienceLevel());
            int expScore = 0;
            if (jobExpIdx != -1 && candExpIdx != -1) {
                int diff = Math.abs(jobExpIdx - candExpIdx);
                expScore = diff == 0 ? 15 : diff == 1 ? 8 : 0;
            }

            int regionScore = 0;
            if (job.region() != null && candidate.municipio() != null
                    && job.region().equalsIgnoreCase(candidate.municipio())) {
                regionScore = 10;
            }

            int baseScore = matchingSkills.isEmpty() ? 0 : 10;

            boolean hasBadge = candidate.diversityBadge() != null && !candidate.diversityBadge().isBlank();
            String badge = hasBadge ? candidate.diversityBadge() : null;

            int diversityExtra = hasBadge ? 5 : 0;

            int total = Math.min(100, skillScore + expScore + regionScore + baseScore + diversityExtra);

            int diversityScore;
            if (!hasBadge) {
                diversityScore = 0;
            } else {
                switch (badge) {
                    case "MUJER_STEM", "GENDER_DIVERSITY", "REGIONAL_DIVERSITY" -> diversityScore = 60;
                    case "TALENTO_REGIONAL", "TALENTO_RURAL" -> diversityScore = 50;
                    default -> diversityScore = 40;
                }
            }

            results.add(new MatchResultResponse(
                    candidate.candidateId(),
                    total,
                    diversityScore,
                    matchingSkills,
                    "Score calculated by fallback rules: " + matchingSkills.size() + "/" + jobSkillsLower.size()
                            + " skills, level " + candidate.experienceLevel()
                            + (hasBadge ? ", badge: " + badge : "")
                            + ". AI unavailable.",
                    badge
            ));
        }

        return rankAndLimitMatches(results, 15, 30);
    }
    private void verifyAntiBias(List<MatchResultResponse> results) {
        for (MatchResultResponse r : results) {
            if (r.inclusionReason() != null) {
                String reasonLower = r.inclusionReason().toLowerCase();
                if (reasonLower.contains("género") || reasonLower.contains("edad") || reasonLower.contains("etnia")) {
                    log.warn("⚠️ ALERTA ANTI-SESGO: La IA mencionó datos sensibles para el candidato {}", r.candidateId());
                }
            }
        }
    }

    static List<MatchResultResponse> rankAndLimitMatches(List<MatchResultResponse> results, int limit, int minimumScore) {
        if (results == null || results.isEmpty()) {
            return List.of();
        }

        return results.stream()
                .filter(Objects::nonNull)
                .filter(result -> result.compatibilityScore() >= minimumScore)
                .sorted(Comparator
                        .comparingInt(MatchResultResponse::compatibilityScore).reversed()
                        .thenComparingInt(MatchResultResponse::diversityScore).reversed()
                        .thenComparingLong(MatchResultResponse::candidateId))
                .limit(limit)
                .toList();
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