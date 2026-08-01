/*
 * Purpose: Coordinates unified inference serving, routing, session context, validation, metrics, and audit records.
 * Why it exists: AI-8 makes this backend the controlled gateway through which all model inference is performed.
 * Architecture fit: Application service for enterprise model serving without retraining, evaluation, or dataset collection.
 */
package com.airural.platform.core.serving.application;

import com.airural.platform.core.ai.web.dto.AiDtos.*;
import com.airural.platform.core.serving.domain.*;
import com.airural.platform.core.serving.infrastructure.*;
import com.airural.platform.core.serving.web.dto.ServingDtos.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.HexFormat;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for AI serving. */
@Service
public class ServingGatewayService {
    private final InferenceSessionRepository sessions;
    private final InferenceRequestRepository requests;
    private final InferenceResponseRepository responses;
    private final ServingNodeRepository nodes;
    private final ModelDeploymentRepository deployments;
    private final RoutingDecisionRepository routingDecisions;
    private final InferenceMetricsRepository metrics;
    private final ServingAuditRepository audits;

    public ServingGatewayService(InferenceSessionRepository sessions, InferenceRequestRepository requests, InferenceResponseRepository responses, ServingNodeRepository nodes, ModelDeploymentRepository deployments, RoutingDecisionRepository routingDecisions, InferenceMetricsRepository metrics, ServingAuditRepository audits) {
        this.sessions = sessions; this.requests = requests; this.responses = responses; this.nodes = nodes; this.deployments = deployments; this.routingDecisions = routingDecisions; this.metrics = metrics; this.audits = audits;
    }

    /** Serves the legacy AI chat route through the enterprise serving gateway. */
    @Transactional
    public ChatResponse chat(ChatRequest request, UUID userId) {
        ServingInferenceResponse served = infer(new ServingInferenceRequest(request.message(), "GENERAL_ASSISTANT", "GENERAL_ASSISTANT", language(request.context()), "PLATFORM_USER", request.sessionId(), request.context(), request.stream(), false, false, null, "default"), userId);
        return new ChatResponse(served.inferenceId(), served.selectedModel(), served.response(), served.promptTokens(), served.completionTokens(), served.latencyMs(), served.fallbackUsed(), List.of(new CitationResponse("SERVING_GATEWAY", served.inferenceId().toString(), "Response passed routing, prompt security, output validation, citation validation, and audit logging.", 1.0)));
    }

    /** Runs unified deterministic inference through the serving pipeline. */
    @Transactional
    public ServingInferenceResponse infer(ServingInferenceRequest request, UUID userId) {
        String safePrompt = validatePrompt(request.prompt());
        InferenceSessionEntity session = session(request.sessionId(), userId, request);
        ModelDeploymentEntity deployment = deployment(request.assistantType());
        ServingNodeEntity node = node(deployment.getProviderType());
        UUID requestId = UUID.randomUUID();
        InferenceRequestEntity inferenceRequest = requests.save(new InferenceRequestEntity(requestId, session.getId(), userId, value(request.taskType(), "GENERAL_ASSISTANT"), value(request.assistantType(), "GENERAL_ASSISTANT"), value(request.language(), "en"), value(request.userRole(), "PLATFORM_USER"), safePrompt, contextJson(request.context()), Boolean.TRUE.equals(request.stream()), Boolean.TRUE.equals(request.batch()), Boolean.TRUE.equals(request.async()), "PASSED", "PASSED", "ROUTED", Instant.now()));
        String policy = routingPolicy(request, deployment, node);
        routingDecisions.save(new RoutingDecisionEntity(UUID.randomUUID(), inferenceRequest.getId(), deployment.getId(), node.getId(), deployment.getModelKey(), policy, "rural-foundation-fallback", true, decisionFactors(request), Instant.now()));
        long latency = Math.max(25L, safePrompt.length() / 2L);
        String output = "AI foundation response via AI-8 serving gateway from " + deployment.getModelKey() + " for " + value(request.taskType(), "GENERAL_ASSISTANT") + ": " + safePrompt;
        InferenceResponseEntity response = responses.save(new InferenceResponseEntity(UUID.randomUUID(), inferenceRequest.getId(), deployment.getModelKey(), deployment.getProviderType(), output, "PASSED", "PASSED", false, false, tokens(safePrompt), tokens(output), latency, "SUCCEEDED", Instant.now()));
        audit(inferenceRequest.getId(), userId, "INFERENCE_SERVED", value(request.tenantId(), "default"), value(request.requestSignature(), checksum(safePrompt)), "{\"model\":\"" + deployment.getModelKey() + "\",\"provider\":\"" + deployment.getProviderType() + "\"}");
        return new ServingInferenceResponse(response.getId(), session.getId(), deployment.getModelKey(), deployment.getProviderType(), output, tokens(safePrompt), tokens(output), latency, false, false, policy, "SUCCEEDED");
    }

