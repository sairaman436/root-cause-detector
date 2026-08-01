/*
 * Purpose: Implements the unified AI gateway.
 * Why it exists: Model routing, safety, fallback behavior, token accounting, latency monitoring, and logs must be centralized.
 * Architecture fit: Application service fronting local LLM providers and future GPU clusters.
 */
package com.airural.platform.core.ai.application;

import com.airural.platform.core.ai.domain.*;
import com.airural.platform.core.ai.infrastructure.*;
import com.airural.platform.core.ai.web.dto.AiDtos.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/** Unified AI gateway service. */
@Service
public class AiGatewayService {
    private final AiSafetyService safetyService;
    private final AIModelRepository modelRepository;
    private final InferenceLogRepository inferenceLogRepository;
    private final TokenUsageRepository usageRepository;
    private final String defaultModel;
    private final String inferenceServiceUrl;
    private final RestTemplate restTemplate;

    public AiGatewayService(
            AiSafetyService safetyService,
            AIModelRepository modelRepository,
            InferenceLogRepository inferenceLogRepository,
            TokenUsageRepository usageRepository,
            @Value("${airural.ai.gateway.default-model:qwen2.5-local}") String defaultModel,
            @Value("${airural.ai.gateway.inference-service-url:http://localhost:8101}") String inferenceServiceUrl,
            RestTemplateBuilder restTemplateBuilder) {
        this.safetyService = safetyService;
        this.modelRepository = modelRepository;
        this.inferenceLogRepository = inferenceLogRepository;
        this.usageRepository = usageRepository;
        this.defaultModel = defaultModel;
        this.inferenceServiceUrl = inferenceServiceUrl;
        this.restTemplate = restTemplateBuilder.setConnectTimeout(Duration.ofSeconds(2)).setReadTimeout(Duration.ofSeconds(8)).build();
    }

    /** Routes a chat request through validation, fallback response generation, and telemetry persistence. */
    @Transactional
    public ChatResponse chat(ChatRequest request, UUID userId) {
        Instant started = Instant.now();
        String modelId = routeModel(request.modelId());
        String safePrompt = safetyService.validateAndMask(request.message());
        int promptTokens = tokens(safePrompt);
        InferenceServiceResult inference = callInferenceService(modelId, safePrompt, request.context());
        String response = inference.output();
        int completionTokens = tokens(response);
        long latency = Duration.between(started, Instant.now()).toMillis();
        InferenceLogEntity log = inferenceLogRepository.save(new InferenceLogEntity(userId, modelId, "CHAT", "SUCCEEDED", promptTokens, completionTokens, latency, false, hash(safePrompt), response));
        usageRepository.save(new TokenUsageEntity(userId, modelId, promptTokens, completionTokens, (promptTokens + completionTokens) * 0.000001));
        return new ChatResponse(log.id(), modelId, response, promptTokens, completionTokens, latency, inference.fallbackUsed(), List.of(new CitationResponse(inference.provider(), log.id().toString(), inference.summary(), 1.0)));
    }

    /** Records an inference log for a RAG response. */
    @Transactional
    public InferenceLogEntity recordRagInference(UUID userId, String modelId, String prompt, String response, long latencyMs) {
        int promptTokens = tokens(prompt);
        int completionTokens = tokens(response);
        InferenceLogEntity log = inferenceLogRepository.save(new InferenceLogEntity(userId, modelId, "RAG", "SUCCEEDED", promptTokens, completionTokens, latencyMs, false, hash(prompt), response));
        usageRepository.save(new TokenUsageEntity(userId, modelId, promptTokens, completionTokens, (promptTokens + completionTokens) * 0.000001));
        return log;
    }

    private String routeModel(String requested) {
        String candidate = requested == null || requested.isBlank() ? defaultModel : requested;
        return modelRepository.findByModelId(candidate).map(AIModelEntity::modelId).orElse(defaultModel);
    }

    private InferenceServiceResult callInferenceService(String modelId, String prompt, Map<String, Object> context) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("prompt", prompt);
            payload.put("task_type", "chat");
            payload.put("model", modelId);
            payload.put("context", context == null ? Map.of() : context);
            payload.put("require_json", true);
            ResponseEntity<Map> response = restTemplate.exchange(inferenceServiceUrl + "/v1/inference", HttpMethod.POST, new HttpEntity<>(payload), Map.class);
            Map<?, ?> body = response.getBody();
            if (body != null && body.get("output") != null) {
                return new InferenceServiceResult(String.valueOf(body.get("output")), Objects.toString(body.get("provider"), "ai-inference-service"), Boolean.TRUE.equals(body.get("fallback_used")), "Response generated through ai-inference-service.");
            }
        } catch (RestClientException ex) {
            // The local deterministic path keeps CI and offline development executable when the AI service is unavailable.
        }
        return new InferenceServiceResult("AI foundation response from " + modelId + ": " + summarize(prompt), "deterministic-local", true, "Backend deterministic fallback used because ai-inference-service was unavailable.");
    }

    private int tokens(String text) {
        return Math.max(1, text.split("\\s+").length);
    }

    private String summarize(String text) {
        return text.length() <= 220 ? text : text.substring(0, 220);
    }

    private String hash(String text) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder out = new StringBuilder();
            for (byte b : digest) {
                out.append(String.format("%02x", b));
            }
            return out.toString();
        } catch (Exception ex) {
            return "unavailable";
        }
    }

    private record InferenceServiceResult(String output, String provider, boolean fallbackUsed, String summary) {}
}
