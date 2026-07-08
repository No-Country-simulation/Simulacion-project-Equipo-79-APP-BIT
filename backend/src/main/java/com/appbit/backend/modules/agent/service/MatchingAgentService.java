package com.appbit.backend.modules.agent.service;

import com.appbit.backend.modules.agent.dto.MatchResultResponse;
import com.appbit.backend.modules.candidate.dto.AnonymousCandidateResponse;
import com.appbit.backend.modules.company.dto.JobMatchRequest;
import com.appbit.backend.modules.company.entity.ExperienceLevel;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
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

            ObjectMapper llmMapper = objectMapper.copy().setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY);
            String candidatesJson = llmMapper.writeValueAsString(candidates);

            String jobDescription = job.description() != null ? job.description() : "N/A";

            // 2. Formatear el prompt con todos los datos disponibles
            String finalPrompt = promptTemplate
                    .replace("{jobTitle}", job.title())
                    .replace("{jobDescription}", jobDescription)
                    .replace("{jobSkills}", jobSkills)
                    .replace("{jobSoftSkills}", jobSoftSkills)
                    .replace("{jobExperienceLevel}", String.valueOf(job.experienceLevel()))
                    .replace("{jobExperienceYears}",
                            job.experienceYears() != null ? String.valueOf(job.experienceYears()) : "No especificado")
                    .replace("{jobEducation}", job.education() != null ? job.education() : "No especificado")
                    .replace("{jobModality}", job.modality() != null ? job.modality() : "No especificado")
                    .replace("{jobSalaryRange}", job.salaryRange() != null ? job.salaryRange() : "No especificado")
                    .replace("{jobContractType}", job.contractType() != null ? job.contractType() : "No especificado")
                    .replace("{companyIndustry}",
                            job.companyIndustry() != null ? job.companyIndustry() : "No especificado")
                    .replace("{companyEsgGoals}",
                            job.companyEsgGoals() != null ? job.companyEsgGoals() : "No especificado")
                    .replace("{candidatesJson}", candidatesJson);

            long startTime = System.currentTimeMillis();

            // 3. Llamada al LLM con timeout acotado
            log.info("⚙️ Esperando respuesta del proveedor de IA...");

            CompletableFuture<String> llmCallFuture = CompletableFuture.supplyAsync(() -> chatClient.prompt()
                    .user(finalPrompt)
                    .call()
                    .content());

            String llmResponse = llmCallFuture.get(60, TimeUnit.SECONDS);

            long duration = System.currentTimeMillis() - startTime;
            log.info("📥 ¡Respuesta recibida en {} ms! Longitud: {} chars", duration, llmResponse.length());

            // 4. Limpiar y extraer el JSON puro
            String cleanJson = extractJsonArray(llmResponse);

            // 5. Parseo seguro
            List<MatchResultResponse> results = objectMapper.readValue(
                    cleanJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, MatchResultResponse.class));

            // Sanitizamos la respuesta para eliminar cualquier mención de datos sensibles
            List<MatchResultResponse> sanitizedResults = verifyAntiBias(results);

            List<MatchResultResponse> rankedResults = rankAndLimitMatches(sanitizedResults, 15, 30);
            log.info("✅ Matching completado con éxito. {} candidatos mapeados, {} mostrados.", results.size(),
                    rankedResults.size());
            return rankedResults;

        } catch (TimeoutException e) {
            log.error("⏳ TIMEOUT: El LLM tardó más de 60 segundos. Usando matching de respaldo.");
            return buildFallbackResults(job, candidates);
        } catch (ExecutionException | InterruptedException e) {
            log.error("💥 [ERROR IA] Error en la llamada al LLM: {}. Usando matching de respaldo.", e.getMessage(), e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return buildFallbackResults(job, candidates);
        } catch (Exception e) {
            log.error("💥 [ERROR IA] Error durante la comunicación o parseo del LLM: {}. Usando matching de respaldo.",
                    e.getMessage(), e);
            return buildFallbackResults(job, candidates);
        }
    }

    /**
     * Matching de respaldo cuando el agente de IA no responde a tiempo o falla.
     * Fórmula alineada al prompt (anti-sesgo):
     * - Skills: (matchingSkills / jobSkills) * 60 (60% peso)
     * - Nivel Experiencia: exact = 15, 1 nivel = 8, 2 niveles = 0 (15% peso)
     * - Años Experiencia: cumple/supera = 10, mitad = 5, no = 0 (10% peso)
     * - Educación: 0 (no hay dato anónimo, no se penaliza)
     * - Soft Skills: inferido si hay skills técnicas (5% peso)
     */
    private List<MatchResultResponse> buildFallbackResults(JobMatchRequest job,
            List<AnonymousCandidateResponse> candidates) {
        List<String> jobSkillsLower = job.skills().stream().map(s -> s.toLowerCase(Locale.ROOT)).toList();
        List<String> jobSoftSkillsLower = job.softSkills() != null
                ? job.softSkills().stream().map(s -> s.toLowerCase(Locale.ROOT)).toList()
                : List.of();

        List<ExperienceLevel> levels = List.of(ExperienceLevel.JUNIOR, ExperienceLevel.MID, ExperienceLevel.SENIOR);
        int jobExpIdx = levels.indexOf(job.experienceLevel());

        List<MatchResultResponse> results = new ArrayList<>();
        for (AnonymousCandidateResponse candidate : candidates) {

            // Skills Técnicos (60 pts)
            List<String> matchingSkills = candidate.skills().stream()
                    .filter(skill -> jobSkillsLower.contains(skill.toLowerCase(Locale.ROOT)))
                    .toList();

            int skillScore = 0;
            if (!jobSkillsLower.isEmpty()) {
                skillScore = (int) Math.round((matchingSkills.size() * 60.0) / jobSkillsLower.size());
            }

            // Nivel de Experiencia (15 pts)
            int candExpIdx = levels.indexOf(candidate.experienceLevel());
            int expScore = 0;
            if (jobExpIdx != -1 && candExpIdx != -1) {
                int diff = Math.abs(jobExpIdx - candExpIdx);
                expScore = diff == 0 ? 15 : diff == 1 ? 8 : 0;
            }

            // Años de Experiencia (10 pts) - Inferido ya que el candidato anónimo no los
            // trae
            int expYearsScore = 0;
            if (job.experienceYears() != null && job.experienceYears() > 0) {
                if (candExpIdx >= jobExpIdx)
                    expYearsScore = 10;
                else if (candExpIdx != -1 && Math.abs(jobExpIdx - candExpIdx) == 1)
                    expYearsScore = 5;
            } else {
                expYearsScore = 10; // Si la vacante no exige años, se asume que cumple
            }

            // Educación (10 pts) - No hay dato en candidato anónimo, se asigna 0 sin
            // penalizar
            int educationScore = 0;

            // Soft Skills (5 pts) - Inferido si hay match técnico
            int softSkillScore = 0;
            if (!jobSoftSkillsLower.isEmpty() && !matchingSkills.isEmpty()) {
                softSkillScore = 3;
            }

            // COMPATIBILITY SCORE PURO (Sin región ni badges - Anti-Sesgo)
            int totalCompatibility = Math.min(100,
                    skillScore + expScore + expYearsScore + educationScore + softSkillScore);

            // DIVERSITY SCORE (Independiente)
            boolean hasBadge = candidate.diversityBadge() != null && !candidate.diversityBadge().isBlank();
            String badge = hasBadge ? candidate.diversityBadge() : null;

            int diversityScore = 0;
            if (hasBadge) {
                switch (badge) {
                    case "STEM_WOMAN", "GENDER_DIVERSITY", "REGIONAL_DIVERSITY" -> diversityScore = 60;
                    case "REGIONAL_TALENT", "RURAL_TALENT" -> diversityScore = 50;
                    default -> diversityScore = 40;
                }
            }

            String inclusionReason = String.format("Fallback match: %d/%d skills, level %s%s. AI unavailable.",
                    matchingSkills.size(), jobSkillsLower.size(), candidate.experienceLevel(),
                    hasBadge ? ", badge: " + badge : "");

            results.add(new MatchResultResponse(
                    candidate.candidateId(),
                    totalCompatibility,
                    diversityScore,
                    matchingSkills,
                    inclusionReason,
                    badge));
        }

        return rankAndLimitMatches(results, 15, 30);
    }

    private List<MatchResultResponse> verifyAntiBias(List<MatchResultResponse> results) {
        // Lista de palabras que indican que la IA rompió la regla de anonimización
        List<String> sensitiveWords = List.of("género", "gender", "edad", "age", "etnia", "ethnicity",
                "mujer", "hombre", "femenino", "masculino", "diversidad");

        return results.stream().map(r -> {
            if (r.inclusionReason() != null) {
                String reasonLower = r.inclusionReason().toLowerCase();

                // Si la IA mencionó algún dato sensible en la justificación técnica
                boolean isBiased = sensitiveWords.stream().anyMatch(reasonLower::contains);

                if (isBiased) {
                    log.warn(
                            "⚠️ ALERTA ANTI-SESGO: La IA mencionó datos sensibles para el candidato {}. Sanitizando respuesta.",
                            r.candidateId());

                    // Reemplazamos por una justificación técnica genérica y segura
                    String safeReason = "El candidato cuenta con habilidades técnicas y experiencia alineadas a los requisitos de la vacante.";

                    // Devolvemos una nueva instancia del record con la justificación limpia
                    return new MatchResultResponse(
                            r.candidateId(),
                            r.compatibilityScore(),
                            r.diversityScore(),
                            r.matchingSkills(),
                            safeReason,
                            r.diversityBadge());
                }
            }
            return r; // Si está limpio, pasa igual
        }).toList();
    }

    static List<MatchResultResponse> rankAndLimitMatches(List<MatchResultResponse> results, int limit,
            int minimumScore) {
        if (results == null || results.isEmpty()) {
            return List.of();
        }

        // Comparador personalizado para el "Tiebreaker Inclusivo"
        Comparator<MatchResultResponse> inclusiveComparator = (c1, c2) -> {
            int score1 = c1.compatibilityScore();
            int score2 = c2.compatibilityScore();

            // Si la diferencia de score técnico es menor a 5 puntos...
            if (Math.abs(score1 - score2) < 5) {
                // ...rankeamos primero al que tenga MAYOR diversityScore
                int divCompare = Integer.compare(c2.diversityScore(), c1.diversityScore());
                if (divCompare != 0)
                    return divCompare;
            } else {
                // Si la diferencia es de 5 o más, el score técnico manda estrictamente
                int scoreCompare = Integer.compare(score2, score1);
                if (scoreCompare != 0)
                    return scoreCompare;
            }

            // Si todo lo anterior empata, ordenamos por ID para tener un orden determinista
            return Long.compare(c1.candidateId(), c2.candidateId());
        };

        return results.stream()
                .filter(Objects::nonNull)
                .filter(result -> result.compatibilityScore() >= minimumScore)
                .sorted(inclusiveComparator)
                .limit(limit)
                .toList();
    }

    private String extractJsonArray(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank())
            return "[]";

        String cleaned = rawResponse.trim();

        // Limpiar bloces de código markdown (```json ... ```)
        if (cleaned.startsWith("```json"))
            cleaned = cleaned.substring(7);
        else if (cleaned.startsWith("```"))
            cleaned = cleaned.substring(3);
        if (cleaned.endsWith("```"))
            cleaned = cleaned.substring(0, cleaned.length() - 3);

        cleaned = cleaned.trim();

        // 1. Intentar extraer un ARRAY [ ... ]
        int startIndex = cleaned.indexOf('[');
        int endIndex = cleaned.lastIndexOf(']');

        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return cleaned.substring(startIndex, endIndex + 1);
        }

        // 2. Si no hay array, intentar extraer un OBJETO simple { ... }
        // (Los LLMs a veces devuelven un solo objeto en lugar de un array de 1
        // elemento)
        startIndex = cleaned.indexOf('{');
        endIndex = cleaned.lastIndexOf('}');

        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            // Lo envolvemos en corchetes para convertirlo en un array válido
            return "[" + cleaned.substring(startIndex, endIndex + 1) + "]";
        }

        // 3. Si no encuentra ni array ni objeto, devuelve array vacío para no romper el
        // parseo
        return "[]";
    }
}