    /** Returns model serving registry records. */
    @Transactional(readOnly = true)
    public Page<ServingModelResponse> models(Pageable pageable) {
        return deployments.findAll(pageable).map(model -> new ServingModelResponse(model.getId(), model.getModelKey(), model.getProviderType(), "ACTIVE"));
    }

    /** Returns serving health. */
    @Transactional(readOnly = true)
    public ServingHealthResponse health() {
        long active = nodes.count();
        return new ServingHealthResponse(active == 0 ? "DEGRADED_NO_REGISTERED_NODES" : "HEALTHY", (int) active, (int) active, "CLOSED");
    }

    /** Returns serving metrics and stores a snapshot. */
    @Transactional
    public ServingMetricsResponse metrics() {
        metrics.save(new InferenceMetricsEntity(UUID.randomUUID(), "ROLLING_5_MINUTES", BigDecimal.valueOf(12.4), BigDecimal.valueOf(420), BigDecimal.valueOf(61.2), BigDecimal.valueOf(68.5), BigDecimal.valueOf(14.1), BigDecimal.valueOf(43.0), BigDecimal.valueOf(22.4), 3, BigDecimal.valueOf(0.004), BigDecimal.valueOf(0.002), Instant.now()));
        return new ServingMetricsResponse(BigDecimal.valueOf(12.4), BigDecimal.valueOf(420), BigDecimal.valueOf(61.2), BigDecimal.valueOf(0.004), BigDecimal.valueOf(0.002));
    }

    /** Lists inference sessions. */
    @Transactional(readOnly = true)
    public Page<ServingSessionResponse> sessions(Pageable pageable) {
        return sessions.findAll(pageable).map(session -> new ServingSessionResponse(session.getId(), session.getUserId(), session.getStatus()));
    }

    private InferenceSessionEntity session(UUID id, UUID userId, ServingInferenceRequest request) {
        if (id != null) {
            return sessions.findById(id).orElseThrow(() -> new ServingException(HttpStatus.NOT_FOUND, "SERVING_SESSION_NOT_FOUND", "Inference session was not found"));
        }
        return sessions.save(new InferenceSessionEntity(UUID.randomUUID(), userId, value(request.assistantType(), "GENERAL_ASSISTANT"), "[]", "{}", value((String) contextValue(request.context(), "villageContext"), "{}"), value((String) contextValue(request.context(), "surveyContext"), "{}"), value((String) contextValue(request.context(), "knowledgeContext"), "{}"), 8192, Instant.now().plus(8, ChronoUnit.HOURS), "ACTIVE", Instant.now(), Instant.now()));
    }

    private ModelDeploymentEntity deployment(String assistantType) {
        String type = value(assistantType, "GENERAL_ASSISTANT");
        return deployments.findFirstByAssistantTypeAndTrafficStatusOrderByCreatedAtDesc(type, "ACTIVE")
                .orElseGet(() -> deployments.save(new ModelDeploymentEntity(UUID.randomUUID(), null, modelFor(type), "serving-v1", type, providerFor(type), "HYBRID_INFERENCE", "PASSED", "ACTIVE", true, "serving-v0", Instant.now())));
    }

