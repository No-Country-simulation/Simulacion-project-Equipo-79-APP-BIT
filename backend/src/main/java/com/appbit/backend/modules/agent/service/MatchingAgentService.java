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
import java.util.List;
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
            return List.of();
        }

        log.info("🤖 Iniciando llamada al LLM para vacante: {}", job.title());

        try {
            // 1. Preparar datos para inyectar en el prompt
            String jobSkills = String.join(", ", job.skills());
            String candidatesJson = objectMapper.writeValueAsString(candidates);

            // Si tu DTO no tiene description, pon un string vacío
            String jobDescription = job.description() != null ? job.description() : "N/A";

            // 2. Formatear el prompt usando REPLACE (Evita el error del símbolo %)
            String finalPrompt = promptTemplate
                    .replace("{jobTitle}", job.title())
                    .replace("{jobDescription}", jobDescription)
                    .replace("{jobSkills}", jobSkills)
                    .replace("{jobExperienceLevel}", job.experienceLevel().toString())
                    .replace("{candidatesJson}", candidatesJson);

            // 3. Llamar al LLM de forma asíncrona con Timeout de 10s
            CompletableFuture<String> llmCallFuture = CompletableFuture.supplyAsync(() -> {
                return chatClient.prompt()
                        .user(finalPrompt)
                        .call()
                        .content();
            });

            String llmResponse = llmCallFuture.get(10, TimeUnit.SECONDS);

            log.info("📥 Respuesta cruda del LLM recibida (Longitud: {} chars)", llmResponse.length());

            // 4. Limpiar y extraer el JSON puro
            String cleanJson = extractJsonArray(llmResponse);

            // 5. Parseo seguro
            List<MatchResultResponse> results = objectMapper.readValue(
                    cleanJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, MatchResultResponse.class)
            );

            // 🛡️ FILTRO ANTI-SESGO EN LOGS
            verifyAntiBias(results);

            log.info("✅ Matching completado. {} candidatos devueltos por IA.", results.size());
            return results;

        } catch (TimeoutException e) {
            log.error("⏳ TIMEOUT: El LLM tardó más de 10 segundos. Abortando.");
            throw new RuntimeException("El agente de IA tardó demasiado en responder.");
        } catch (ExecutionException | InterruptedException e) {
            log.error("💥 Error en el hilo de la llamada al LLM: {}", e.getMessage(), e);
            throw new RuntimeException("Falló la ejecución del agente de IA", e);
        } catch (Exception e) {
            log.error("💥 Error general en matching: {}", e.getMessage(), e);
            throw new RuntimeException("Error inesperado en el agente", e);
        }
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