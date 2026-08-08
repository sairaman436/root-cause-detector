/*
 * Purpose: Coordinates local LLM root-cause analysis through the provider-neutral AI inference service.
 * Why it exists: Survey workflows need real Qwen/Ollama analysis while keeping model provider details outside business modules and validating structured output before persistence.
 * Architecture fit: Application-layer service for the AI bounded context; it depends on survey/evidence repositories for read-only context assembly and persists analysis metadata in ai.llm_analysis_results.
 */
package com.airural.platform.core.ai.application;

import com.airural.platform.core.ai.domain.LlmAnalysisResultEntity;
import com.airural.platform.core.ai.infrastructure.LlmAnalysisResultRepository;
import com.airural.platform.core.ai.web.dto.AiDtos.*;
import com.airural.platform.core.evidence.domain.EvidenceEntity;
import com.airural.platform.core.evidence.infrastructure.EvidenceRepository;
import com.airural.platform.core.survey.domain.SurveyEntity;
import com.airural.platform.core.survey.domain.SurveySubmissionAnswerEntity;
import com.airural.platform.core.survey.domain.SurveySubmissionEntity;
import com.airural.platform.core.survey.infrastructure.SurveyRepository;
import com.airural.platform.core.survey.infrastructure.SurveySubmissionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/** Application service for local LLM analysis. */
@Service
public class LocalLlmAnalysisService {
    private static final String PROMPT_ID = "ROOT_CAUSE_ANALYSIS";
    private static final String PROMPT_VERSION = "1.0.0";

    private final SurveyRepository surveys;
    private final SurveySubmissionRepository submissions;
    private final EvidenceRepository evidence;
    private final LlmAnalysisResultRepository results;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final String inferenceServiceUrl;
    private final String configuredModel;

