package com.appbit.backend.modules.agent.service;

import com.appbit.backend.modules.candidate.Service.CandidateService;
import com.appbit.backend.modules.candidate.dto.AnonymousCandidateResponse;
import com.appbit.backend.modules.company.dto.JobMatchRequest;
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
            String candidatesJson = objectMapper.writeValueAsString(candidates);
            String jobDescription = job.description() != null ? job.description() : "N/A";

            // 2. Formatear el prompt
            String finalPrompt = promptTemplate
                    .replace("{jobTitle}", job.title())
                    .replace("{jobDescription}", jobDescription)
                    .replace("{jobSkills}", jobSkills)
                    .replace("{jobExperienceLevel}", job.experienceLevel().toString())
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

            String llmResponse = llmCallFuture.get(20, TimeUnit.SECONDS);

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

            List<MatchResultResponse> rankedResults = rankAndLimitMatches(results, 10, 50);
            log.info("✅ Matching completado con éxito. {} candidatos mapeados, {} mostrados.", results.size(), rankedResults.size());
            return rankedResults;

        } catch (TimeoutException e) {
            log.error("⏳ TIMEOUT: El LLM tardó más de 20 segundos. Usando matching de respaldo.");
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
     * Se basa únicamente en solapamiento de skills y nivel de experiencia (mismas
     * reglas anti-sesgo que el prompt: nunca infiere diversityBadge sin el LLM,
     * para no inventar una acción afirmativa sin criterio verificado).
     */
    private List<MatchResultResponse> buildFallbackResults(JobMatchRequest job, List<AnonymousCandidateResponse> candidates) {
        List<String> jobSkillsLower = job.skills().stream().map(s -> s.toLowerCase(Locale.ROOT)).toList();

        List<MatchResultResponse> results = new ArrayList<>();
        for (AnonymousCandidateResponse candidate : candidates) {
            List<String> matchingSkills = candidate.skills().stream()
                    .filter(skill -> jobSkillsLower.contains(skill.toLowerCase(Locale.ROOT)))
                    .toList();

            int compatibilityScore;
            if (!jobSkillsLower.isEmpty()) {
                compatibilityScore = (int) Math.round((matchingSkills.size() * 100.0) / jobSkillsLower.size());
            } else {
                compatibilityScore = 50;
            }
            if (candidate.experienceLevel() == job.experienceLevel()) {
                compatibilityScore = Math.min(100, compatibilityScore + 10);
            }

            results.add(new MatchResultResponse(
                    candidate.candidateId(),
                    compatibilityScore,
                    0,
                    matchingSkills,
                    "Score calculado por reglas de respaldo (skills en común y nivel de experiencia): " +
                            "el agente de IA no estuvo disponible para esta búsqueda.",
                    null
            ));
        }

        results.sort((a, b) -> Integer.compare(b.compatibilityScore(), a.compatibilityScore()));
        return results;
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
                .sorted(Comparator.comparingInt(MatchResultResponse::compatibilityScore).reversed()
                        .thenComparing(MatchResultResponse::candidateId))
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