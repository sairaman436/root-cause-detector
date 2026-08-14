/*
 * Purpose: Converts eligible per-scenario evaluation results into governed training candidates.
 * Why it exists: Evaluation output needs a controlled bridge into the existing human-review workflow without auto-approval or training.
 * Architecture fit: Application service at the evaluation-to-learning bounded-context boundary.
 */
package com.airural.platform.core.learning.application;

import com.airural.platform.core.evaluation.domain.PilotRunEntity;
import com.airural.platform.core.evaluation.domain.PilotScenarioEntity;
import com.airural.platform.core.evaluation.domain.PilotScenarioResultEntity;
import com.airural.platform.core.evaluation.infrastructure.PilotRunRepository;
import com.airural.platform.core.evaluation.infrastructure.PilotScenarioRepository;
import com.airural.platform.core.evaluation.infrastructure.PilotScenarioResultRepository;
import com.airural.platform.core.identity.security.AuthenticatedUser;
import com.airural.platform.core.learning.application.TrainingCandidateQueue.EvaluationCandidateData;
import com.airural.platform.core.learning.domain.TrainingCandidateEntity;
import com.airural.platform.core.learning.infrastructure.LearningRecordRepository;
import com.airural.platform.core.learning.web.dto.LearningDtos.CandidateGenerationRequest;
import com.airural.platform.core.learning.web.dto.LearningDtos.CandidateGenerationResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Generates pending candidates from validated evaluation results. */
@Service
public class EvaluationTrainingCandidateService {
    private static final BigDecimal MINIMUM_SCORE = BigDecimal.valueOf(0.80);
    private static final Set<String> SUPPORTED_TASKS = Set.of("root-cause-analysis", "recommendation-generation", "rag-grounded-responses");
    private static final Pattern PII = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}|(?<!\\d)\\d{10,12}(?!\\d)");

    private final PilotScenarioResultRepository results;
    private final PilotRunRepository runs;
    private final PilotScenarioRepository scenarios;
    private final LearningRecordRepository learningRecords;
    private final TrainingCandidateQueue learning;
    private final ObjectMapper objectMapper;

    public EvaluationTrainingCandidateService(
            PilotScenarioResultRepository results,
            PilotRunRepository runs,
            PilotScenarioRepository scenarios,
            LearningRecordRepository learningRecords,
            TrainingCandidateQueue learning,
            ObjectMapper objectMapper) {
        this.results = results;
        this.runs = runs;
        this.scenarios = scenarios;
        this.learningRecords = learningRecords;
        this.learning = learning;
        this.objectMapper = objectMapper;
    }

    /** Queues eligible results; every generated candidate remains pending human review. */
    @Transactional
    public CandidateGenerationResponse generate(CandidateGenerationRequest request, AuthenticatedUser actor) {
        if (actor == null || actor.userId() == null) {
            throw new LearningException(HttpStatus.UNAUTHORIZED, "AUTHENTICATED_GENERATOR_REQUIRED", "An authenticated generator is required");
        }
        List<PilotScenarioResultEntity> sourceResults = request == null || request.pilotRunId() == null
                ? results.findAll()
                : results.findByPilotRunId(request.pilotRunId());
        int generated = 0;
        int blocked = 0;
        int duplicates = 0;
        Map<String, Integer> reasons = new LinkedHashMap<>();
        for (PilotScenarioResultEntity result : sourceResults) {
            CandidateData candidate = eligibleData(result, reasons);
            if (candidate == null) {
                blocked++;
                continue;
            }
            if (learningRecords.existsByEvaluationResultId(result.getId())) {
                duplicates++;
                increment(reasons, "DUPLICATE_EVALUATION_RESULT");
                continue;
            }
            TrainingCandidateEntity queued = learning.queueEvaluationCandidate(candidate.data(), actor.email());
            if (queued != null) {
                generated++;
            } else {
                blocked++;
                increment(reasons, "CANDIDATE_PERSISTENCE_FAILED");
            }
        }
        return new CandidateGenerationResponse(generated, blocked, duplicates, Map.copyOf(reasons));
    }

    private CandidateData eligibleData(PilotScenarioResultEntity result, Map<String, Integer> reasons) {
        if (result == null || !Boolean.TRUE.equals(result.getPass())) {
            return blocked(reasons, "RESULT_NOT_PASSED");
        }
        if (result.getId() == null || result.getPilotRunId() == null || result.getScenarioId() == null) {
            return blocked(reasons, "EVALUATION_PROVENANCE_MISSING");
        }
        PilotRunEntity run = runs.findById(result.getPilotRunId()).orElse(null);
        if (run == null || !"COMPLETED".equalsIgnoreCase(run.getStatus())) {
            return blocked(reasons, "RUN_NOT_COMPLETED");
        }
        PilotScenarioEntity scenario = scenarios.findById(result.getScenarioId()).orElse(null);
        if (scenario == null) {
            return blocked(reasons, "SCENARIO_NOT_FOUND");
        }
        // The legacy synthetic_label remains SYNTHETIC for storage compatibility. The
        // explicit evaluation classification is the governance boundary: development
        // fixtures are blocked, while PILOT_EVALUATION results enter pending human review.
        if (!"PILOT_EVALUATION".equalsIgnoreCase(scenario.getEvaluationClassification())) {
            return blocked(reasons, "SYNTHETIC_FIXTURE");
        }
        if (scenario.isAdversarial()) {
            return blocked(reasons, "ADVERSARIAL_SCENARIO");
        }
        if (result.getOverallScore() == null || result.getOverallScore().compareTo(MINIMUM_SCORE) < 0) {
            return blocked(reasons, "SCORE_BELOW_THRESHOLD");
        }
        if (result.getUnsupportedClaimsCount() > 0 || result.getFalseCitationsCount() > 0 || result.getInventedStatisticsCount() > 0 || result.getInventedSchemesCount() > 0 || result.getFalseEligibilityCount() > 0) {
            return blocked(reasons, "QUALITY_GATE_FAILED");
        }
        JsonNode output = parseObject(result.getPipelineOutputJson());
        if (output == null) {
            return blocked(reasons, "PIPELINE_OUTPUT_INVALID");
        }
        if (scenario.getScenarioId() != null && scenario.getScenarioId().startsWith("pilot-v05-recommendation-coverage-")) {
            if (!output.path("bounded_output").asBoolean(false) || !output.path("sequence_gate").asBoolean(false)) {
                return blocked(reasons, "SEQUENCE_GATE_REQUIRED");
            }
        }
        String task = normalize(text(output, "task", "taskType", "task_type"));
        if (!SUPPORTED_TASKS.contains(task)) {
            return blocked(reasons, "UNSUPPORTED_TASK");
        }
        String input = firstNonBlank(text(output, "input", "prompt"), scenario.getProblemStatement());
        String aiOutput = text(output, "ai_output", "output", "response", "answer");
        if (input.isBlank()) {
            return blocked(reasons, "INPUT_MISSING");
        }
        if (aiOutput.isBlank()) {
            return blocked(reasons, "OUTPUT_MISSING");
        }
        JsonNode citations = firstNode(output, "citations", "evidence");
        if (citations == null) {
            citations = parseAny(scenario.getEvidenceJson());
        }
        if (citations == null || !citations.isArray() || citations.isEmpty()) {
            return blocked(reasons, "CITATIONS_MISSING_OR_INVALID");
        }
        String citationsJson = write(citations);
        if (containsDevelopmentSource(citations)) {
            return blocked(reasons, "DEVELOPMENT_SYNTHETIC_EVIDENCE");
        }
        if (scenario.getScenarioId() != null && scenario.getScenarioId().startsWith("pilot-v05r-")) {
            Set<String> permittedSources = permittedScenarioSources(scenario);
            boolean outsideAllowlist = citations.findValues("source_id").stream()
                    .map(JsonNode::asText)
                    .anyMatch(source -> !permittedSources.contains(source));
            if (outsideAllowlist) {
                return blocked(reasons, "EVIDENCE_SOURCE_NOT_PERMITTED");
            }
        }
        input = input + "\n\nRetrieved evidence and citation context:\n" + citationsJson;
        if (containsPii(input) || containsPii(aiOutput) || containsPii(citationsJson)) {
            return blocked(reasons, "PII_DETECTED");
        }
        ObjectNode metadata = objectMapper.createObjectNode();
        metadata.put("evaluation_result_id", result.getId().toString());
        metadata.put("pilot_run_id", result.getPilotRunId().toString());
        metadata.put("scenario_id", result.getScenarioId().toString());
        metadata.put("scenario_key", scenario.getScenarioId());
        metadata.put("pass", true);
        metadata.put("evaluation_classification", scenario.getEvaluationClassification());
        metadata.put("review_status", scenario.getReviewStatus());
        metadata.put("constructed", true);
        metadata.put("overall_score", result.getOverallScore());
        metadata.put("evaluated_at", result.getEvaluatedAt() == null ? Instant.EPOCH.toString() : result.getEvaluatedAt().toString());
        metadata.set("pipeline_output", output);
        return new CandidateData(new EvaluationCandidateData(result.getId(), task, scenario.getScenarioId(), input, firstNonBlank(text(output, "retrieved_context", "retrievedContext", "context"), scenario.getKnowledgeDocumentsJson()), aiOutput, citationsJson, "EVALUATION_RESULT", run.getModelVersion(), run.getPromptVersion(), result.getOverallScore(), write(metadata), false));
    }

    private CandidateData blocked(Map<String, Integer> reasons, String reason) {
        increment(reasons, reason);
        return null;
    }

    private void increment(Map<String, Integer> reasons, String reason) {
        reasons.merge(reason, 1, Integer::sum);
    }

    private JsonNode parseObject(String value) {
        JsonNode node = parseAny(value);
        return node != null && node.isObject() ? node : null;
    }

    private JsonNode parseAny(String value) {
        try {
            return value == null || value.isBlank() ? null : objectMapper.readTree(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private JsonNode firstNode(JsonNode node, String... names) {
        for (String name : names) {
            JsonNode value = node.get(name);
            if (value != null && !value.isNull()) {
                return value;
            }
        }
        return null;
    }

    private String text(JsonNode node, String... names) {
        JsonNode value = firstNode(node, names);
        return value == null || !value.isValueNode() ? "" : value.asText("").trim();
    }

    private String firstNonBlank(String first, String second) {
        return first == null || first.isBlank() ? second == null ? "" : second.trim() : first.trim();
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase().replace('_', '-').replace(' ', '-');
    }

    private boolean isSynthetic(String value) {
        return value != null && value.toUpperCase().contains("SYNTHETIC");
    }

    private boolean containsPii(String value) {
        return value != null && PII.matcher(value).find();
    }

    private boolean containsDevelopmentSource(JsonNode citations) {
        return citations.findValues("source_id").stream()
                .map(JsonNode::asText)
                .map(value -> value.toLowerCase())
                .anyMatch(value -> value.contains("development") || value.contains("synthetic") || value.contains("fixture"));
    }

    private Set<String> permittedScenarioSources(PilotScenarioEntity scenario) {
        Set<String> permitted = new java.util.HashSet<>();
        JsonNode provenance = parseAny(scenario.getScenarioProvenanceJson());
        String source = text(provenance, "evidence_source_id");
        if (!source.isBlank()) permitted.add(source);
        JsonNode evidence = parseAny(scenario.getEvidenceJson());
        if (evidence != null && evidence.isArray()) {
            evidence.forEach(row -> {
                String rowSource = text(row, "source");
                if (!rowSource.isBlank()) permitted.add(rowSource);
            });
        }
        return permitted;
    }

    private String write(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private record CandidateData(EvaluationCandidateData data) {}
}
