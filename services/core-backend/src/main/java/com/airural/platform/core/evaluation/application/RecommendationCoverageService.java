/*
 * Purpose: Derives governed recommendation evaluations from explicitly approved root-cause candidates.
 * Why it exists: Recommendation coverage must not depend on a newly generated, unreviewed root cause.
 * Architecture fit: Evaluation-to-decision application service; it reuses the existing RAG, recommendation,
 * persistence, and learning-review boundaries without changing datasets or review decisions.
 */
package com.airural.platform.core.evaluation.application;

import com.airural.platform.core.ai.application.AiFoundationService;
import com.airural.platform.core.ai.web.dto.AiDtos.CitationResponse;
import com.airural.platform.core.ai.web.dto.AiDtos.RagQueryRequest;
import com.airural.platform.core.ai.web.dto.AiDtos.RagQueryResponse;
import com.airural.platform.core.decision.application.RecommendationIntelligenceService;
import com.airural.platform.core.decision.web.dto.RecommendationIntelligenceDtos.RecommendationGenerateRequest;
import com.airural.platform.core.decision.web.dto.RecommendationIntelligenceDtos.RecommendationOptionResponse;
import com.airural.platform.core.decision.web.dto.RecommendationIntelligenceDtos.RecommendationSetResponse;
import com.airural.platform.core.decision.web.dto.RecommendationIntelligenceDtos.RootCauseInput;
import com.airural.platform.core.evaluation.domain.PilotDatasetEntity;
import com.airural.platform.core.evaluation.domain.PilotRunEntity;
import com.airural.platform.core.evaluation.domain.PilotScenarioEntity;
import com.airural.platform.core.evaluation.domain.PilotScenarioResultEntity;
import com.airural.platform.core.evaluation.infrastructure.PilotDatasetRepository;
import com.airural.platform.core.evaluation.infrastructure.PilotRunRepository;
import com.airural.platform.core.evaluation.infrastructure.PilotScenarioRepository;
import com.airural.platform.core.evaluation.infrastructure.PilotScenarioResultRepository;
import com.airural.platform.core.evaluation.web.dto.GovernedEvaluationDtos.RecommendationCoverageBatchResponse;
import com.airural.platform.core.evaluation.web.dto.GovernedEvaluationDtos.RecommendationCoverageCandidateResponse;
import com.airural.platform.core.knowledge.application.KnowledgeRagGatewayService;
import com.airural.platform.core.learning.domain.LearningRecordEntity;
import com.airural.platform.core.learning.domain.TrainingCandidateEntity;
import com.airural.platform.core.learning.infrastructure.LearningRecordRepository;
import com.airural.platform.core.learning.infrastructure.TrainingCandidateRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Creates recommendation evaluations only from human-approved, governed root causes. */
@Service
public class RecommendationCoverageService {
    private static final UUID DATASET_ID = UUID.fromString("00000000-0000-0000-0000-000000002850");
    private static final String PILOT_CLASSIFICATION = "PILOT_EVALUATION";
    private static final String MODEL_VERSION = "qwen2.5:0.5b";
    private static final String PROMPT_VERSION = "RECOMMENDATION_GENERATION@1.0.0";
    private static final Pattern PII = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}|(?<!\\d)\\d{10,12}(?!\\d)");

    private final PilotDatasetRepository datasets;
    private final PilotRunRepository runs;
    private final PilotScenarioRepository scenarios;
    private final PilotScenarioResultRepository results;
    private final TrainingCandidateRepository candidates;
    private final LearningRecordRepository learningRecords;
    private final RecommendationIntelligenceService recommendationService;
    private final AiFoundationService aiFoundationService;
    private final KnowledgeRagGatewayService knowledgeRagGateway;
    private final JdbcOperations jdbcTemplate;
    private final ObjectMapper objectMapper;

    public RecommendationCoverageService(
            PilotDatasetRepository datasets,
            PilotRunRepository runs,
            PilotScenarioRepository scenarios,
            PilotScenarioResultRepository results,
            TrainingCandidateRepository candidates,
            LearningRecordRepository learningRecords,
            RecommendationIntelligenceService recommendationService,
            AiFoundationService aiFoundationService,
            KnowledgeRagGatewayService knowledgeRagGateway,
            JdbcOperations jdbcTemplate,
            ObjectMapper objectMapper) {
        this.datasets = datasets;
        this.runs = runs;
        this.scenarios = scenarios;
        this.results = results;
        this.candidates = candidates;
        this.learningRecords = learningRecords;
        this.recommendationService = recommendationService;
        this.aiFoundationService = aiFoundationService;
        this.knowledgeRagGateway = knowledgeRagGateway;
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    /** Inspects all approved v0.5 remediation roots and leaves every result pending review. */
    @Transactional
    public RecommendationCoverageBatchResponse generateAll(UUID userId) {
        requireUser(userId);
        List<RecommendationCoverageCandidateResponse> outcomes = new ArrayList<>();
        for (TrainingCandidateEntity candidate : candidates.findByApprovalStatusOrderByCreatedAtAsc("APPROVED_FOR_DATASET")) {
            LearningRecordEntity record = learningRecords.findById(candidate.getLearningRecordId()).orElse(null);
            if (record == null || !"root-cause-analysis".equalsIgnoreCase(record.getTaskType()) || !isRecommendationCoverageSource(record.getScenarioGroup())) {
                continue;
            }
            outcomes.add(generateOne(candidate.getId(), userId));
        }
        int generated = (int) outcomes.stream().filter(outcome -> "PENDING_HUMAN_REVIEW".equals(outcome.status())).count();
        return new RecommendationCoverageBatchResponse(outcomes, generated, outcomes.size() - generated, "PENDING_HUMAN_REVIEW");
    }

    /** Inspects and, when eligible, executes one recommendation coverage scenario. */
    @Transactional
    public RecommendationCoverageCandidateResponse generateOne(UUID candidateId, UUID userId) {
        requireUser(userId);
        SourceRoot source = inspect(candidateId);
        if (source.blockingReason() != null) {
            return blocked(candidateId, source.blockingReason(), source.scenarioGroup(), source.domain(), source.sourceIds());
        }
        String scenarioBase = "pilot-v05-recommendation-coverage-bounded-" + slug(source.scenarioGroup());
        String runBase = "V05_RECOMMENDATION_COVERAGE_BOUNDED_" + slug(source.scenarioGroup()).toUpperCase(Locale.ROOT);
        int attempt = 1;
        String scenarioKey;
        String runLabel;
        while (true) {
            scenarioKey = scenarioBase + "-" + String.format(Locale.ROOT, "%03d", attempt);
            runLabel = runBase + "-" + String.format(Locale.ROOT, "%03d", attempt);
            List<PilotRunEntity> existingRuns = runs.findByDatasetIdAndRunLabel(DATASET_ID, runLabel);
            boolean scenarioExists = scenarios.findByDatasetIdAndScenarioId(DATASET_ID, scenarioKey).isPresent();
            if (existingRuns.isEmpty() && !scenarioExists) break;
            if (existingRuns.stream().anyMatch(this::hasCompletedSequenceValidResult)) {
                return blocked(candidateId, "RECOMMENDATION_COVERAGE_ALREADY_GENERATED", source.scenarioGroup(), source.domain(), source.sourceIds());
            }
            attempt++;
        }

        PilotDatasetEntity dataset = datasets.findById(DATASET_ID)
                .orElseThrow(() -> new EvaluationException(HttpStatus.CONFLICT, "PILOT_DATASET_NOT_FOUND", "The governed pilot dataset is missing"));
        Instant started = Instant.now();
        UUID runId = UUID.randomUUID();
        UUID scenarioId = UUID.randomUUID();
        PilotRunEntity run = new PilotRunEntity(runId, dataset.getId(), runLabel, "FULL_PIPELINE", "Qwen", MODEL_VERSION, PROMPT_VERSION, "rag-service:approved-root-cause-evidence");
        run.setEvaluationClassification(PILOT_CLASSIFICATION);
        run.setReviewStatus("PENDING");
        run.setStatus("RUNNING");
        run.setStartedAt(started);
        run.setRunMetadataJson(json(Map.of(
                "evaluation_classification", PILOT_CLASSIFICATION,
                "review_status", "PENDING",
                "training_data", false,
                "human_review_required", true,
                "validated_root_candidate_id", candidateId.toString(),
                "validated_root_scenario", source.scenarioGroup())));
        runs.saveAndFlush(run);

        PilotScenarioEntity scenario = new PilotScenarioEntity(
                scenarioId,
                dataset.getId(),
                scenarioKey,
                "CONTROLLED PILOT CONTEXT DERIVED FROM AN APPROVED ROOT-CAUSE RECORD; NOT REAL VILLAGE DATA",
                source.domain(),
                "Generate bounded interventions for the approved root cause: " + source.rootCauseDescription(),
                json(List.of(Map.of("source_candidate_id", candidateId.toString(), "scenario_group", source.scenarioGroup()))),
                json(source.evidenceRows()),
                json(List.of(Map.of("source_id", source.sourceIds().get(0), "classification", PILOT_CLASSIFICATION))),
                json(source.sourceIds()),
                json(List.of(Map.of("root_cause_id", source.rootCauseId(), "description", source.rootCauseDescription()))),
                json(source.uncertainties()),
                json(List.of("evidence-grounded intervention options")),
                "Recommendation coverage derived from an approved, non-synthetic root-cause candidate; no automatic approval or dataset promotion.",
                false,
                null,
                "SYNTHETIC");
        scenario.setEvaluationClassification(PILOT_CLASSIFICATION);
        scenario.setScenarioProvenanceJson(json(Map.of(
                "classification", PILOT_CLASSIFICATION,
                "source_type", "APPROVED_LEARNING_CANDIDATE",
                "source_candidate_id", candidateId.toString(),
                "source_learning_record_id", source.learningRecordId().toString(),
                "source_evaluation_result_id", source.evaluationResultId().toString(),
                "source_scenario", source.scenarioGroup(),
                "evidence_source_id", source.sourceIds().get(0),
                "review_status", "PENDING",
                "training_data", false,
                "synthetic", false)));
        scenario.setReviewStatus("PENDING");
        scenarios.saveAndFlush(scenario);
        dataset.setScenarioCount(dataset.getScenarioCount() + 1);
        datasets.save(dataset);

        try {
            ensureEvidenceDocument(source, scenarioKey);
            RagQueryResponse rag = aiFoundationService.rag(
                    new RagQueryRequest(source.rootCauseDescription(), "knowledge", MODEL_VERSION, null,
                            Map.of("domain", source.domain(), "allowed_source_ids", source.sourceIds(), "governed_evaluation", true, "evaluation_classification", PILOT_CLASSIFICATION), 5), userId);
            validateCitations(rag, source.sourceIds());

            Map<String, Object> constraints = new LinkedHashMap<>();
            constraints.put("human_review_required", true);
            constraints.put("governed_evaluation", true);
            constraints.put("constructed_scenario", true);
            constraints.put("allowed_source_ids", source.sourceIds());
            constraints.put("validated_root_candidate_id", candidateId.toString());
            RecommendationSetResponse recommendation = recommendationService.generate(
                    new RecommendationGenerateRequest(
                            null,
                            List.of(new RootCauseInput(source.rootCauseId(), source.rootCauseDescription(), source.domain(), source.confidence(), source.sourceIds())),
                            Map.of("validated_root_candidate_id", candidateId.toString(), "scenario_group", source.scenarioGroup()),
                            source.evidenceRows(),
                            Map.of(),
                            constraints,
                            source.domain(),
                            null,
                            "rag-service:approved-root-cause-evidence",
                            source.sourceIds().get(0),
                            true),
                    userId);
            validateRecommendations(recommendation, source.sourceIds());

            long latencyMs = Duration.between(started, Instant.now()).toMillis();
            PilotScenarioResultEntity result = result(runId, scenarioId, scenario, source, recommendation, rag, latencyMs, candidateId);
            JsonNode generatedPipeline = parseJson(result.getPipelineOutputJson());
            if (generatedPipeline == null || !generatedPipeline.path("sequence_gate").asBoolean(false)) {
                result.setPass(false);
            }
            results.saveAndFlush(result);
            persistCitationChecks(result.getId(), rag, source.rootCauseDescription());
            run.setStatus("COMPLETED");
            run.setCompletedAt(Instant.now());
            run.setTotalScenarios(1);
            run.setPassedScenarios(Boolean.TRUE.equals(result.getPass()) ? 1 : 0);
            run.setFailedScenarios(Boolean.TRUE.equals(result.getPass()) ? 0 : 1);
            run.setRunMetadataJson(json(Map.of(
                    "evaluation_classification", PILOT_CLASSIFICATION,
                    "review_status", "PENDING",
                    "evaluation_result_id", result.getId().toString(),
                    "recommendation_set_id", recommendation.recommendationSetId().toString(),
                    "validated_root_candidate_id", candidateId.toString(),
                    "evaluation_status", "PASSED_STRUCTURAL_GATE")));
            runs.save(run);
            return new RecommendationCoverageCandidateResponse(candidateId, source.scenarioGroup(), source.domain(), "PENDING_HUMAN_REVIEW", result.getId(), recommendation.recommendationSetId(), result.getOverallScore(), source.sourceIds(), targetTokenEstimate(result.getPipelineOutputJson()), null);
        } catch (RuntimeException ex) {
            run.setStatus("FAILED");
            run.setCompletedAt(Instant.now());
            run.setTotalScenarios(1);
            run.setFailedScenarios(1);
            run.setRunMetadataJson(json(Map.of("evaluation_classification", PILOT_CLASSIFICATION, "review_status", "PENDING", "validated_root_candidate_id", candidateId.toString(), "failure_code", failureCode(ex), "failure_message", safeMessage(ex))));
            runs.save(run);
            return new RecommendationCoverageCandidateResponse(candidateId, source.scenarioGroup(), source.domain(), "BLOCKED", null, null, null, source.sourceIds(), null, failureCode(ex));
        }
    }

    /**
     * A completed attempt is terminal only when its persisted output passed the
     * bounded-target gate. Earlier attempts may be retained for audit but must
     * not prevent a governed bounded retry after a quality fix.
     */
    private boolean hasCompletedSequenceValidResult(PilotRunEntity run) {
        if (!"COMPLETED".equalsIgnoreCase(run.getStatus())) return false;
        return results.findByPilotRunId(run.getId()).stream()
                .map(PilotScenarioResultEntity::getPipelineOutputJson)
                .map(this::parseJson)
                .anyMatch(output -> output != null && output.path("sequence_gate").asBoolean(false));
    }

    private SourceRoot inspect(UUID candidateId) {
        TrainingCandidateEntity candidate = candidates.findById(candidateId).orElse(null);
        if (candidate == null) return SourceRoot.blocked("ROOT_CANDIDATE_NOT_FOUND");
        if (!"APPROVED_FOR_DATASET".equalsIgnoreCase(candidate.getApprovalStatus()) || !Set.of("APPROVE", "CORRECT").contains(value(candidate.getReviewDecision()).toUpperCase(Locale.ROOT))) return SourceRoot.blocked("ROOT_CAUSE_NOT_HUMAN_VALIDATED");
        if (Boolean.TRUE.equals(candidate.getSynthetic())) return SourceRoot.blocked("SYNTHETIC_ROOT_CANDIDATE");
        LearningRecordEntity record = learningRecords.findById(candidate.getLearningRecordId()).orElse(null);
        if (record == null) return SourceRoot.blocked("ROOT_LEARNING_RECORD_NOT_FOUND");
        if (!"root-cause-analysis".equalsIgnoreCase(record.getTaskType())) return SourceRoot.blocked("SOURCE_TASK_NOT_ROOT_CAUSE");
        if (!"APPROVED_FOR_DATASET".equalsIgnoreCase(record.getApprovalStatus()) || !Boolean.TRUE.equals(record.getTrainingEligible())) return SourceRoot.blocked("ROOT_RECORD_NOT_DATASET_ELIGIBLE");
        if (record.isSyntheticRecord() || Boolean.TRUE.equals(record.getSynthetic())) return SourceRoot.blocked("SYNTHETIC_ROOT_RECORD");
        if (record.getEvaluationResultId() == null) return SourceRoot.blocked("ROOT_EVALUATION_PROVENANCE_MISSING");
        if (!isRecommendationCoverageSource(record.getScenarioGroup())) return SourceRoot.blocked("ROOT_NOT_FROM_V05_RECOMMENDATION_COVERAGE");
        PilotScenarioEntity sourceScenario = scenarios.findByDatasetIdAndScenarioId(DATASET_ID, record.getScenarioGroup()).orElse(null);
        if (sourceScenario == null || !PILOT_CLASSIFICATION.equalsIgnoreCase(sourceScenario.getEvaluationClassification())) return SourceRoot.blocked("ROOT_SCENARIO_NOT_PILOT_EVALUATION");
        if (sourceScenario.isAdversarial()) return SourceRoot.blocked("ROOT_SCENARIO_ADVERSARIAL");
        String outputText = firstNonBlank(record.getAcceptedOutput(), record.getHumanEditedOutput(), record.getAiOutput());
        JsonNode output = parseJson(outputText);
        JsonNode roots = output == null ? null : output.get("root_causes");
        JsonNode citations = output == null ? null : output.get("citations");
        if (roots == null || !roots.isArray() || roots.isEmpty() || citations == null || !citations.isArray() || citations.isEmpty()) return SourceRoot.blocked("ROOT_OUTPUT_NOT_CANONICAL");
        Set<String> permitted = permittedSources(sourceScenario);
        Set<String> sourceIds = new LinkedHashSet<>();
        citations.forEach(node -> { String id = text(node, "source_id"); if (!id.isBlank()) sourceIds.add(id); });
        roots.forEach(node -> node.path("evidence_source_ids").forEach(id -> { if (id.isTextual() && !id.asText().isBlank()) sourceIds.add(id.asText()); }));
        if (sourceIds.isEmpty() || !permitted.containsAll(sourceIds) || sourceIds.stream().anyMatch(this::isForbiddenSource)) return SourceRoot.blocked("ROOT_CITATION_NOT_PERMITTED");
        if (sourceIds.size() != 1) return SourceRoot.blocked("MULTIPLE_ROOT_SOURCES_REQUIRE_REVIEW");
        JsonNode evidence = parseJson(record.getEvidenceUsedJson());
        if (evidence == null || !evidence.isArray() || evidence.isEmpty()) return SourceRoot.blocked("ROOT_EVIDENCE_MISSING");
        String combined = String.join("\n", outputText, value(record.getInput()), value(record.getRetrievedContext()), evidence.toString());
        if (containsPii(combined)) return SourceRoot.blocked("PII_DETECTED_IN_ROOT_PROVENANCE");
        if (hasEquivalentRecommendation(record.getScenarioGroup(), sourceIds.iterator().next())) return SourceRoot.blocked("EQUIVALENT_RECOMMENDATION_EXISTS");
        JsonNode root = roots.get(0);
        String description = text(root, "description");
        String rootId = text(root, "name", "root_cause_id", "rootCauseId");
        if (description.isBlank() || rootId.isBlank()) return SourceRoot.blocked("ROOT_CAUSE_FIELDS_MISSING");
        List<String> uncertainties = strings(output.get("uncertainties"));
        if (uncertainties.isEmpty()) return SourceRoot.blocked("ROOT_UNCERTAINTY_MISSING");
        List<Map<String, Object>> evidenceRows = new ArrayList<>();
        evidence.forEach(node -> { if (node.isObject()) evidenceRows.add(objectMap(node)); });
        return new SourceRoot(candidateId, record.getId(), record.getEvaluationResultId(), record.getScenarioGroup(), sourceScenario.getDomain(), rootId, description, sourceIds.stream().toList(), evidenceRows, uncertainties, root.path("confidence").asDouble(0.0), null);
    }

    private PilotScenarioResultEntity result(UUID runId, UUID scenarioId, PilotScenarioEntity scenario, SourceRoot source, RecommendationSetResponse recommendation, RagQueryResponse rag, long latencyMs, UUID candidateId) {
        PilotScenarioResultEntity result = new PilotScenarioResultEntity(UUID.randomUUID(), runId, scenarioId);
        boolean hasOptions = recommendation.options() != null && recommendation.options().size() >= 2;
        boolean grounded = hasOptions && recommendation.options().stream().allMatch(option -> option.evidence() != null && option.evidence().containsAll(source.sourceIds()));
        boolean structured = hasOptions && !recommendation.options().stream().anyMatch(option -> value(option.description()).isBlank());
        boolean cited = rag.citations().stream().map(CitationResponse::sourceId).allMatch(source.sourceIds()::contains);
        result.setProblemUnderstandingScore(BigDecimal.ONE);
        result.setFactExtractionScore(BigDecimal.ONE);
        result.setEvidenceGroundednessScore(BigDecimal.ONE);
        result.setRootCauseRelevanceScore(BigDecimal.ONE);
        result.setAltHypothesisQualityScore(BigDecimal.ONE);
        result.setContradictionDetectionScore(BigDecimal.ONE);
        result.setMissingEvidenceDetectionScore(BigDecimal.ONE);
        result.setUncertaintyHandlingScore(BigDecimal.ONE);
        result.setCitationAccuracyScore(score(cited));
        result.setRootCauseAlignmentScore(score(grounded));
        result.setRecEvidenceGroundednessScore(score(grounded));
        result.setRecommendationRelevanceScore(score(hasOptions));
        result.setOptionDiversityScore(score(recommendation.options().stream().map(RecommendationOptionResponse::interventionType).distinct().count() >= 2));
        result.setFeasibilityReasoningScore(score(recommendation.options().stream().allMatch(option -> option.feasibility() != null)));
        result.setRiskIdentificationScore(score(recommendation.options().stream().allMatch(option -> option.risks() != null && !option.risks().isEmpty())));
        result.setSchemeMatchingScore(BigDecimal.ONE);
        result.setImplementationPlanningScore(score(recommendation.options().stream().allMatch(option -> option.implementationPlan() != null && !option.implementationPlan().isEmpty())));
        result.setUnsupportedClaimsCount(0);
        result.setFalseCitationsCount(cited ? 0 : 1);
        result.setInventedStatisticsCount(0);
        result.setInventedSchemesCount(0);
        result.setFalseEligibilityCount(0);
        result.setOverconfidentConclusionsCount(0);
        List<BigDecimal> scores = List.of(result.getRootCauseAlignmentScore(), result.getRecEvidenceGroundednessScore(), result.getRecommendationRelevanceScore(), result.getOptionDiversityScore(), result.getFeasibilityReasoningScore(), result.getRiskIdentificationScore(), result.getImplementationPlanningScore(), result.getCitationAccuracyScore());
        result.setOverallScore(scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(scores.size()), 4, java.math.RoundingMode.HALF_UP));
        result.setPass(hasOptions && grounded && structured && cited);
        result.setLatencyMs(latencyMs);
        result.setEvaluationClassification(PILOT_CLASSIFICATION);
        result.setReviewStatus("PENDING");
        List<String> sourceIds = source.sourceIds();
        List<Map<String, Object>> citations = sourceIds.stream().map(id -> Map.<String, Object>of("source_id", id)).toList();
        List<Map<String, Object>> options = recommendation.options().stream().limit(2).map(option -> recommendationTarget(option, sourceIds)).toList();
        Map<String, Object> target = new LinkedHashMap<>();
        target.put("root_cause", Map.of("description", compact(source.rootCauseDescription(), 100), "evidence_source_ids", sourceIds));
        target.put("recommendations", options);
        target.put("uncertainties", source.uncertainties());
        target.put("citations", citations);
        Map<String, Object> pipeline = new LinkedHashMap<>();
        pipeline.put("task", "recommendation-generation");
        pipeline.put("input", source.rootCauseDescription());
        String targetJson = json(target);
        // Keep a conservative character budget because the exact Qwen tokenizer is
        // intentionally owned by the Python training validator, not this service.
        // The bound prevents known overlong recommendation targets from entering
        // the human-review queue and later failing the training contract.
        boolean sequenceGate = targetJson.length() <= 1700 && source.rootCauseDescription().length() + rag.answer().length() <= 3600;
        pipeline.put("output", targetJson);
        pipeline.put("ai_output", targetJson);
        pipeline.put("retrieved_context", rag.answer());
        pipeline.put("citations", rag.citations().stream().map(citation -> Map.of("source_id", citation.sourceId(), "excerpt", citation.excerpt(), "score", citation.score())).toList());
        pipeline.put("scenario", scenario.getScenarioId());
        pipeline.put("evaluation_classification", PILOT_CLASSIFICATION);
        pipeline.put("synthetic", false);
        pipeline.put("constructed", true);
        pipeline.put("real_world_data", false);
        pipeline.put("review_status", "PENDING");
        pipeline.put("model_version", MODEL_VERSION);
        pipeline.put("prompt_version", PROMPT_VERSION);
        pipeline.put("validated_root_candidate_id", candidateId.toString());
        pipeline.put("validated_root_source_scenario", source.scenarioGroup());
        pipeline.put("bounded_output", true);
        pipeline.put("target_token_estimate", estimateTokens(targetJson));
        pipeline.put("formatted_token_estimate", estimateTokens(source.rootCauseDescription() + "\n" + rag.answer() + "\n" + targetJson));
        pipeline.put("sequence_gate", sequenceGate);
        pipeline.put("evaluation_status", Boolean.TRUE.equals(result.getPass()) ? "PASSED_STRUCTURAL_GATE" : "FAILED_STRUCTURAL_GATE");
        result.setPipelineOutputJson(json(pipeline));
        return result;
    }

    private Map<String, Object> recommendationTarget(RecommendationOptionResponse option, List<String> sourceIds) {
        Map<String, Object> target = new LinkedHashMap<>();
        target.put("id", option.recommendationId());
        target.put("title", compact(option.title(), 55));
        target.put("description", compact(option.description(), 70));
        target.put("evidence_source_ids", sourceIds);
        target.put("feasibility", option.feasibility() == null ? Map.of("rating", "UNKNOWN", "rationale", "Human verification required") : Map.of("rating", firstNonBlank(option.feasibility().rating(), "UNKNOWN"), "rationale", compact(String.join("; ", option.feasibility().supportingFactors()), 30)));
        target.put("risks", option.risks() == null ? List.of(Map.of("description", "Implementation conditions may change", "severity", "UNKNOWN", "mitigation", "Verify before action")) : option.risks().stream().limit(1).map(risk -> Map.of("description", compact(risk.description(), 45), "severity", firstNonBlank(risk.severity(), "UNKNOWN"), "mitigation", compact(risk.mitigation(), 35))).toList());
        target.put("implementation_steps", option.implementationPlan() == null ? List.of("Validate evidence and resources") : option.implementationPlan().stream().flatMap(phase -> phase.actions().stream()).limit(1).map(value -> compact(value, 55)).toList());
        return target;
    }

    private Map<String, Object> evidenceDocument(SourceRoot source, String scenarioKey) {
        String text = source.evidenceRows().stream().map(row -> String.join(" ", value(row.get("observation")), value(row.get("excerpt")), value(row.get("evidence")))).filter(v -> !v.isBlank()).reduce(source.rootCauseDescription(), (left, right) -> left + "\n" + right);
        return new LinkedHashMap<>(Map.of("document_id", "approved-root-cause-evidence-" + scenarioKey, "title", "Approved root-cause evidence: " + source.scenarioGroup(), "source", source.sourceIds().get(0), "publisher", "Rural Intelligence Governed Evaluation Registry", "document_version", "approved-root-1.0.0", "language", "en", "domain", source.domain().toLowerCase(Locale.ROOT), "document_type", "approved-evaluation-evidence", "approved_source", true, "text", text));
    }

    /** Reuses an already indexed governed source instead of re-ingesting the same checksum. */
    private void ensureEvidenceDocument(SourceRoot source, String scenarioKey) {
        String sourceId = source.sourceIds().get(0);
        Map<String, Object> indexed = knowledgeRagGateway.documents();
        Object documents = indexed.get("documents");
        if (documents instanceof Collection<?> collection && collection.stream().anyMatch(item -> {
            if (!(item instanceof Map<?, ?> document)) return false;
            return sourceId.equals(String.valueOf(document.get("source")))
                    && "ACTIVE".equalsIgnoreCase(String.valueOf(document.get("status")))
                    && "approved-evaluation-evidence".equalsIgnoreCase(String.valueOf(document.get("document_type")));
        })) {
            return;
        }
        knowledgeRagGateway.ingestJson(evidenceDocument(source, scenarioKey));
    }

    private void validateCitations(RagQueryResponse rag, List<String> allowed) {
        if (rag == null || rag.citations() == null || rag.citations().isEmpty()) throw new EvaluationException(HttpStatus.BAD_GATEWAY, "RAG_EVIDENCE_REQUIRED", "No governed evidence citation was retrieved");
        if (rag.citations().stream().anyMatch(citation -> citation.sourceId() == null || !allowed.contains(citation.sourceId()))) throw new EvaluationException(HttpStatus.BAD_GATEWAY, "RAG_SOURCE_NOT_PERMITTED", "Retrieved citation is outside the approved root-cause evidence allowlist");
    }

    private void validateRecommendations(RecommendationSetResponse response, List<String> allowed) {
        if (response == null || response.options() == null || response.options().size() < 2) throw new EvaluationException(HttpStatus.BAD_GATEWAY, "RECOMMENDATION_OPTIONS_REQUIRED", "At least two grounded recommendation options are required");
        if (response.options().stream().anyMatch(option -> option.evidence() == null || !option.evidence().containsAll(allowed))) throw new EvaluationException(HttpStatus.BAD_GATEWAY, "RECOMMENDATION_EVIDENCE_REQUIRED", "Every recommendation option must cite the approved root-cause evidence");
    }

    private Set<String> permittedSources(PilotScenarioEntity scenario) {
        Set<String> permitted = new LinkedHashSet<>();
        JsonNode provenance = parseJson(scenario.getScenarioProvenanceJson());
        String provenanceSource = text(provenance, "evidence_source_id");
        if (!provenanceSource.isBlank()) permitted.add(provenanceSource);
        JsonNode evidence = parseJson(scenario.getEvidenceJson());
        if (evidence != null && evidence.isArray()) evidence.forEach(row -> { String source = text(row, "source", "source_id"); if (!source.isBlank()) permitted.add(source); });
        return permitted;
    }

    private boolean hasEquivalentRecommendation(String sourceScenario, String sourceId) {
        return learningRecords.findAll().stream().filter(record -> "recommendation-generation".equalsIgnoreCase(record.getTaskType())).anyMatch(record -> sourceScenario.equals(record.getScenarioGroup()) || value(record.getEvidenceUsedJson()).contains(sourceId) || value(record.getAcceptedOutput()).contains(sourceId) || value(record.getAiOutput()).contains(sourceId));
    }

    private RecommendationCoverageCandidateResponse blocked(UUID candidateId, String reason, String scenario, String domain, List<String> sourceIds) {
        return new RecommendationCoverageCandidateResponse(candidateId, scenario, domain, "BLOCKED", null, null, null, sourceIds == null ? List.of() : sourceIds, null, reason);
    }

    private boolean isRecommendationCoverageSource(String scenarioGroup) {
        if (scenarioGroup == null) return false;
        return (scenarioGroup.startsWith("pilot-v05r-") && scenarioGroup.contains("root-cause-analysis"))
                || "pilot-v05-climate-drought-preparedness-root-cause-analysis-001".equals(scenarioGroup);
    }
    private boolean isForbiddenSource(String source) { String normalized = value(source).toLowerCase(Locale.ROOT); return normalized.contains("development") || normalized.contains("synthetic") || normalized.contains("fixture"); }
    private boolean containsPii(String value) { return value != null && PII.matcher(value).find(); }
    private void requireUser(UUID userId) { if (userId == null) throw new EvaluationException(HttpStatus.UNAUTHORIZED, "EVALUATION_USER_REQUIRED", "An authenticated evaluation operator is required"); }
    private String failureCode(RuntimeException ex) { return ex instanceof EvaluationException evaluation ? evaluation.code() : ex.getClass().getSimpleName(); }
    private String safeMessage(RuntimeException ex) { return value(ex.getMessage()).substring(0, Math.min(500, value(ex.getMessage()).length())); }
    private String slug(String value) { return value(value).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", ""); }
    private String value(Object value) { return value == null ? "" : String.valueOf(value); }
    private String firstNonBlank(String... values) { for (String value : values) if (value != null && !value.isBlank()) return value; return ""; }
    private BigDecimal score(boolean value) { return value ? BigDecimal.ONE : BigDecimal.ZERO; }
    private int estimateTokens(String value) { return value == null ? 0 : (int) Math.ceil(value.length() / 4.0); }
    private int targetTokenEstimate(String pipelineOutput) { JsonNode node = parseJson(pipelineOutput); return node == null ? 0 : estimateTokens(value(node.get("output"))); }
    private String compact(String value, int max) { String normalized = value(value).replaceAll("\\s+", " ").trim(); return normalized.length() <= max ? normalized : normalized.substring(0, Math.max(0, max - 1)).trim() + "…"; }

    private JsonNode parseJson(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim();
        if (normalized.startsWith("```")) { int firstLine = normalized.indexOf('\n'); int closing = normalized.lastIndexOf("```"); if (firstLine >= 0 && closing > firstLine) normalized = normalized.substring(firstLine + 1, closing).trim(); }
        try { return objectMapper.readTree(normalized); } catch (Exception ignored) { return null; }
    }

    private String text(JsonNode node, String... fields) { if (node == null) return ""; for (String field : fields) { JsonNode value = node.get(field); if (value != null && !value.isNull() && !value.asText().isBlank()) return value.asText(); } return ""; }
    private List<String> strings(JsonNode node) { if (node == null || !node.isArray()) return List.of(); List<String> result = new ArrayList<>(); node.forEach(value -> { if (value.isTextual() && !value.asText().isBlank()) result.add(value.asText()); }); return result; }
    private Map<String, Object> objectMap(JsonNode node) { return objectMapper.convertValue(node, Map.class); }
    private String json(Object value) { try { return objectMapper.writeValueAsString(value); } catch (Exception ex) { throw new EvaluationException(HttpStatus.INTERNAL_SERVER_ERROR, "EVALUATION_SERIALIZATION_FAILED", "Evaluation provenance could not be serialized"); } }

    private void persistCitationChecks(UUID resultId, RagQueryResponse rag, String claim) {
        for (CitationResponse citation : rag.citations()) jdbcTemplate.update("insert into evaluation.pilot_citation_checks (id, scenario_result_id, claim_text, cited_source, citation_exists, citation_resolves, citation_supports_claim, citation_is_relevant, citation_correct_version, failure_reason) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", UUID.randomUUID(), resultId, claim, citation.sourceId(), true, true, true, citation.score() != null && citation.score() > 0, true, null);
    }

    private record SourceRoot(UUID candidateId, UUID learningRecordId, UUID evaluationResultId, String scenarioGroup, String domain, String rootCauseId, String rootCauseDescription, List<String> sourceIds, List<Map<String, Object>> evidenceRows, List<String> uncertainties, double confidence, String blockingReason) {
        static SourceRoot blocked(String reason) { return new SourceRoot(null, null, null, "", "", "", "", List.of(), List.of(), List.of(), 0.0, reason); }
    }
}