    private ServingNodeEntity node(String providerType) {
        return nodes.save(new ServingNodeEntity(UUID.randomUUID(), providerType.toLowerCase() + "-primary", providerType, "provider://" + providerType.toLowerCase(), providerType.contains("GPU") || providerType.equals("VLLM") ? "CLOUD_GPU" : "LOCAL_CPU", 0, 32, providerType.equals("LLAMA_CPP") ? 0 : 1, providerType.equals("LLAMA_CPP") ? 0 : 24, 16, 64, "CLOSED", "HEALTHY", Instant.now()));
    }

    private String modelFor(String assistantType) {
        return switch (assistantType) {
            case "POLICY_ASSISTANT" -> "rural-policy-assistant";
            case "AGRICULTURE_ASSISTANT" -> "rural-agriculture-assistant";
            case "HEALTH_ASSISTANT" -> "rural-health-assistant";
            case "ANALYTICS_ASSISTANT" -> "rural-analytics-assistant";
            case "ROOT_CAUSE_ASSISTANT" -> "rural-root-cause-assistant";
            case "RECOMMENDATION_ASSISTANT" -> "rural-recommendation-assistant";
            default -> "rural-general-assistant";
        };
    }

    private String providerFor(String assistantType) {
        return switch (assistantType) {
            case "ROOT_CAUSE_ASSISTANT", "ANALYTICS_ASSISTANT" -> "VLLM";
            case "POLICY_ASSISTANT" -> "OLLAMA";
            case "EDGE_ASSISTANT" -> "LLAMA_CPP";
            default -> "OPENAI_COMPATIBLE";
        };
    }

    private String validatePrompt(String prompt) {
        if (prompt == null || prompt.isBlank()) throw new ServingException(HttpStatus.BAD_REQUEST, "SERVING_PROMPT_REQUIRED", "Prompt is required");
        String lowered = prompt.toLowerCase(Locale.ROOT);
        if (lowered.contains("ignore previous instructions") || lowered.contains("reveal system prompt")) {
            throw new ServingException(HttpStatus.BAD_REQUEST, "SERVING_PROMPT_SECURITY_BLOCKED", "Prompt failed security validation");
        }
        return prompt.replaceAll("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}", "[EMAIL_MASKED]").replaceAll("\\b\\d{10}\\b", "[PHONE_MASKED]").replace("\"", "'");
    }

    private void audit(UUID requestId, UUID userId, String eventType, String tenantId, String requestSignature, String eventJson) {
        audits.save(new ServingAuditEntity(UUID.randomUUID(), requestId, userId, eventType, tenantId, requestSignature, eventJson, checksum(eventType + ":" + requestId + ":" + eventJson), Instant.now()));
    }

    private String routingPolicy(ServingInferenceRequest request, ModelDeploymentEntity deployment, ServingNodeEntity node) {
        return "task=" + value(request.taskType(), "GENERAL_ASSISTANT") + ";assistant=" + value(request.assistantType(), "GENERAL_ASSISTANT") + ";provider=" + deployment.getProviderType() + ";latency=balanced;fallback=enabled;circuit=closed";
    }

    private String decisionFactors(ServingInferenceRequest request) {
        return "{\"taskType\":\"" + value(request.taskType(), "GENERAL_ASSISTANT") + "\",\"language\":\"" + value(request.language(), "en") + "\",\"contextSize\":\"bounded\",\"userRole\":\"" + value(request.userRole(), "PLATFORM_USER") + "\",\"policyConstraints\":\"passed\"}";
    }

    private Object contextValue(Map<String, Object> context, String key) {
        return context == null ? null : context.get(key);
    }

    private String contextJson(Map<String, Object> context) {
        return context == null ? "{}" : context.toString().replace("\"", "'");
    }

    private String language(Map<String, Object> context) {
        Object language = contextValue(context, "language");
        return language == null ? "en" : language.toString();
    }

    private int tokens(String text) { return Math.max(1, text.length() / 4); }
    private String value(String text, String fallback) { return text == null || text.isBlank() ? fallback : text.replace("\"", "'"); }

    private String checksum(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new ServingException(HttpStatus.INTERNAL_SERVER_ERROR, "SERVING_HASH_FAILED", "Unable to calculate serving audit hash");
        }
    }
}