    public LocalLlmAnalysisService(
            SurveyRepository surveys,
            SurveySubmissionRepository submissions,
            EvidenceRepository evidence,
            LlmAnalysisResultRepository results,
            ObjectMapper objectMapper,
            RestTemplateBuilder restTemplateBuilder,
            @Value("${airural.ai.gateway.inference-service-url:http://localhost:8101}") String inferenceServiceUrl,
            @Value("${airural.ai.gateway.default-model:qwen2.5:0.5b}") String configuredModel,
            @Value("${airural.ai.gateway.timeout-seconds:130}") int timeoutSeconds) {
        this.surveys = surveys;
        this.submissions = submissions;
        this.evidence = evidence;
        this.results = results;
        this.objectMapper = objectMapper;
        this.inferenceServiceUrl = inferenceServiceUrl;
        this.configuredModel = configuredModel;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(Math.max(10, timeoutSeconds)))
                .build();
    }

    /** Runs real local LLM root-cause analysis and persists only validated structured output. */
    @Transactional(noRollbackFor = AiException.class)
    public LlmAnalysisResponse analyzeRootCause(LlmAnalysisRequest request, UUID userId) {
        if (userId == null) {
            throw new AiException("AI_USER_REQUIRED", "Authenticated user is required for AI analysis.", HttpStatus.UNAUTHORIZED);
        }
        SurveyEntity survey = surveys.findByIdAndIsActiveTrue(request.surveyId())
                .orElseThrow(() -> new AiException("AI_SURVEY_NOT_FOUND", "Survey was not found for AI analysis.", HttpStatus.NOT_FOUND));
        SurveySubmissionEntity submission = loadSubmission(request.submissionId());
        List<EvidenceEntity> evidenceItems = loadEvidence(request.evidenceIds());
        UUID requestId = UUID.randomUUID();
        Instant started = Instant.now();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("request_id", requestId.toString());
        payload.put("problem", request.problem());
        payload.put("survey", surveyContext(survey));
        payload.put("submission", submissionContext(submission));
        payload.put("evidence", evidenceContext(evidenceItems));
        payload.put("citations", request.citations() == null ? List.of() : request.citations());
        payload.put("model", value(request.modelId(), configuredModel));
        payload.put("prompt_id", PROMPT_ID);
        payload.put("prompt_version", PROMPT_VERSION);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    inferenceServiceUrl + "/v1/analysis/root-cause",
                    HttpMethod.POST,
                    new HttpEntity<>(payload),
                    Map.class);
            Map<?, ?> body = response.getBody();
            if (body == null || body.get("output") == null) {
                throw new AiException("AI_EMPTY_ANALYSIS_RESPONSE", "AI inference service returned an empty analysis response.", HttpStatus.BAD_GATEWAY);
            }
            RuralAnalysisOutput output = validateOutput(body.get("output"));
            String resultJson = writeJson(output);
            LlmAnalysisResultEntity saved = results.save(new LlmAnalysisResultEntity(
                    requestId,
                    survey.id(),
                    submission == null ? null : submission.id(),
                    userId,
                    Objects.toString(body.get("provider"), "unknown"),
                    Objects.toString(body.get("model"), value(request.modelId(), configuredModel)),
                    Objects.toString(body.get("model_version"), null),
                    Objects.toString(body.get("prompt_id"), PROMPT_ID),
                    Objects.toString(body.get("prompt_version"), PROMPT_VERSION),
                    "SUCCEEDED",
                    asLong(body.get("latency_ms"), Duration.between(started, Instant.now()).toMillis()),
                    asInt(body.get("tokens_estimate"), 0),
                    resultJson,
                    null,
                    null));
            return response(saved, output);
        } catch (HttpStatusCodeException ex) {
            OperationalError error = parseOperationalError(ex.getResponseBodyAsString(), "AI_PROVIDER_ERROR", ex.getMessage());
            recordFailure(requestId, survey.id(), submission, userId, request, error, started);
            throw new AiException(error.code(), error.message(), HttpStatus.valueOf(ex.getStatusCode().value()));
        } catch (ResourceAccessException ex) {
            OperationalError error = new OperationalError("AI_PROVIDER_UNAVAILABLE", "AI inference service is unavailable or timed out.");
            recordFailure(requestId, survey.id(), submission, userId, request, error, started);
            throw new AiException(error.code(), error.message(), HttpStatus.SERVICE_UNAVAILABLE);
        } catch (RestClientException ex) {
            OperationalError error = new OperationalError("AI_PROVIDER_REQUEST_FAILED", "AI inference service request failed.");
            recordFailure(requestId, survey.id(), submission, userId, request, error, started);
            throw new AiException(error.code(), error.message(), HttpStatus.BAD_GATEWAY);
        }
    }

    private SurveySubmissionEntity loadSubmission(UUID submissionId) {
        if (submissionId == null) {
            return null;
        }
        return submissions.findWithAnswersById(submissionId)
                .orElseThrow(() -> new AiException("AI_SUBMISSION_NOT_FOUND", "Survey submission was not found for AI analysis.", HttpStatus.NOT_FOUND));
    }

    private List<EvidenceEntity> loadEvidence(List<UUID> evidenceIds) {
        if (evidenceIds == null || evidenceIds.isEmpty()) {
            return List.of();
        }
        return evidenceIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(id -> evidence.findByIdAndIsActiveTrue(id)
                        .orElseThrow(() -> new AiException("AI_EVIDENCE_NOT_FOUND", "Evidence was not found for AI analysis: " + id, HttpStatus.NOT_FOUND)))
                .toList();
    }

    private Map<String, Object> surveyContext(SurveyEntity survey) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("id", survey.id());
        context.put("name", survey.name());
        context.put("description", survey.description());
        context.put("status", survey.status().name());
        context.put("currentVersion", survey.currentVersion());
        context.put("organizationId", survey.organizationId());
        return context;
    }

    private Map<String, Object> submissionContext(SurveySubmissionEntity submission) {
        if (submission == null) {
            return Map.of();
        }
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("id", submission.id());
        context.put("status", submission.status());
        context.put("submittedAt", submission.submittedAt());
        context.put("answers", submission.answers().stream().map(this::answerContext).toList());
        return context;
    }

    private Map<String, Object> answerContext(SurveySubmissionAnswerEntity answer) {
        return Map.of(
                "questionId", answer.questionId(),
                "questionCode", answer.questionCode(),
                "value", answer.answerValue());
    }

    private List<Map<String, Object>> evidenceContext(List<EvidenceEntity> items) {
        return items.stream().map(item -> {
            Map<String, Object> context = new LinkedHashMap<>();
            context.put("id", item.id());
            context.put("fileName", item.originalFileName());
            context.put("mimeType", item.mimeType());
            context.put("evidenceType", item.evidenceType().name());
            context.put("sizeBytes", item.sizeBytes());
            context.put("sha256Checksum", item.sha256Checksum());
            return context;
        }).toList();
    }

    @SuppressWarnings("unchecked")
    private RuralAnalysisOutput validateOutput(Object raw) {
        if (!(raw instanceof Map<?, ?> rawMap)) {
            throw new AiException("AI_INVALID_ANALYSIS_OUTPUT", "AI analysis output was not a JSON object.", HttpStatus.BAD_GATEWAY);
        }
        String problem = string(rawMap.get("problem"), "problem");
        String summary = string(rawMap.get("summary"), "summary");
        double confidence = confidence(rawMap.get("confidence"));
        return new RuralAnalysisOutput(
                problem,
                summary,
                stringList(rawMap.get("contributing_factors")),
                stringList(rawMap.get("root_causes")),
                stringList(rawMap.get("evidence")),
                confidence,
                stringList(rawMap.get("recommendations")),
                stringList(rawMap.get("limitations")));
    }

    private String string(Object value, String field) {
        if (value == null || value.toString().isBlank()) {
            throw new AiException("AI_INVALID_ANALYSIS_OUTPUT", "AI analysis output is missing required field: " + field, HttpStatus.BAD_GATEWAY);
        }
        return value.toString();
    }

    private List<String> stringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().filter(Objects::nonNull).map(Object::toString).map(String::trim).filter(item -> !item.isBlank()).toList();
    }

    private double confidence(Object value) {
        if (!(value instanceof Number number)) {
            throw new AiException("AI_INVALID_ANALYSIS_OUTPUT", "AI analysis confidence must be numeric.", HttpStatus.BAD_GATEWAY);
        }
        double confidence = number.doubleValue();
        if (confidence < 0.0 || confidence > 1.0) {
            throw new AiException("AI_INVALID_ANALYSIS_OUTPUT", "AI analysis confidence must be between 0 and 1.", HttpStatus.BAD_GATEWAY);
        }
        return confidence;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new AiException("AI_ANALYSIS_SERIALIZATION_FAILED", "Unable to serialize validated AI analysis output.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("unchecked")
    private OperationalError parseOperationalError(String body, String fallbackCode, String fallbackMessage) {
        try {
            Map<String, Object> parsed = objectMapper.readValue(body, Map.class);
            Object detail = parsed.get("detail");
            if (detail instanceof Map<?, ?> detailMap) {
                return new OperationalError(
                        Objects.toString(detailMap.get("code"), fallbackCode),
                        Objects.toString(detailMap.get("message"), fallbackMessage));
            }
        } catch (Exception ignored) {
            // Client receives a sanitized operational error instead of raw provider payloads.
        }
        return new OperationalError(fallbackCode, fallbackMessage);
    }

    private void recordFailure(UUID requestId, UUID surveyId, SurveySubmissionEntity submission, UUID userId, LlmAnalysisRequest request, OperationalError error, Instant started) {
        results.save(new LlmAnalysisResultEntity(
                requestId,
                surveyId,
                submission == null ? null : submission.id(),
                userId,
                "ollama",
                value(request.modelId(), configuredModel),
                null,
                PROMPT_ID,
                PROMPT_VERSION,
                "FAILED",
                Duration.between(started, Instant.now()).toMillis(),
                0,
                "{}",
                error.code(),
                error.message()));
    }

    private LlmAnalysisResponse response(LlmAnalysisResultEntity saved, RuralAnalysisOutput output) {
        return new LlmAnalysisResponse(
                saved.id(),
                saved.requestId(),
                saved.surveyId(),
                saved.submissionId(),
                saved.provider(),
                saved.model(),
                saved.modelVersion(),
                saved.promptId(),
                saved.promptVersion(),
                saved.status(),
                saved.latencyMs(),
                saved.tokensEstimate(),
                saved.createdAt(),
                output);
    }

    private long asLong(Object value, long fallback) {
        return value instanceof Number number ? number.longValue() : fallback;
    }

    private int asInt(Object value, int fallback) {
        return value instanceof Number number ? number.intValue() : fallback;
    }

    private String value(String text, String fallback) {
        return text == null || text.isBlank() ? fallback : text;
    }

    private record OperationalError(String code, String message) {}
}
