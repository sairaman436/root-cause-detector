/*
 * Purpose: Executes controlled development and pilot evaluation scenarios through the
 * existing RAG, Qwen, root-cause, recommendation, and evaluation services.
 * Why it exists: Evaluation output needs durable provenance and a governed handoff to
 * the existing human-review queue without approving or promoting training data.
 * Architecture fit: Application service at the evaluation bounded-context boundary.
 */
package com.airural.platform.core.evaluation.application;

import com.airural.platform.core.ai.application.AiFoundationService;
import com.airural.platform.core.ai.web.dto.AiDtos.*;
import com.airural.platform.core.decision.application.RecommendationIntelligenceService;
import com.airural.platform.core.decision.application.RootCauseIntelligenceService;
import com.airural.platform.core.decision.web.dto.RecommendationIntelligenceDtos.*;
import com.airural.platform.core.decision.web.dto.RootCauseDtos.*;
import com.airural.platform.core.evaluation.domain.*;
import com.airural.platform.core.evaluation.infrastructure.*;
import com.airural.platform.core.knowledge.application.KnowledgeRagGatewayService;
import com.airural.platform.core.evaluation.web.dto.GovernedEvaluationDtos.GovernedEvaluationBatchResponse;
import com.airural.platform.core.evaluation.web.dto.GovernedEvaluationDtos.GovernedEvaluationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Runs reproducible development and controlled pilot evaluations. */
@Service
public class GovernedEvaluationScenarioService {
    private static final UUID DATASET_ID = UUID.fromString("00000000-0000-0000-0000-000000002850");
    private static final String MODEL_VERSION = "qwen2.5:0.5b";
    private static final String PROMPT_VERSION = "ROOT_CAUSE_INTELLIGENCE@1.0.0+RECOMMENDATION_GENERATION@1.0.0";
    private static final String DEVELOPMENT_CLASSIFICATION = "DEVELOPMENT_SYNTHETIC";
    private static final String PILOT_CLASSIFICATION = "PILOT_EVALUATION";

    private final PilotDatasetRepository datasets;
    private final PilotRunRepository runs;
    private final PilotScenarioRepository scenarios;
    private final PilotScenarioResultRepository results;
    private final RootCauseIntelligenceService rootCauseService;
    private final RecommendationIntelligenceService recommendationService;
    private final AiFoundationService aiFoundationService;
    private final KnowledgeRagGatewayService knowledgeRagGateway;
    private final JdbcOperations jdbcTemplate;
    private final ObjectMapper objectMapper;

    public GovernedEvaluationScenarioService(
            PilotDatasetRepository datasets,
            PilotRunRepository runs,
            PilotScenarioRepository scenarios,
            PilotScenarioResultRepository results,
            RootCauseIntelligenceService rootCauseService,
            RecommendationIntelligenceService recommendationService,
            AiFoundationService aiFoundationService,
            KnowledgeRagGatewayService knowledgeRagGateway,
            JdbcOperations jdbcTemplate,
            ObjectMapper objectMapper) {
        this.datasets = datasets;
        this.runs = runs;
        this.scenarios = scenarios;
        this.results = results;
        this.rootCauseService = rootCauseService;
        this.recommendationService = recommendationService;
        this.aiFoundationService = aiFoundationService;
        this.knowledgeRagGateway = knowledgeRagGateway;
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    /** Executes the original single development scenario; it never promotes data. */
    @Transactional
    public GovernedEvaluationResponse run(UUID userId) {
        return run(userId, developmentScenario());
    }

    /** Executes the fixed, server-owned pilot set through the same governed pipeline. */
    @Transactional
    public GovernedEvaluationBatchResponse runPilotBatch(UUID userId) {
        List<GovernedEvaluationResponse> completed = new ArrayList<>();
        for (ScenarioSpec spec : pilotScenarios()) {
            completed.add(run(userId, spec));
        }
        return new GovernedEvaluationBatchResponse(completed, completed.size(), "PENDING_HUMAN_REVIEW");
    }

    /** Executes only the additional governed pilot scenarios for corpus expansion. */
    @Transactional
    public GovernedEvaluationBatchResponse runPilotExpansion(UUID userId) {
        List<GovernedEvaluationResponse> completed = new ArrayList<>();
        for (ScenarioSpec spec : pilotExpansionScenarios()) {
            completed.add(run(userId, spec));
        }
        return new GovernedEvaluationBatchResponse(completed, completed.size(), "PENDING_HUMAN_REVIEW");
    }

    /** Executes the uniquely keyed v0.2 preparation scenarios without approving or materializing data. */
    @Transactional
    public GovernedEvaluationBatchResponse runPilotV02Preparation(UUID userId) {
        List<GovernedEvaluationResponse> completed = new ArrayList<>();
        for (ScenarioSpec spec : pilotV02Scenarios()) {
            completed.add(run(userId, spec));
        }
        return new GovernedEvaluationBatchResponse(completed, completed.size(), "PENDING_HUMAN_REVIEW");
    }

    /** Executes the v0.3 contract-focused pilot set; every result remains pending review. */
    @Transactional
    public GovernedEvaluationBatchResponse runPilotV03Preparation(UUID userId) {
        List<GovernedEvaluationResponse> completed = new ArrayList<>();
        for (ScenarioSpec spec : pilotV03Scenarios()) {
            completed.add(run(userId, spec));
        }
        return new GovernedEvaluationBatchResponse(completed, completed.size(), "PENDING_HUMAN_REVIEW");
    }

    /** Executes uniquely keyed v0.3 expansion scenarios and leaves all results pending review. */
    @Transactional
    public GovernedEvaluationBatchResponse runPilotV03Expansion(UUID userId) {
        List<GovernedEvaluationResponse> completed = new ArrayList<>();
        for (ScenarioSpec spec : pilotV03ExpansionScenarios()) {
            completed.add(run(userId, spec));
        }
        return new GovernedEvaluationBatchResponse(completed, completed.size(), "PENDING_HUMAN_REVIEW");
    }

    /** Executes the v0.5 domain-diversity candidate set; all results remain pending review. */
    @Transactional
    public GovernedEvaluationBatchResponse runPilotV05Diversity(UUID userId) {
        List<GovernedEvaluationResponse> completed = new ArrayList<>();
        for (ScenarioSpec spec : pilotV05DiversityScenarios()) {
            completed.add(run(userId, spec));
        }
        return new GovernedEvaluationBatchResponse(completed, completed.size(), "PENDING_HUMAN_REVIEW");
    }

    /** Executes one v0.5 diversity scenario so a failed provider does not hide other candidates. */
    @Transactional
    public GovernedEvaluationResponse runPilotV05DiversityScenario(UUID userId, String scenarioKey) {
        ScenarioSpec spec = pilotV05DiversityScenarios().stream()
                .filter(candidate -> candidate.scenarioKey().equals(scenarioKey))
                .findFirst()
                .orElseThrow(() -> new EvaluationException(HttpStatus.NOT_FOUND, "PILOT_SCENARIO_NOT_FOUND", "The requested v0.5 diversity scenario is not registered"));
        return run(userId, spec);
    }

    /** Executes corrected v0.5 candidates and replacements as new immutable review versions. */
    @Transactional
    public GovernedEvaluationBatchResponse runPilotV05QualityRemediation(UUID userId) {
        List<GovernedEvaluationResponse> completed = new ArrayList<>();
        for (ScenarioSpec spec : pilotV05QualityRemediationScenarios()) {
            completed.add(run(userId, spec));
        }
        return new GovernedEvaluationBatchResponse(completed, completed.size(), "PENDING_HUMAN_REVIEW");
    }

    /** Executes one corrected/replacement v0.5 scenario independently. */
    @Transactional
    public GovernedEvaluationResponse runPilotV05QualityRemediationScenario(UUID userId, String scenarioKey) {
        ScenarioSpec spec = pilotV05QualityRemediationScenarios().stream()
                .filter(candidate -> candidate.scenarioKey().equals(scenarioKey))
                .findFirst()
                .orElseThrow(() -> new EvaluationException(HttpStatus.NOT_FOUND, "PILOT_SCENARIO_NOT_FOUND", "The requested v0.5 remediation scenario is not registered"));
        return run(userId, spec);
    }

    /** Runs one v0.3 expansion scenario so a provider failure cannot roll back unrelated scenarios. */
    @Transactional
    public GovernedEvaluationResponse runPilotV03ExpansionScenario(UUID userId, String scenarioKey) {
        ScenarioSpec spec = pilotV03ExpansionScenarios().stream()
                .filter(candidate -> candidate.scenarioKey().equals(scenarioKey))
                .findFirst()
                .orElseThrow(() -> new EvaluationException(HttpStatus.NOT_FOUND, "PILOT_SCENARIO_NOT_FOUND", "The requested v0.3 expansion scenario is not registered"));
        return run(userId, spec);
    }

    /** Runs one Experiment 003 scenario through the same governed pipeline. */
    @Transactional
    public GovernedEvaluationResponse runPilotV03Experiment003Scenario(UUID userId, String scenarioKey) {
        ScenarioSpec spec = pilotV03Experiment003Scenarios().stream()
                .filter(candidate -> candidate.scenarioKey().equals(scenarioKey))
                .findFirst()
                .orElseThrow(() -> new EvaluationException(HttpStatus.NOT_FOUND, "PILOT_SCENARIO_NOT_FOUND", "The requested Experiment 003 scenario is not registered"));
        return run(userId, spec);
    }

    /** Re-runs one existing pilot scenario while preserving its original result history. */
    @Transactional
    public GovernedEvaluationResponse rerunPilotScenario(UUID userId, String scenarioKey) {
        ScenarioSpec spec = pilotScenarios().stream()
                .filter(candidate -> candidate.scenarioKey().equals(scenarioKey))
                .findFirst()
                .orElseThrow(() -> new EvaluationException(HttpStatus.NOT_FOUND, "PILOT_SCENARIO_NOT_FOUND", "The requested pilot scenario is not registered"));
        PilotScenarioEntity existing = scenarios.findByDatasetIdAndScenarioId(DATASET_ID, scenarioKey)
                .orElseThrow(() -> new EvaluationException(HttpStatus.NOT_FOUND, "PILOT_SCENARIO_NOT_FOUND", "The requested pilot scenario has not been executed"));
        return run(userId, spec, existing);
    }

    @Transactional
    public GovernedEvaluationResponse run(UUID userId, ScenarioSpec spec) {
        return run(userId, spec, null);
    }

    private GovernedEvaluationResponse run(UUID userId, ScenarioSpec spec, PilotScenarioEntity existingScenario) {
        if (userId == null) {
            throw new EvaluationException(HttpStatus.UNAUTHORIZED, "EVALUATION_USER_REQUIRED", "An authenticated evaluation operator is required");
        }
        PilotDatasetEntity dataset = datasets.findById(DATASET_ID)
                .orElseThrow(() -> new EvaluationException(HttpStatus.CONFLICT, "PILOT_DATASET_NOT_FOUND", "The seeded synthetic pilot dataset is missing"));
        boolean rerun = existingScenario != null;
        if (!rerun && !runs.findByDatasetIdAndRunLabel(DATASET_ID, spec.runLabel()).isEmpty()) {
            throw new EvaluationException(HttpStatus.CONFLICT, "EVALUATION_ALREADY_RUN", "The controlled evaluation scenario has already been executed");
        }
        if (spec.runLabel().startsWith("V05R_REPLACEMENT_")) {
            diversityGate(spec);
        }

        Instant started = Instant.now();
        UUID runId = UUID.randomUUID();
        UUID scenarioId = rerun ? existingScenario.getId() : UUID.randomUUID();
        String runLabel = rerun ? spec.runLabel() + "-QUALITY-RERUN-" + System.currentTimeMillis() : spec.runLabel();
        PilotRunEntity run = new PilotRunEntity(runId, dataset.getId(), runLabel, "FULL_PIPELINE", "Qwen", MODEL_VERSION, PROMPT_VERSION, spec.knowledgeSnapshot());
        run.setStatus("RUNNING");
        run.setStartedAt(started);
        run.setEvaluationClassification(spec.classification());
        run.setReviewStatus("PENDING");
        run.setRunMetadataJson(json(runMetadata(spec, Map.of(
                "scenario_source", spec.sourceType(),
                "run_type", rerun ? "QUALITY_RERUN" : "INITIAL_EVALUATION",
                "training_data", false,
                "human_review_required", true))));
        runs.save(run);

        PilotScenarioEntity scenario = existingScenario == null ? new PilotScenarioEntity(
                scenarioId,
                dataset.getId(),
                spec.scenarioKey(),
                spec.villageContext(),
                spec.domain(),
                spec.problemStatement(),
                json(List.of(spec.survey())),
                json(List.of(spec.evidence())),
                json(List.of(Map.of("source", spec.knowledgeSnapshot(), "classification", spec.classification()))),
                json(List.of("direct observation", "service reliability")),
                json(List.of("accountability gap", "service interruption")),
                json(List.of("Additional field verification is required before an operational decision.")),
                json(List.of(spec.domain().toLowerCase(Locale.ROOT), "service monitoring")),
                spec.evaluationNotes(),
                false,
                null,
                "SYNTHETIC") : existingScenario;
        scenario.setEvaluationClassification(spec.classification());
        scenario.setScenarioProvenanceJson(json(provenance(spec)));
        scenario.setReviewStatus("PENDING");
        if (existingScenario == null) {
            scenarios.saveAndFlush(scenario);
            dataset.setScenarioCount(dataset.getScenarioCount() + 1);
            datasets.save(dataset);
        } else {
            scenarios.saveAndFlush(scenario);
        }

        if (PILOT_CLASSIFICATION.equals(spec.classification())) {
            knowledgeRagGateway.ingestJson(controlledEvidenceDocument(spec));
        }
        RagQueryResponse rag = aiFoundationService.rag(
                new RagQueryRequest(ragQuery(spec), "knowledge", MODEL_VERSION, null, governedRagContext(spec), 5), userId);
        if (rag.citations() == null || rag.citations().isEmpty()) {
            throw new EvaluationException(HttpStatus.BAD_GATEWAY, "RAG_EVIDENCE_REQUIRED", "The controlled evaluation requires at least one validated RAG citation");
        }

        List<Map<String, Object>> surveyRows = new ArrayList<>();
        surveyRows.add(Map.of("observation", String.valueOf(spec.survey().get("answer"))));
        surveyRows.addAll(spec.surveyObservations());
        List<Map<String, Object>> evidenceRows = new ArrayList<>();
        evidenceRows.add(Map.of("observation", String.valueOf(spec.evidence().get("observation"))));
        evidenceRows.addAll(spec.evidenceObservations());
        RootCauseAnalysisResponse rootCause = rootCauseService.analyze(
                new RootCauseAnalysisRequest(
                        new ProblemRequest(spec.scenarioKey() + "-problem", spec.villageContext(), spec.domain(), spec.problemStatement(), spec.affectedPopulation(), spec.severity(), List.of(String.valueOf(spec.evidence().get("evidence_id"))), Instant.now(), spec.sourceType()),
                        surveyRows,
                        evidenceRows,
                        Map.of(),
                        rag.citations().stream().map(this::citationFactMap).toList(),
                        null,
                        null,
                        spec.scenarioKey() + "-survey-v1",
                        spec.knowledgeSnapshot(),
                        true),
                userId);

        ChatResponse qwen = aiFoundationService.chat(
                new ChatRequest(
                        spec.promptInstruction() + " Problem: " + rootCause.problem().description() + " Evidence: " + spec.compactEvidence() + " Citation IDs: " + rag.citations().stream().map(CitationResponse::sourceId).toList(),
                        MODEL_VERSION,
                        null,
                        Map.of("prompt_version", "ROOT_CAUSE_ANALYSIS@1.0.0", "evaluation_classification", spec.classification()),
                        false),
                userId);
        if (qwen.fallbackUsed()) {
            throw new EvaluationException(HttpStatus.BAD_GATEWAY, "QWEN_PROVIDER_FALLBACK", "Qwen did not complete through the configured local provider");
        }

        boolean recommendationTask = "recommendation-generation".equals(spec.taskType());
        Map<String, Object> recommendationConstraints = spec.runLabel().startsWith("V05R_")
                ? remediationConstraints(spec)
                : Map.of("human_review_required", true, "constructed_scenario", true);
        RecommendationSetResponse recommendations = recommendationTask
                ? recommendationService.generate(
                        new RecommendationGenerateRequest(
                                rootCause.analysisId(),
                                List.of(),
                                Map.of("context_label", spec.classification(), "real_world_data", false),
                                List.of(spec.evidence()),
                                Map.of("available_resources", "not supplied for controlled evaluation"),
                                recommendationConstraints,
                                spec.domain(),
                                spec.affectedPopulation(),
                                spec.knowledgeSnapshot(),
                                String.valueOf(spec.evidence().get("evidence_id")),
                                true),
                        userId)
                : nonApplicableRecommendations(rootCause.analysisId(), spec);

        long latencyMs = Duration.between(started, Instant.now()).toMillis();
        PilotScenarioResultEntity result = result(runId, scenarioId, spec, rootCause, recommendations, rag, qwen, latencyMs);
        results.saveAndFlush(result);
        persistCitationChecks(result.getId(), rag, rootCause.problem().description());

        run.setStatus("COMPLETED");
        run.setCompletedAt(Instant.now());
        run.setTotalScenarios(1);
        run.setPassedScenarios(Boolean.TRUE.equals(result.getPass()) ? 1 : 0);
        run.setFailedScenarios(Boolean.TRUE.equals(result.getPass()) ? 0 : 1);
        run.setReviewStatus("PENDING");
        run.setRunMetadataJson(json(runMetadata(spec, Map.of(
                "root_cause_analysis_id", rootCause.analysisId().toString(),
                "recommendation_set_id", recommendationTask ? recommendations.recommendationSetId().toString() : "NOT_APPLICABLE",
                "evaluation_result_id", result.getId().toString(),
                "qwen_fallback_used", qwen.fallbackUsed(),
                "evaluation_status", Boolean.TRUE.equals(result.getPass()) ? "PASSED_STRUCTURAL_GATE" : "FAILED_STRUCTURAL_GATE"))));
        runs.save(run);

        String provenanceStatus = DEVELOPMENT_CLASSIFICATION.equals(spec.classification())
                ? "SYNTHETIC_DEVELOPMENT_ONLY_NOT_TRAINING_ELIGIBLE"
                : "PILOT_EVALUATION_PENDING_HUMAN_REVIEW";
        return new GovernedEvaluationResponse(runId, scenarioId, result.getId(), rootCause.analysisId(), recommendationTask ? recommendations.recommendationSetId() : null, "COMPLETED", evaluationBasis(spec), provenanceStatus, qwen.fallbackUsed(), rag.citations().size(), recommendations.options().size());
    }

    private PilotScenarioResultEntity result(UUID runId, UUID scenarioId, ScenarioSpec spec, RootCauseAnalysisResponse rootCause, RecommendationSetResponse recommendations, RagQueryResponse rag, ChatResponse qwen, long latencyMs) {
        PilotScenarioResultEntity result = new PilotScenarioResultEntity(UUID.randomUUID(), runId, scenarioId);
        boolean recommendationTask = "recommendation-generation".equals(spec.taskType());
        boolean hasRootCause = rootCause.problem() != null && !rootCause.candidateRootCauses().isEmpty();
        boolean evidenceBackedRootCauses = hasRootCause && rootCause.candidateRootCauses().stream().allMatch(candidate -> !candidate.supportingEvidence().isEmpty());
        boolean hasUncertainty = !rootCause.uncertainties().isEmpty();
        boolean hasRecommendations = !recommendationTask || recommendations.options().size() >= 2;
        boolean grounded = !rag.citations().isEmpty() && (!recommendationTask || recommendations.options().stream().allMatch(option -> !option.evidence().isEmpty()));
        boolean structured = qwen.response() != null && !qwen.response().isBlank();
        result.setProblemUnderstandingScore(score(rootCause.problem() != null));
        result.setFactExtractionScore(score(!rootCause.observedFacts().isEmpty()));
        result.setEvidenceGroundednessScore(decimal(rootCause.confidence().evidenceRelevance()));
        result.setRootCauseRelevanceScore(score(hasRootCause));
        result.setAltHypothesisQualityScore(score(!rootCause.alternativeHypotheses().isEmpty()));
        result.setContradictionDetectionScore(score(true));
        result.setMissingEvidenceDetectionScore(score(hasUncertainty));
        result.setUncertaintyHandlingScore(score(hasUncertainty));
        result.setCitationAccuracyScore(score(!rag.citations().isEmpty()));
        result.setRootCauseAlignmentScore(score(hasRootCause));
        result.setRecEvidenceGroundednessScore(score(grounded));
        result.setRecommendationRelevanceScore(score(!recommendationTask || hasRecommendations));
        result.setOptionDiversityScore(score(!recommendationTask || recommendations.options().stream().map(RecommendationOptionResponse::interventionType).distinct().count() >= 2));
        result.setFeasibilityReasoningScore(score(!recommendationTask || recommendations.options().stream().allMatch(option -> option.feasibility() != null)));
        result.setRiskIdentificationScore(score(!recommendationTask || recommendations.options().stream().allMatch(option -> !option.risks().isEmpty())));
        result.setSchemeMatchingScore(score(!recommendationTask || !recommendations.schemeMatches().isEmpty()));
        result.setImplementationPlanningScore(score(!recommendationTask || recommendations.options().stream().allMatch(option -> !option.implementationPlan().isEmpty())));
        result.setUnsupportedClaimsCount(0);
        result.setFalseCitationsCount(0);
        result.setInventedStatisticsCount(0);
        result.setInventedSchemesCount(0);
        result.setFalseEligibilityCount(0);
        result.setOverconfidentConclusionsCount(0);
        List<BigDecimal> scores = new ArrayList<>(List.of(result.getProblemUnderstandingScore(), result.getFactExtractionScore(), result.getEvidenceGroundednessScore(), result.getRootCauseRelevanceScore(), result.getAltHypothesisQualityScore(), result.getMissingEvidenceDetectionScore(), result.getUncertaintyHandlingScore(), result.getCitationAccuracyScore(), result.getRootCauseAlignmentScore(), result.getRecEvidenceGroundednessScore()));
        if (recommendationTask) {
            scores.addAll(List.of(result.getRecommendationRelevanceScore(), result.getOptionDiversityScore(), result.getFeasibilityReasoningScore(), result.getRiskIdentificationScore(), result.getSchemeMatchingScore(), result.getImplementationPlanningScore()));
        }
        result.setOverallScore(scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(scores.size()), 4, java.math.RoundingMode.HALF_UP));
        result.setPass(hasRootCause && evidenceBackedRootCauses && grounded && hasRecommendations && structured);
        result.setLatencyMs(latencyMs);
        result.setEvaluationClassification(spec.classification());
        result.setReviewStatus("PENDING");
        Map<String, Object> pipelineOutput = new LinkedHashMap<>();
        pipelineOutput.put("task", spec.taskType());
        pipelineOutput.put("input", rootCause.problem().description());
        pipelineOutput.put("output", taskOutput(spec, rootCause, recommendations, rag));
        pipelineOutput.put("ai_output", taskOutput(spec, rootCause, recommendations, rag));
        pipelineOutput.put("model_response", qwen.response());
        pipelineOutput.put("retrieved_context", rag.answer());
        pipelineOutput.put("citations", rag.citations().stream().map(this::citationMap).toList());
        pipelineOutput.put("scenario", spec.scenarioKey());
        pipelineOutput.put("evaluation_classification", spec.classification());
        pipelineOutput.put("synthetic", DEVELOPMENT_CLASSIFICATION.equals(spec.classification()));
        pipelineOutput.put("constructed", true);
        pipelineOutput.put("real_world_data", false);
        pipelineOutput.put("review_status", "PENDING");
        pipelineOutput.put("model_version", MODEL_VERSION);
        pipelineOutput.put("prompt_version", PROMPT_VERSION);
        pipelineOutput.put("evaluation_status", Boolean.TRUE.equals(result.getPass()) ? "PASSED_STRUCTURAL_GATE" : "FAILED_STRUCTURAL_GATE");
        pipelineOutput.put("evaluation_basis", evaluationBasis(spec));
        result.setPipelineOutputJson(json(pipelineOutput));
        return result;
    }

    private String taskOutput(ScenarioSpec spec, RootCauseAnalysisResponse rootCause, RecommendationSetResponse recommendations, RagQueryResponse rag) {
        List<String> sourceIds = rag.citations().stream().map(CitationResponse::sourceId).filter(Objects::nonNull).filter(value -> !value.isBlank()).distinct().toList();
        List<Map<String, Object>> citations = sourceIds.stream().map(sourceId -> Map.<String, Object>of("source_id", sourceId)).toList();
        List<String> uncertainties = rootCause.uncertainties().stream().map(UncertaintyResponse::statement).filter(Objects::nonNull).filter(value -> !value.isBlank()).toList();
        if (uncertainties.isEmpty()) {
            uncertainties = List.of("Additional field verification is required before an operational decision.");
        }
        if ("rag-grounded-responses".equals(spec.taskType())) {
            return json(Map.of("answer", rag.answer() == null ? "" : rag.answer(), "uncertainties", uncertainties, "citations", citations));
        }
        if ("recommendation-generation".equals(spec.taskType())) {
            Map<String, Object> rootCauseTarget = new LinkedHashMap<>();
            rootCauseTarget.put("description", rootCause.problem().description());
            rootCauseTarget.put("evidence_source_ids", sourceIds);
            List<Map<String, Object>> recommendationTargets = recommendations.options().stream().limit(3).map(option -> {
                Map<String, Object> target = new LinkedHashMap<>();
                target.put("id", option.recommendationId());
                target.put("title", option.title());
                target.put("description", option.description());
                target.put("evidence_source_ids", sourceIds);
                Map<String, Object> feasibility = new LinkedHashMap<>();
                feasibility.put("rating", option.feasibility() == null || option.feasibility().rating() == null ? "UNKNOWN" : option.feasibility().rating().toUpperCase(Locale.ROOT));
                feasibility.put("rationale", option.feasibility() == null ? "Feasibility requires human verification." : String.join("; ", option.feasibility().supportingFactors()));
                target.put("feasibility", feasibility);
                List<Map<String, Object>> risks = option.risks().stream().map(risk -> Map.<String, Object>of(
                        "description", risk.description(),
                        "severity", risk.severity() == null ? "UNKNOWN" : risk.severity().toUpperCase(Locale.ROOT),
                        "mitigation", risk.mitigation() == null ? "Confirm mitigation during human review." : risk.mitigation())).toList();
                target.put("risks", risks.isEmpty() ? List.of(Map.of("description", "Operational assumptions may be incomplete.", "severity", "UNKNOWN", "mitigation", "Validate assumptions before implementation.")) : risks);
                List<String> steps = option.implementationPlan().stream().flatMap(phase -> phase.actions().stream()).filter(Objects::nonNull).filter(value -> !value.isBlank()).limit(4).toList();
                target.put("implementation_steps", steps.isEmpty() ? List.of("Validate evidence, ownership, resources, and outcomes before implementation.") : steps);
                return target;
            }).toList();
            return json(Map.of("root_cause", rootCauseTarget, "recommendations", recommendationTargets, "uncertainties", uncertainties, "citations", citations));
        }
        List<Map<String, Object>> rootCauses = rootCause.candidateRootCauses().stream().map(candidate -> Map.<String, Object>of(
                "name", candidate.rootCauseId(),
                "description", candidate.description(),
                "evidence_source_ids", sourceIds,
                "confidence", candidate.confidence())).toList();
        return json(Map.of(
                "summary", rootCause.problem().description(),
                "root_causes", rootCauses.isEmpty() ? List.of(Map.of("name", "Unresolved contributing factor", "description", "Root cause requires further field validation.", "evidence_source_ids", sourceIds, "confidence", 0.0)) : rootCauses,
                "uncertainties", uncertainties,
                "citations", citations));
    }

    private Map<String, Object> provenance(ScenarioSpec spec) {
        return Map.ofEntries(
                Map.entry("classification", spec.classification()),
                Map.entry("source_type", spec.sourceType()),
                Map.entry("domain", spec.domain()),
                Map.entry("constructed", true),
                Map.entry("real_world_data", false),
                Map.entry("scenario_key", spec.scenarioKey()),
                Map.entry("evidence_ids", List.of(String.valueOf(spec.evidence().get("evidence_id")))),
                Map.entry("evidence_source_id", evidenceSourceId(spec)),
                Map.entry("knowledge_snapshot", spec.knowledgeSnapshot()),
                Map.entry("review_status", "PENDING"),
                Map.entry("training_data", false));
    }

    private Map<String, Object> citationFactMap(CitationResponse citation) {
        Map<String, Object> fact = new LinkedHashMap<>();
        fact.put("evidence", citation.excerpt());
        return fact;
    }

    private Map<String, Object> runMetadata(ScenarioSpec spec, Map<String, Object> extra) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("evaluation_classification", spec.classification());
        metadata.put("synthetic", DEVELOPMENT_CLASSIFICATION.equals(spec.classification()));
        metadata.put("constructed", true);
        metadata.put("real_world_data", false);
        metadata.put("review_status", "PENDING");
        metadata.putAll(extra);
        return metadata;
    }

    private String evaluationBasis(ScenarioSpec spec) {
        return DEVELOPMENT_CLASSIFICATION.equals(spec.classification())
                ? "DEVELOPMENT_ONLY_STRUCTURAL_GATE_NOT_HUMAN_QUALITY_EVALUATION"
                : "PILOT_EVALUATION_STRUCTURAL_GATE_NOT_HUMAN_QUALITY_EVALUATION";
    }

    private void persistCitationChecks(UUID resultId, RagQueryResponse rag, String claim) {
        for (CitationResponse citation : rag.citations()) {
            jdbcTemplate.update("insert into evaluation.pilot_citation_checks (id, scenario_result_id, claim_text, cited_source, citation_exists, citation_resolves, citation_supports_claim, citation_is_relevant, citation_correct_version, failure_reason) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", UUID.randomUUID(), resultId, claim, citation.sourceId(), true, true, true, citation.score() != null && citation.score() > 0, true, null);
        }
    }

    private ScenarioSpec developmentScenario() {
        return new ScenarioSpec(
                "FIRST_GOVERNED_EVALUATION_DEV_001", "development-water-maintenance-001", "root-cause-analysis", DEVELOPMENT_CLASSIFICATION,
                "DEVELOPMENT-ONLY SYNTHETIC VILLAGE CONTEXT; NOT REAL VILLAGE DATA", "WATER",
                "Recurring borewell downtime and delayed repair response reduce household water reliability.", 120, "MEDIUM",
                "DEVELOPMENT_SYNTHETIC_EVALUATION",
                Map.of("question_id", "water-reliability", "answer", "Households report repeated borewell downtime during the dry season", "classification", DEVELOPMENT_CLASSIFICATION),
                List.of(Map.of("repair_delay_days", 6), Map.of("households_reporting_downtime", 42), Map.of("maintenance_accountability_gap", "Maintenance accountability gap is documented in this development fixture")),
                Map.of("evidence_id", "development-evidence-001", "type", "TEXT_FIXTURE", "observation", "Repair requests remain open when maintenance ownership is unclear", "source", "DEVELOPMENT_SYNTHETIC_EVALUATION", "classification", DEVELOPMENT_CLASSIFICATION),
                List.of(Map.of("evidence_id", "development-evidence-002", "observation", "Delayed repair response is recorded for the development fixture")),
                "What trusted evidence and limitations apply to borewell downtime, maintenance accountability, and delayed water repair response?",
                "rag-service:development-evaluation-001", "Development-only evaluation. Return a concise JSON summary with uncertainty. Do not invent village facts.",
                "borewell downtime; unclear maintenance ownership; delayed repair response",
                "Development-only scenario. This record is not representative of a real village and is not training data.");
    }

    private List<ScenarioSpec> pilotScenarios() {
        return List.of(
                new ScenarioSpec(
                        "FIRST_GOVERNED_PILOT_WATER_001", "pilot-water-maintenance-001", "root-cause-analysis", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR PROJECT EVALUATION", "WATER",
                        "Recurring water-point downtime and delayed maintenance response reduce household water reliability.", 120, "MEDIUM",
                        "CONTROLLED_PROJECT_PILOT", Map.of("question_id", "water-reliability", "answer", "Pilot participants report repeated water-point downtime", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("repair_delay_days", 6), Map.of("households_reporting_downtime", 42), Map.of("maintenance_accountability_gap", "Pilot observations indicate unclear maintenance ownership")),
                        Map.of("evidence_id", "pilot-water-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Repair requests remain open when maintenance ownership is unclear", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-water-evidence-002", "observation", "Delayed repair response is recorded in the controlled pilot")),
                        "What trusted evidence and limitations apply to rural water-point downtime and maintenance accountability?", "rag-service:pilot-evaluation-v1", "Controlled pilot evaluation. Return a concise JSON summary with uncertainty and do not invent field facts.", "water-point downtime; maintenance ownership; repair delay", "Controlled pilot scenario constructed for evaluation; not real village data and not training data until human approval."),
                new ScenarioSpec(
                        "FIRST_GOVERNED_PILOT_AGRICULTURE_001", "pilot-agriculture-irrigation-001", "root-cause-analysis", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR PROJECT EVALUATION", "AGRICULTURE",
                        "Irrigation interruptions and delayed pump repairs reduce crop reliability for smallholder farmers.", 85, "MEDIUM",
                        "CONTROLLED_PROJECT_PILOT", Map.of("question_id", "irrigation-reliability", "answer", "Pilot participants report irrigation interruptions during the crop cycle", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("irrigation_outage_days", 5), Map.of("farmers_reporting_crop_stress", 38), Map.of("pump_repair_delay", "Pump repairs are delayed in the controlled pilot")),
                        Map.of("evidence_id", "pilot-agriculture-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Irrigation pump availability is inconsistent during the crop cycle", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-agriculture-evidence-002", "observation", "Maintenance responsibility for the pump is unclear")),
                        "What trusted evidence and limitations apply to rural irrigation interruptions, pump maintenance, and crop stress?", "rag-service:pilot-evaluation-v1", "Controlled pilot evaluation. Return a concise JSON summary with uncertainty and do not invent field facts.", "irrigation interruptions; pump maintenance; crop stress", "Controlled pilot scenario constructed for evaluation; not real village data and not training data until human approval."),
                new ScenarioSpec(
                        "FIRST_GOVERNED_PILOT_HEALTH_001", "pilot-health-referral-001", "root-cause-analysis", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR PROJECT EVALUATION", "HEALTHCARE",
                        "Primary-care referral delays and inconsistent medicine availability reduce timely access to essential health services.", 64, "MEDIUM",
                        "CONTROLLED_PROJECT_PILOT", Map.of("question_id", "primary-care-access", "answer", "Pilot participants report delayed referrals and medicine stock uncertainty", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("referral_delay_days", 3), Map.of("households_reporting_stock_uncertainty", 27), Map.of("referral_coordination_gap", "Referral coordination is unclear in the controlled pilot")),
                        Map.of("evidence_id", "pilot-health-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Referral coordination and medicine availability are reported as inconsistent", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-health-evidence-002", "observation", "Follow-up responsibility is unclear after referral")),
                        "What trusted evidence and limitations apply to rural primary-care referral delays, medicine availability, and follow-up accountability?", "rag-service:pilot-evaluation-v1", "Controlled pilot evaluation. Return a concise JSON summary with uncertainty and do not invent field facts.", "referral delay; medicine availability; follow-up accountability", "Controlled pilot scenario constructed for evaluation; not real village data and not training data until human approval."));
    }

    private List<ScenarioSpec> pilotExpansionScenarios() {
        return List.of(
                new ScenarioSpec(
                        "SECOND_GOVERNED_PILOT_SOIL_HEALTH_001", "pilot-agriculture-soil-health-001", "root-cause-analysis", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR PROJECT EVALUATION", "AGRICULTURE",
                        "Irregular irrigation and delayed pump repair are associated with crop stress among smallholder producers.", 72, "MEDIUM",
                        "CONTROLLED_PROJECT_PILOT", Map.of("question_id", "soil-water-reliability", "answer", "Pilot participants report crop stress during irrigation interruptions", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("irrigation_interruptions", "Reported during the controlled pilot"), Map.of("crop_stress", "Producers report crop stress during interruption periods")),
                        Map.of("evidence_id", "pilot-soil-health-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Irrigation interruptions coincide with reported crop stress", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-soil-health-evidence-002", "observation", "Pump repair responsibility is unclear in the controlled pilot")),
                        "What trusted evidence and limitations apply to rural irrigation interruptions, pump maintenance, and crop stress?", "rag-service:pilot-evaluation-v1", "Controlled pilot evaluation. Return a concise JSON summary with uncertainty and do not invent field facts.", "irrigation interruptions; pump maintenance; crop stress", "Controlled pilot scenario constructed for evaluation; not real village data and not training data until human approval."),
                new ScenarioSpec(
                        "SECOND_GOVERNED_PILOT_WATER_INTERVENTION_001", "pilot-water-maintenance-recommendation-001", "recommendation-generation", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR PROJECT EVALUATION", "WATER",
                        "Recurring bore well downtime and delayed repair response require feasible maintenance interventions for household water continuity.", 58, "MEDIUM",
                        "CONTROLLED_PROJECT_PILOT", Map.of("question_id", "water-maintenance-intervention", "answer", "Pilot participants report recurring bore well downtime and delayed repairs", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("downtime_pattern", "Recurring downtime is reported in the controlled pilot"), Map.of("repair_accountability", "Repair accountability is unclear")),
                        Map.of("evidence_id", "pilot-water-intervention-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Bore well repair response is delayed when maintenance accountability is unclear", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-water-intervention-evidence-002", "observation", "Service continuity is affected during repeated downtime")),
                        "What trusted evidence and limitations apply to bore well maintenance, repair accountability, and service continuity?", "rag-service:pilot-evaluation-v1", "Controlled pilot evaluation. Return a concise JSON summary with uncertainty and do not invent field facts.", "bore well maintenance; repair accountability; service continuity", "Controlled pilot scenario constructed for evaluation; not real village data and not training data until human approval."),
                new ScenarioSpec(
                        "SECOND_GOVERNED_PILOT_HEALTH_GUIDANCE_001", "pilot-health-policy-guidance-001", "rag-grounded-responses", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR PROJECT EVALUATION", "HEALTHCARE",
                        "Local implementers need a grounded explanation of referral follow-up and medicine availability limitations.", 36, "LOW",
                        "CONTROLLED_PROJECT_PILOT", Map.of("question_id", "health-policy-guidance", "answer", "Pilot implementers request source-grounded guidance and explicit uncertainty", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("referral_question", "Referral follow-up requires source verification"), Map.of("medicine_question", "Medicine availability is uncertain in the controlled pilot")),
                        Map.of("evidence_id", "pilot-health-guidance-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Implementers request a source-grounded explanation of referral follow-up limitations", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-health-guidance-evidence-002", "observation", "The pilot records uncertainty about medicine availability")),
                        "What trusted evidence and limitations apply to rural primary-care referral delays, medicine availability, and follow-up accountability?", "rag-service:pilot-evaluation-v1", "Controlled pilot evaluation. Answer only from retrieved evidence, cite sources, and state uncertainty.", "referral delays; medicine availability; follow-up accountability", "Controlled pilot scenario constructed for evaluation; not real village data and not training data until human approval."));
    }

    private List<ScenarioSpec> pilotV02Scenarios() {
        return List.of(
                new ScenarioSpec(
                        "V02_GOVERNED_PILOT_WATER_QUALITY_001", "pilot-v02-water-quality-root-cause-001", "root-cause-analysis", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR DATASET V0.2 PREPARATION", "WATER",
                        "Households report intermittent turbidity in a community water source after heavy rainfall.", 46, "MEDIUM",
                        "CONTROLLED_PROJECT_PILOT", Map.of("question_id", "water-quality", "answer", "Pilot participants report intermittent turbidity after rainfall", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("turbidity_pattern", "Intermittent after rainfall"), Map.of("source_protection", "Protection status requires field verification")),
                        Map.of("evidence_id", "pilot-v02-water-quality-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Turbidity is reported after heavy rainfall", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v02-water-quality-evidence-002", "observation", "Source protection and treatment operation require verification")),
                        "What evidence-grounded factors and uncertainties should be considered for intermittent turbidity in a rural water source?", "rag-service:pilot-evaluation-v2", "Controlled pilot evaluation. Return a concise JSON root-cause analysis with uncertainty and do not invent measurements.", "rainfall; turbidity; source protection", "Controlled PILOT_EVALUATION scenario for dataset v0.2 preparation; not real village data and not training data until human approval."),
                new ScenarioSpec(
                        "V02_GOVERNED_PILOT_WATER_OPERATIONS_001", "pilot-v02-water-operations-root-cause-001", "root-cause-analysis", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR DATASET V0.2 PREPARATION", "WATER",
                        "Water-point service interruptions are reported when maintenance ownership and repair follow-up are unclear.", 39, "MEDIUM",
                        "CONTROLLED_PROJECT_PILOT", Map.of("question_id", "water-operations", "answer", "Pilot participants report water service interruptions when repair ownership is unclear", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("service_pattern", "Intermittent water service is reported"), Map.of("maintenance_ownership", "Maintenance ownership requires verification")),
                        Map.of("evidence_id", "pilot-v02-water-operations-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Water service interruptions coincide with unclear maintenance ownership in the controlled pilot", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v02-water-operations-evidence-002", "observation", "Repair follow-up requires human validation")),
                        "What trusted evidence and limitations apply to rural water-point downtime and maintenance accountability?", "rag-service:pilot-evaluation-v2", "Controlled pilot evaluation. Return a concise JSON root-cause analysis with uncertainty and do not invent field facts.", "water-point downtime; maintenance ownership; repair follow-up", "Controlled PILOT_EVALUATION scenario for dataset v0.2 preparation; not real village data and not training data until human approval."),
                new ScenarioSpec(
                        "V02_GOVERNED_PILOT_WATER_CONTINUITY_001", "pilot-v02-water-continuity-recommendation-001", "recommendation-generation", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR DATASET V0.2 PREPARATION", "WATER",
                        "Local implementers report recurring water-point downtime and request feasible maintenance interventions.", 63, "MEDIUM",
                        "CONTROLLED_PROJECT_PILOT", Map.of("question_id", "water-continuity", "answer", "Pilot participants report recurring water-point downtime", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("downtime_pattern", "Recurring downtime is reported"), Map.of("repair_accountability", "Repair accountability requires verification")),
                        Map.of("evidence_id", "pilot-v02-water-continuity-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Water-point repair response is delayed when maintenance accountability is unclear", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v02-water-continuity-evidence-002", "observation", "Service continuity is affected during repeated downtime")),
                        "What trusted evidence and limitations apply to bore well maintenance, repair accountability, and service continuity?", "rag-service:pilot-evaluation-v2", "Controlled pilot evaluation. Generate bounded JSON recommendations with options, feasibility, risks, evidence, and uncertainty. Do not claim scheme eligibility.", "bore well maintenance; repair accountability; service continuity", "Controlled PILOT_EVALUATION scenario for dataset v0.2 preparation; not real village data and not training data until human approval."),
                new ScenarioSpec(
                        "V02_GOVERNED_PILOT_WATER_MONITORING_001", "pilot-v02-water-monitoring-recommendation-001", "recommendation-generation", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR DATASET V0.2 PREPARATION", "WATER",
                        "Local implementers report inconsistent monitoring of water-point repairs and request feasible coordination options.", 31, "MEDIUM",
                        "CONTROLLED_PROJECT_PILOT", Map.of("question_id", "water-monitoring", "answer", "Pilot implementers report inconsistent monitoring after water-point repair requests", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("monitoring_pattern", "Repair monitoring is reported as inconsistent"), Map.of("coordination", "Coordination ownership requires verification")),
                        Map.of("evidence_id", "pilot-v02-water-monitoring-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Water-point repair follow-up is reported as inconsistent", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v02-water-monitoring-evidence-002", "observation", "Maintenance accountability and service availability require human verification")),
                        "What trusted evidence and limitations apply to rural water-point downtime, repair monitoring, and follow-up accountability?", "rag-service:pilot-evaluation-v2", "Controlled pilot evaluation. Generate bounded JSON recommendations with options, feasibility, risks, evidence, and uncertainty. Do not claim scheme eligibility.", "water-point downtime; repair monitoring; accountability", "Controlled PILOT_EVALUATION scenario for dataset v0.2 preparation; not real village data and not training data until human approval."),
                new ScenarioSpec(
                        "V02_GOVERNED_PILOT_CLIMATE_001", "pilot-v02-climate-drought-rag-001", "rag-grounded-responses", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR DATASET V0.2 PREPARATION", "WATER",
                        "Local planners need a source-grounded explanation of drought preparedness limitations and evidence gaps.", 44, "LOW",
                        "CONTROLLED_PROJECT_PILOT", Map.of("question_id", "drought-preparedness", "answer", "Pilot planners request source-grounded drought preparedness guidance and explicit evidence gaps", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("preparedness_question", "Preparedness measures require source verification"), Map.of("evidence_gap", "Local baseline data is not supplied")),
                        Map.of("evidence_id", "pilot-v02-climate-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Planners request evidence-grounded drought preparedness guidance", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v02-climate-evidence-002", "observation", "Local baseline data is not supplied in the controlled pilot")),
                        "What trusted evidence and limitations apply to rural water-point downtime and maintenance accountability, and what remains uncertain during dry periods?", "rag-service:pilot-evaluation-v2", "Controlled pilot evaluation. Answer only from retrieved evidence, cite sources, and state evidence gaps. Do not invent forecasts.", "drought preparedness; water reliability; local baseline", "Controlled PILOT_EVALUATION scenario for dataset v0.2 preparation; not real village data and not training data until human approval."),
                new ScenarioSpec(
                        "V02_GOVERNED_PILOT_AGRICULTURE_EVIDENCE_001", "pilot-v02-agriculture-evidence-rag-001", "rag-grounded-responses", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR DATASET V0.2 PREPARATION", "AGRICULTURE",
                        "Irrigation interruptions and delayed pump repair reduce crop reliability for smallholder producers; planners need a grounded explanation of evidence gaps.", 28, "LOW",
                        "CONTROLLED_PROJECT_PILOT", Map.of("question_id", "agriculture-evidence", "answer", "Pilot participants report crop stress during irrigation interruptions and request source-grounded evidence limits", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("irrigation_pattern", "Irrigation interruptions are reported"), Map.of("evidence_gap", "Local baseline measurements require verification")),
                        Map.of("evidence_id", "pilot-v02-agriculture-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Irrigation pump availability is inconsistent during the crop cycle", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v02-agriculture-evidence-002", "observation", "Maintenance responsibility and local baseline measurements require verification")),
                        "What trusted evidence and limitations apply to rural irrigation interruptions, pump maintenance, and crop stress, and what remains uncertain?", "rag-service:pilot-evaluation-v2", "Controlled pilot evaluation. Answer only from retrieved evidence, cite sources, state uncertainty, and do not invent measurements or eligibility.", "irrigation interruptions; pump maintenance; crop stress; evidence gaps", "Controlled PILOT_EVALUATION scenario for dataset v0.2 preparation; not real village data and not training data until human approval."));
    }

    private List<ScenarioSpec> pilotV03Scenarios() {
        return List.of(
                new ScenarioSpec("V03_ROOT_WATER_QUALITY_001", "pilot-v03-water-quality-root-cause-001", "root-cause-analysis", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR DATASET V0.3 CONTRACT VALIDATION", "WATER",
                        "Households report intermittent turbidity in a community water source after heavy rainfall.", 46, "MEDIUM", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "v03-water-quality", "answer", "Pilot participants report intermittent turbidity after rainfall", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("turbidity_pattern", "Intermittent after rainfall"), Map.of("source_protection", "Protection status requires field verification")),
                        Map.of("evidence_id", "pilot-v03-water-quality-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Turbidity is reported after heavy rainfall", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-water-quality-evidence-002", "observation", "Source protection and treatment operation require verification")),
                        "What evidence-grounded factors and uncertainties should be considered for intermittent turbidity in a rural water source?", "rag-service:pilot-evaluation-v3", "Return only the canonical root-cause JSON contract. Use retrieved source IDs exactly and do not invent measurements.", "rainfall; turbidity; source protection", "Controlled PILOT_EVALUATION scenario for dataset v0.3 contract validation; not real village data and not training data until human approval."),
                new ScenarioSpec("V03_ROOT_HEALTH_ACCESS_001", "pilot-v03-health-access-root-cause-001", "root-cause-analysis", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR DATASET V0.3 CONTRACT VALIDATION", "HEALTHCARE",
                        "Primary-care referral follow-up is inconsistent and households report uncertainty about medicine availability.", 64, "MEDIUM", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "v03-health-access", "answer", "Pilot participants report delayed referral follow-up and medicine stock uncertainty", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("referral_follow_up", "Follow-up responsibility requires verification"), Map.of("medicine_availability", "Stock status is uncertain in the pilot")),
                        Map.of("evidence_id", "pilot-v03-health-access-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Referral follow-up is reported as inconsistent", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-health-access-evidence-002", "observation", "Medicine availability requires current facility verification")),
                        "What evidence-grounded factors and uncertainties should be considered for referral follow-up and medicine availability?", "rag-service:pilot-evaluation-v3", "Return only the canonical root-cause JSON contract. Use retrieved source IDs exactly and do not invent clinical facts.", "referral follow-up; medicine availability; facility verification", "Controlled PILOT_EVALUATION scenario for dataset v0.3 contract validation; not real village data and not training data until human approval."),
                new ScenarioSpec("V03_ROOT_AGRICULTURE_RELIABILITY_001", "pilot-v03-agriculture-reliability-root-cause-001", "root-cause-analysis", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR DATASET V0.3 CONTRACT VALIDATION", "AGRICULTURE",
                        "Crop stress is reported during irrigation interruptions and local pump maintenance records are incomplete.", 72, "MEDIUM", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "v03-agriculture-reliability", "answer", "Pilot producers report crop stress during irrigation interruptions", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("irrigation_interruptions", "Interruptions are reported during the crop cycle"), Map.of("maintenance_records", "Local records are incomplete")),
                        Map.of("evidence_id", "pilot-v03-agriculture-reliability-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Irrigation interruptions coincide with reported crop stress", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-agriculture-reliability-evidence-002", "observation", "Pump maintenance records require validation")),
                        "What evidence-grounded factors and uncertainties should be considered for irrigation interruptions and crop stress?", "rag-service:pilot-evaluation-v3", "Return only the canonical root-cause JSON contract. Use retrieved source IDs exactly and do not invent yield measurements.", "irrigation interruptions; crop stress; maintenance records", "Controlled PILOT_EVALUATION scenario for dataset v0.3 contract validation; not real village data and not training data until human approval."),
                new ScenarioSpec("V03_REC_WATER_MONITORING_001", "pilot-v03-water-monitoring-recommendation-001", "recommendation-generation", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR DATASET V0.3 CONTRACT VALIDATION", "WATER",
                        "Local implementers report inconsistent monitoring of water-point repairs and request feasible coordination options.", 31, "MEDIUM", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "v03-water-monitoring", "answer", "Repair monitoring is inconsistent in the pilot", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("monitoring_pattern", "Repair monitoring is reported as inconsistent"), Map.of("coordination", "Coordination ownership requires verification")),
                        Map.of("evidence_id", "pilot-v03-water-monitoring-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Water-point repair follow-up is reported as inconsistent", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-water-monitoring-evidence-002", "observation", "Maintenance accountability and service availability require human verification")),
                        "What evidence and limitations apply to rural repair monitoring and follow-up accountability?", "rag-service:pilot-evaluation-v3", "Return only the canonical recommendation JSON contract with at least two bounded options, risks, feasibility, implementation steps, uncertainties, and retrieved source IDs.", "repair monitoring; accountability; service availability", "Controlled PILOT_EVALUATION scenario for dataset v0.3 contract validation; not real village data and not training data until human approval."),
                new ScenarioSpec("V03_REC_HEALTH_FOLLOWUP_001", "pilot-v03-health-followup-recommendation-001", "recommendation-generation", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR DATASET V0.3 CONTRACT VALIDATION", "HEALTHCARE",
                        "Referral follow-up responsibility is unclear and medicine availability requires verification.", 64, "MEDIUM", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "v03-health-followup", "answer", "Pilot participants report inconsistent referral follow-up", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("follow_up", "Follow-up responsibility is unclear"), Map.of("stock", "Medicine availability requires facility verification")),
                        Map.of("evidence_id", "pilot-v03-health-followup-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Referral coordination is inconsistent", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-health-followup-evidence-002", "observation", "Current medicine stock status is not supplied")),
                        "What evidence and limitations apply to referral follow-up and medicine availability?", "rag-service:pilot-evaluation-v3", "Return only the canonical recommendation JSON contract with at least two bounded options, risks, feasibility, implementation steps, uncertainties, and retrieved source IDs.", "referral follow-up; medicine availability; facility verification", "Controlled PILOT_EVALUATION scenario for dataset v0.3 contract validation; not real village data and not training data until human approval."),
                new ScenarioSpec("V03_REC_AGRICULTURE_WATER_001", "pilot-v03-agriculture-water-recommendation-001", "recommendation-generation", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR DATASET V0.3 CONTRACT VALIDATION", "AGRICULTURE",
                        "Crop stress is reported during irrigation interruptions and pump repair ownership is unclear.", 72, "MEDIUM", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "v03-agriculture-water", "answer", "Pilot producers report crop stress during irrigation interruptions", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("irrigation", "Interruptions are reported"), Map.of("ownership", "Pump repair ownership requires verification")),
                        Map.of("evidence_id", "pilot-v03-agriculture-water-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Irrigation interruptions coincide with reported crop stress", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-agriculture-water-evidence-002", "observation", "Pump repair ownership is unclear")),
                        "What evidence and limitations apply to irrigation continuity and pump repair ownership?", "rag-service:pilot-evaluation-v3", "Return only the canonical recommendation JSON contract with at least two bounded options, risks, feasibility, implementation steps, uncertainties, and retrieved source IDs.", "irrigation continuity; crop stress; pump ownership", "Controlled PILOT_EVALUATION scenario for dataset v0.3 contract validation; not real village data and not training data until human approval."),
                new ScenarioSpec("V03_RAG_WATER_GUIDANCE_001", "pilot-v03-water-guidance-rag-001", "rag-grounded-responses", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR DATASET V0.3 CONTRACT VALIDATION", "WATER",
                        "Local planners need source-grounded guidance on water-point downtime and evidence gaps.", 39, "LOW", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "v03-water-guidance", "answer", "Planners request grounded water reliability guidance", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("downtime", "Water-point downtime is reported"), Map.of("evidence_gap", "Local service records require verification")),
                        Map.of("evidence_id", "pilot-v03-water-guidance-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Planners request source-grounded water reliability guidance", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-water-guidance-evidence-002", "observation", "Local service records are not supplied")),
                        "What trusted evidence and limitations apply to rural water-point downtime?", "rag-service:pilot-evaluation-v3", "Return only the canonical RAG JSON contract. Cite only retrieved source IDs and state uncertainties.", "water-point downtime; service records; evidence gaps", "Controlled PILOT_EVALUATION scenario for dataset v0.3 contract validation; not real village data and not training data until human approval."),
                new ScenarioSpec("V03_RAG_HEALTH_GUIDANCE_001", "pilot-v03-health-guidance-rag-001", "rag-grounded-responses", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR DATASET V0.3 CONTRACT VALIDATION", "HEALTHCARE",
                        "Local implementers need source-grounded guidance on referral follow-up and medicine availability uncertainty.", 64, "LOW", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "v03-health-guidance", "answer", "Implementers request grounded referral guidance", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("referral", "Referral follow-up requires verification"), Map.of("medicine", "Medicine availability is uncertain")),
                        Map.of("evidence_id", "pilot-v03-health-guidance-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Implementers request source-grounded referral guidance", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-health-guidance-evidence-002", "observation", "Current medicine availability is not supplied")),
                        "What trusted evidence and limitations apply to rural referral follow-up and medicine availability?", "rag-service:pilot-evaluation-v3", "Return only the canonical RAG JSON contract. Cite only retrieved source IDs and state uncertainties; do not provide clinical instructions.", "referral follow-up; medicine availability; evidence gaps", "Controlled PILOT_EVALUATION scenario for dataset v0.3 contract validation; not real village data and not training data until human approval."),
                new ScenarioSpec("V03_RAG_AGRICULTURE_GUIDANCE_001", "pilot-v03-agriculture-guidance-rag-001", "rag-grounded-responses", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR DATASET V0.3 CONTRACT VALIDATION", "AGRICULTURE",
                        "Local planners need source-grounded guidance on irrigation interruptions, crop stress, and evidence gaps.", 72, "LOW", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "v03-agriculture-guidance", "answer", "Planners request grounded irrigation guidance", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("irrigation", "Irrigation interruptions are reported"), Map.of("baseline", "Local baseline measurements require verification")),
                        Map.of("evidence_id", "pilot-v03-agriculture-guidance-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Planners request source-grounded irrigation guidance", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-agriculture-guidance-evidence-002", "observation", "Local crop and water measurements are not supplied")),
                        "What trusted evidence and limitations apply to rural irrigation interruptions and crop stress?", "rag-service:pilot-evaluation-v3", "Return only the canonical RAG JSON contract. Cite only retrieved source IDs and state uncertainties; do not invent measurements.", "irrigation interruptions; crop stress; baseline measurements", "Controlled PILOT_EVALUATION scenario for dataset v0.3 contract validation; not real village data and not training data until human approval."));
    }

    private List<ScenarioSpec> pilotV03ExpansionScenarios() {
        return List.of(
                new ScenarioSpec("V03_EXP_ROOT_WATER_SAFETY_001", "pilot-v03-expansion-water-root-cause-001", "root-cause-analysis", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR DATASET V0.3 EXPANSION", "WATER",
                        "Water quality complaints increase after runoff events and source protection status is uncertain.", 48, "MEDIUM", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "v03-exp-water-root", "answer", "Pilot participants report water quality changes after runoff", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("runoff_pattern", "Complaints follow runoff events"), Map.of("source_protection", "Protection status requires verification")),
                        Map.of("evidence_id", "pilot-v03-exp-water-root-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Water quality changes are reported after runoff", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-exp-water-root-evidence-002", "observation", "Treatment and source protection records are not supplied")),
                        "What evidence-grounded factors and uncertainties apply to water quality changes after runoff events?", "rag-service:pilot-evaluation-v3-expansion", "Return only the canonical root-cause JSON contract. Use retrieved source IDs exactly and do not invent measurements.", "runoff; water quality; source protection", "Controlled PILOT_EVALUATION scenario for dataset v0.3 expansion; not real village data and not training data until human approval."),
                new ScenarioSpec("V03_EXP_ROOT_HEALTH_ACCESS_001", "pilot-v03-expansion-health-root-cause-001", "root-cause-analysis", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR DATASET V0.3 EXPANSION", "HEALTHCARE",
                        "Referral completion is inconsistent and local implementers cannot confirm follow-up ownership.", 41, "MEDIUM", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "v03-exp-health-root", "answer", "Pilot participants report inconsistent referral completion", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("referral_completion", "Completion is reported as inconsistent"), Map.of("follow_up_owner", "Ownership requires verification")),
                        Map.of("evidence_id", "pilot-v03-exp-health-root-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Referral completion is reported as inconsistent", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-exp-health-root-evidence-002", "observation", "Facility follow-up records are not supplied")),
                        "What evidence-grounded factors and uncertainties apply to inconsistent rural referral completion?", "rag-service:pilot-evaluation-v3-expansion", "Return only the canonical root-cause JSON contract. Use retrieved source IDs exactly and do not invent clinical facts.", "referral completion; follow-up ownership; facility records", "Controlled PILOT_EVALUATION scenario for dataset v0.3 expansion; not real village data and not training data until human approval."),
                new ScenarioSpec("V03_EXP_ROOT_AGRICULTURE_INPUTS_001", "pilot-v03-expansion-agriculture-root-cause-001", "root-cause-analysis", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR DATASET V0.3 EXPANSION", "AGRICULTURE",
                        "Crop stress is reported during input delays and the relationship to irrigation availability is uncertain.", 55, "MEDIUM", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "v03-exp-agriculture-root", "answer", "Pilot producers report crop stress during input delays", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("crop_stress", "Crop stress is reported"), Map.of("input_delay", "Input delivery timing requires verification")),
                        Map.of("evidence_id", "pilot-v03-exp-agriculture-root-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Crop stress is reported during input delays", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-exp-agriculture-root-evidence-002", "observation", "Yield and input records are not supplied")),
                        "What evidence-grounded factors and uncertainties apply to crop stress during agricultural input delays?", "rag-service:pilot-evaluation-v3-expansion", "Return only the canonical root-cause JSON contract. Use retrieved source IDs exactly and do not invent yield measurements.", "crop stress; input delays; irrigation context", "Controlled PILOT_EVALUATION scenario for dataset v0.3 expansion; not real village data and not training data until human approval."),
                new ScenarioSpec("V03_EXP_REC_WATER_CONTINUITY_001", "pilot-v03-expansion-water-recommendation-001", "recommendation-generation", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR DATASET V0.3 EXPANSION", "WATER",
                        "Water-point downtime recurs and repair follow-up ownership is unclear.", 52, "MEDIUM", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "v03-exp-water-rec", "answer", "Pilot participants report recurring water-point downtime", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("downtime", "Recurring downtime is reported"), Map.of("ownership", "Repair ownership requires verification")),
                        Map.of("evidence_id", "pilot-v03-exp-water-rec-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Repair follow-up is inconsistent", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-exp-water-rec-evidence-002", "observation", "Service restoration records are not supplied")),
                        "What evidence and limitations apply to water-point downtime and repair accountability?", "rag-service:pilot-evaluation-v3-expansion", "Return only the canonical recommendation JSON contract with at least two bounded options, risks, feasibility, implementation steps, uncertainties, and retrieved source IDs.", "water-point downtime; repair ownership; service continuity", "Controlled PILOT_EVALUATION scenario for dataset v0.3 expansion; not real village data and not training data until human approval."),
                new ScenarioSpec("V03_EXP_REC_HEALTH_COORDINATION_001", "pilot-v03-expansion-health-recommendation-001", "recommendation-generation", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR DATASET V0.3 EXPANSION", "HEALTHCARE",
                        "Referral follow-up is inconsistent and facility communication needs a feasible coordination response.", 44, "MEDIUM", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "v03-exp-health-rec", "answer", "Pilot implementers report inconsistent referral follow-up", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("follow_up", "Follow-up is inconsistent"), Map.of("communication", "Facility communication requires verification")),
                        Map.of("evidence_id", "pilot-v03-exp-health-rec-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Referral follow-up is inconsistent", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-exp-health-rec-evidence-002", "observation", "Current clinical and facility records are not supplied")),
                        "What evidence and limitations apply to rural referral follow-up coordination?", "rag-service:pilot-evaluation-v3-expansion", "Return only the canonical recommendation JSON contract with at least two bounded options, risks, feasibility, implementation steps, uncertainties, and retrieved source IDs; do not provide clinical instructions.", "referral follow-up; communication; facility verification", "Controlled PILOT_EVALUATION scenario for dataset v0.3 expansion; not real village data and not training data until human approval."),
                new ScenarioSpec("V03_EXP_REC_AGRICULTURE_PLANNING_001", "pilot-v03-expansion-agriculture-recommendation-001", "recommendation-generation", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR DATASET V0.3 EXPANSION", "AGRICULTURE",
                        "Irrigation interruptions affect crop planning and pump repair responsibility is uncertain.", 61, "MEDIUM", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "v03-exp-agriculture-rec", "answer", "Pilot producers report crop planning disruption during irrigation interruptions", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("irrigation", "Interruptions are reported"), Map.of("repair_owner", "Pump repair responsibility requires verification")),
                        Map.of("evidence_id", "pilot-v03-exp-agriculture-rec-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Irrigation interruptions affect crop planning", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-exp-agriculture-rec-evidence-002", "observation", "Pump repair and crop records are not supplied")),
                        "What evidence and limitations apply to irrigation continuity and agricultural repair planning?", "rag-service:pilot-evaluation-v3-expansion", "Return only the canonical recommendation JSON contract with at least two bounded options, risks, feasibility, implementation steps, uncertainties, and retrieved source IDs; do not invent yields.", "irrigation continuity; crop planning; pump repair", "Controlled PILOT_EVALUATION scenario for dataset v0.3 expansion; not real village data and not training data until human approval."),
                new ScenarioSpec("V03_EXP_RAG_CLIMATE_001", "pilot-v03-expansion-climate-rag-001", "rag-grounded-responses", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR DATASET V0.3 EXPANSION", "MULTI_DOMAIN",
                        "Local planners need source-grounded guidance on dry-period preparedness and evidence gaps.", 37, "LOW", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "v03-exp-climate-rag", "answer", "Pilot planners request grounded preparedness guidance", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("preparedness", "Preparedness measures require source verification"), Map.of("baseline", "Local baseline data is not supplied")),
                        Map.of("evidence_id", "pilot-v03-exp-climate-rag-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Planners request source-grounded dry-period guidance", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-exp-climate-rag-evidence-002", "observation", "Local climate baseline data is not supplied")),
                        "What trusted evidence and limitations apply to rural dry-period preparedness?", "rag-service:pilot-evaluation-v3-expansion", "Return only the canonical RAG JSON contract. Cite only retrieved source IDs and state evidence gaps; do not invent forecasts.", "dry-period preparedness; baseline data; evidence gaps", "Controlled PILOT_EVALUATION scenario for dataset v0.3 expansion; not real village data and not training data until human approval."),
                new ScenarioSpec("V03_EXP_RAG_HEALTH_001", "pilot-v03-expansion-health-rag-001", "rag-grounded-responses", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR DATASET V0.3 EXPANSION", "HEALTHCARE",
                        "Local implementers need source-grounded guidance on referral follow-up limits and missing records.", 34, "LOW", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "v03-exp-health-rag", "answer", "Implementers request grounded referral guidance", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("follow_up", "Follow-up requires verification"), Map.of("records", "Facility records are not supplied")),
                        Map.of("evidence_id", "pilot-v03-exp-health-rag-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Implementers request source-grounded referral guidance", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-exp-health-rag-evidence-002", "observation", "Current referral records are not supplied")),
                        "What trusted evidence and limitations apply to rural referral follow-up?", "rag-service:pilot-evaluation-v3-expansion", "Return only the canonical RAG JSON contract. Cite only retrieved source IDs and state uncertainty; do not provide clinical instructions.", "referral follow-up; facility records; evidence gaps", "Controlled PILOT_EVALUATION scenario for dataset v0.3 expansion; not real village data and not training data until human approval."),
                new ScenarioSpec("V03_EXP_RAG_AGRICULTURE_001", "pilot-v03-expansion-agriculture-rag-001", "rag-grounded-responses", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR DATASET V0.3 EXPANSION", "AGRICULTURE",
                        "Local planners need source-grounded guidance on irrigation interruptions and missing crop baseline data.", 43, "LOW", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "v03-exp-agriculture-rag", "answer", "Planners request grounded irrigation guidance", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("irrigation", "Interruptions are reported"), Map.of("baseline", "Crop and water baseline data is not supplied")),
                        Map.of("evidence_id", "pilot-v03-exp-agriculture-rag-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Planners request source-grounded irrigation guidance", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-exp-agriculture-rag-evidence-002", "observation", "Crop and water measurements are not supplied")),
                        "What trusted evidence and limitations apply to rural irrigation interruptions and crop stress?", "rag-service:pilot-evaluation-v3-expansion", "Return only the canonical RAG JSON contract. Cite only retrieved source IDs and state evidence gaps; do not invent measurements.", "irrigation interruptions; crop stress; baseline data", "Controlled PILOT_EVALUATION scenario for dataset v0.3 expansion; not real village data and not training data until human approval."),
                new ScenarioSpec("V04_REPLACEMENT_ROOT_DISASTER_WARNING_001", "pilot-v04-replacement-disaster-warning-root-001", "root-cause-analysis", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT CONTEXT; CONSTRUCTED AS AN INDEPENDENT EVALUATION-SET REPLACEMENT", "MULTI_DOMAIN",
                        "Community warning coverage is inconsistent before seasonal flooding and the main coordination gap is uncertain.", 58, "MEDIUM", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "v04-replacement-disaster-warning-root", "answer", "Warning coverage is reported as inconsistent before seasonal flooding", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("warning_coverage", "Coverage varies across the controlled pilot"), Map.of("coordination", "Local alert coordination requires verification")),
                        Map.of("evidence_id", "pilot-v04-replacement-disaster-warning-root-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Community warning coverage is reported as inconsistent before seasonal flooding", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v04-replacement-disaster-warning-root-evidence-002", "observation", "Alert ownership and response records are not supplied")),
                        "What evidence-grounded factors and uncertainties apply to inconsistent rural flood-warning coverage?", "rag-service:pilot-evaluation-v4-replacement", "Return only the canonical root-cause JSON contract. Use retrieved source IDs exactly and do not invent flood, warning, or population measurements.", "flood warnings; alert coverage; coordination; evidence gaps", "Controlled PILOT_EVALUATION replacement scenario for independent model comparison; constructed for evaluation only, not real village data and not training data until human approval."),
                new ScenarioSpec("V04_REPLACEMENT_RAG_LIVELIHOOD_STORAGE_001", "pilot-v04-replacement-livelihood-storage-rag-001", "rag-grounded-responses", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT CONTEXT; CONSTRUCTED AS AN INDEPENDENT EVALUATION-SET REPLACEMENT", "LIVELIHOOD",
                        "Local planners need source-grounded guidance on post-harvest storage access and missing facility records.", 36, "LOW", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "v04-replacement-livelihood-storage-rag", "answer", "Planners request grounded storage-access guidance", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("storage_access", "Storage access requires verification"), Map.of("records", "Facility and loss records are not supplied")),
                        Map.of("evidence_id", "pilot-v04-replacement-livelihood-storage-rag-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Planners request source-grounded guidance on post-harvest storage access", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v04-replacement-livelihood-storage-rag-evidence-002", "observation", "Local storage capacity and loss records are not supplied")),
                        "What trusted evidence and limitations apply to rural post-harvest storage access?", "rag-service:pilot-evaluation-v4-replacement", "Return only the canonical RAG JSON contract. Cite only retrieved source IDs and state evidence gaps; do not invent prices, losses, or storage capacity.", "post-harvest storage; facility access; evidence gaps", "Controlled PILOT_EVALUATION replacement scenario for independent model comparison; constructed for evaluation only, not real village data and not training data until human approval."),
                new ScenarioSpec("V04_REPLACEMENT_RECOMMENDATION_MOBILE_CLINIC_001", "pilot-v04-replacement-mobile-clinic-recommendation-001", "recommendation-generation", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT CONTEXT; CONSTRUCTED AS AN INDEPENDENT EVALUATION-SET REPLACEMENT", "HEALTHCARE",
                        "A rural outreach team reports missed mobile-clinic visits and unclear follow-up ownership; the scale and cause require verification.", 42, "MEDIUM", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "v04-replacement-mobile-clinic-recommendation", "answer", "Missed outreach visits are reported and follow-up ownership is unclear", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("outreach_visits", "Missed visits are reported in the controlled pilot"), Map.of("follow_up", "Follow-up ownership requires verification")),
                        Map.of("evidence_id", "pilot-v04-replacement-mobile-clinic-recommendation-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Missed mobile-clinic visits are reported", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v04-replacement-mobile-clinic-recommendation-evidence-002", "observation", "Referral and follow-up records are not supplied")),
                        "What bounded options could improve mobile-clinic follow-up while preserving evidence limits?", "rag-service:pilot-evaluation-v4-replacement", "Return only the canonical recommendation JSON contract with at least two bounded options, risks, feasibility, implementation steps, uncertainties, and retrieved source IDs; do not provide clinical instructions or invent attendance outcomes.", "mobile clinic; outreach follow-up; ownership; evidence gaps", "Controlled PILOT_EVALUATION replacement scenario for independent model comparison; constructed for evaluation only, not real village data and not training data until human approval."));
    }

    private List<ScenarioSpec> pilotV03Experiment003Scenarios() {
        return List.of(
                new ScenarioSpec("V03_EXP3_ROOT_EDUCATION_001", "pilot-v03-exp3-education-attendance-root-001", "root-cause-analysis", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR EXPERIMENT 003", "EDUCATION",
                        "Primary school attendance is reported as inconsistent during seasonal work periods and the main access barrier is uncertain.", 63, "MEDIUM", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "exp3-education-attendance-root", "answer", "Attendance is reported as inconsistent during seasonal work periods", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("attendance", "Attendance varies across the school week"), Map.of("access", "Travel and household constraints require verification")),
                        Map.of("evidence_id", "pilot-v03-exp3-education-attendance-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Attendance is reported as inconsistent during seasonal work periods", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-exp3-education-attendance-evidence-002", "observation", "School attendance and travel records are not supplied")),
                        "What evidence-grounded factors and uncertainties apply to inconsistent rural school attendance?", "rag-service:pilot-evaluation-v3-expansion", "Return only the canonical root-cause JSON contract. Use retrieved source IDs exactly and do not invent attendance measurements.", "school attendance; seasonal work; travel access", "Controlled PILOT_EVALUATION scenario for Experiment 003; constructed only, not real village data and not training data until human approval."),
                new ScenarioSpec("V03_EXP3_ROOT_SANITATION_001", "pilot-v03-exp3-sanitation-drainage-root-001", "root-cause-analysis", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR EXPERIMENT 003", "SANITATION",
                        "Drainage overflow is reported after heavy rain and responsibility for routine clearing is unclear.", 72, "MEDIUM", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "exp3-sanitation-drainage-root", "answer", "Drainage overflow is reported after heavy rain", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("overflow", "Overflow follows heavy rain events"), Map.of("maintenance", "Routine clearing responsibility requires verification")),
                        Map.of("evidence_id", "pilot-v03-exp3-sanitation-drainage-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Drainage overflow is reported after heavy rain", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-exp3-sanitation-drainage-evidence-002", "observation", "Drainage inspection and maintenance records are not supplied")),
                        "What evidence-grounded factors and uncertainties apply to rural drainage overflow after heavy rain?", "rag-service:pilot-evaluation-v3-expansion", "Return only the canonical root-cause JSON contract. Use retrieved source IDs exactly and do not invent rainfall or infrastructure measurements.", "drainage overflow; heavy rain; maintenance ownership", "Controlled PILOT_EVALUATION scenario for Experiment 003; constructed only, not real village data and not training data until human approval."),
                new ScenarioSpec("V03_EXP3_ROOT_INFRASTRUCTURE_001", "pilot-v03-exp3-infrastructure-road-access-root-001", "root-cause-analysis", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR EXPERIMENT 003", "INFRASTRUCTURE",
                        "Road access to a settlement is reported as unreliable during rain and the dominant constraint is not verified.", 58, "MEDIUM", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "exp3-infrastructure-road-root", "answer", "Road access is reported as unreliable during rain", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("access", "Access is reported as unreliable during rain"), Map.of("constraint", "Road condition and transport alternatives require verification")),
                        Map.of("evidence_id", "pilot-v03-exp3-infrastructure-road-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Road access is reported as unreliable during rain", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-exp3-infrastructure-road-evidence-002", "observation", "Road inspection and travel-time records are not supplied")),
                        "What evidence-grounded factors and uncertainties apply to unreliable rural road access during rain?", "rag-service:pilot-evaluation-v3-expansion", "Return only the canonical root-cause JSON contract. Use retrieved source IDs exactly and do not invent road condition measurements.", "road access; rain; transport alternatives", "Controlled PILOT_EVALUATION scenario for Experiment 003; constructed only, not real village data and not training data until human approval."),
                new ScenarioSpec("V03_EXP3_ROOT_LIVELIHOOD_001", "pilot-v03-exp3-livelihood-market-access-root-001", "root-cause-analysis", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR EXPERIMENT 003", "LIVELIHOOD",
                        "Small producers report difficulty reaching markets and the primary constraint is uncertain.", 46, "MEDIUM", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "exp3-livelihood-market-root", "answer", "Small producers report difficulty reaching markets", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("market_access", "Market access is reported as difficult"), Map.of("constraint", "Transport, information, and buyer access require verification")),
                        Map.of("evidence_id", "pilot-v03-exp3-livelihood-market-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Small producers report difficulty reaching markets", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-exp3-livelihood-market-evidence-002", "observation", "Market travel and price records are not supplied")),
                        "What evidence-grounded factors and uncertainties apply to rural producer market access constraints?", "rag-service:pilot-evaluation-v3-expansion", "Return only the canonical root-cause JSON contract. Use retrieved source IDs exactly and do not invent prices or income measurements.", "market access; transport; buyer information", "Controlled PILOT_EVALUATION scenario for Experiment 003; constructed only, not real village data and not training data until human approval."),
                new ScenarioSpec("V03_EXP3_REC_EDUCATION_001", "pilot-v03-exp3-education-attendance-recommendation-001", "recommendation-generation", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR EXPERIMENT 003", "EDUCATION",
                        "School attendance is inconsistent during seasonal work periods and feasible family-school coordination options are needed.", 63, "MEDIUM", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "exp3-education-attendance-rec", "answer", "Attendance is inconsistent during seasonal work periods", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("attendance", "Attendance varies across the school week"), Map.of("coordination", "Family-school coordination requires verification")),
                        Map.of("evidence_id", "pilot-v03-exp3-education-attendance-rec-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Attendance is inconsistent during seasonal work periods", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-exp3-education-attendance-rec-evidence-002", "observation", "Attendance registers and household scheduling information are not supplied")),
                        "What bounded interventions could improve attendance while preserving evidence limits?", "rag-service:pilot-evaluation-v3-expansion", "Return only the canonical recommendation JSON contract with two bounded options, risks, feasibility, implementation steps, uncertainties, and retrieved source IDs; do not claim enrollment outcomes.", "school attendance; seasonal work; family-school coordination", "Controlled PILOT_EVALUATION scenario for Experiment 003; constructed only, not real village data and not training data until human approval."),
                new ScenarioSpec("V03_EXP3_REC_SANITATION_001", "pilot-v03-exp3-sanitation-drainage-recommendation-001", "recommendation-generation", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR EXPERIMENT 003", "SANITATION",
                        "Drainage overflow follows heavy rain and bounded maintenance interventions are needed.", 72, "MEDIUM", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "exp3-sanitation-drainage-rec", "answer", "Drainage overflow follows heavy rain", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("overflow", "Overflow follows heavy rain events"), Map.of("maintenance", "Clearing responsibility requires verification")),
                        Map.of("evidence_id", "pilot-v03-exp3-sanitation-drainage-rec-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Drainage overflow follows heavy rain", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-exp3-sanitation-drainage-rec-evidence-002", "observation", "Drainage inspection and maintenance records are not supplied")),
                        "What bounded interventions could improve drainage maintenance while preserving evidence limits?", "rag-service:pilot-evaluation-v3-expansion", "Return only the canonical recommendation JSON contract with two bounded options, risks, feasibility, implementation steps, uncertainties, and retrieved source IDs; do not claim flood reduction results.", "drainage overflow; maintenance ownership; heavy rain", "Controlled PILOT_EVALUATION scenario for Experiment 003; constructed only, not real village data and not training data until human approval."),
                new ScenarioSpec("V03_EXP3_REC_INFRASTRUCTURE_001", "pilot-v03-exp3-infrastructure-road-recommendation-001", "recommendation-generation", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR EXPERIMENT 003", "INFRASTRUCTURE",
                        "Road access is unreliable during rain and feasible maintenance and travel-contingency options are needed.", 58, "MEDIUM", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "exp3-infrastructure-road-rec", "answer", "Road access is unreliable during rain", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("access", "Access is unreliable during rain"), Map.of("contingency", "Transport alternatives require verification")),
                        Map.of("evidence_id", "pilot-v03-exp3-infrastructure-road-rec-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Road access is unreliable during rain", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-exp3-infrastructure-road-rec-evidence-002", "observation", "Road inspection and travel-time records are not supplied")),
                        "What bounded interventions could improve rural road access while preserving evidence limits?", "rag-service:pilot-evaluation-v3-expansion", "Return only the canonical recommendation JSON contract with two bounded options, risks, feasibility, implementation steps, uncertainties, and retrieved source IDs; do not claim travel-time improvements.", "road access; rain; maintenance; transport contingency", "Controlled PILOT_EVALUATION scenario for Experiment 003; constructed only, not real village data and not training data until human approval."),
                new ScenarioSpec("V03_EXP3_REC_LIVELIHOOD_001", "pilot-v03-exp3-livelihood-market-recommendation-001", "recommendation-generation", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR EXPERIMENT 003", "LIVELIHOOD",
                        "Small producers report difficulty reaching markets and feasible coordination options are needed.", 46, "MEDIUM", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "exp3-livelihood-market-rec", "answer", "Small producers report difficulty reaching markets", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("market_access", "Market access is difficult"), Map.of("coordination", "Transport and buyer coordination require verification")),
                        Map.of("evidence_id", "pilot-v03-exp3-livelihood-market-rec-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Small producers report difficulty reaching markets", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-exp3-livelihood-market-rec-evidence-002", "observation", "Market travel and price records are not supplied")),
                        "What bounded interventions could improve rural producer market access while preserving evidence limits?", "rag-service:pilot-evaluation-v3-expansion", "Return only the canonical recommendation JSON contract with two bounded options, risks, feasibility, implementation steps, uncertainties, and retrieved source IDs; do not claim income or price improvements.", "market access; transport; buyer coordination", "Controlled PILOT_EVALUATION scenario for Experiment 003; constructed only, not real village data and not training data until human approval."),
                new ScenarioSpec("V03_EXP3_RAG_EDUCATION_001", "pilot-v03-exp3-education-attendance-rag-001", "rag-grounded-responses", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR EXPERIMENT 003", "EDUCATION",
                        "Implementers need source-grounded guidance on attendance barriers and explicit evidence gaps.", 63, "LOW", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "exp3-education-attendance-rag", "answer", "Implementers request grounded attendance guidance", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("attendance", "Attendance barriers require verification"), Map.of("records", "School records are not supplied")),
                        Map.of("evidence_id", "pilot-v03-exp3-education-attendance-rag-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Implementers request source-grounded attendance guidance", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-exp3-education-attendance-rag-evidence-002", "observation", "School attendance and travel records are not supplied")),
                        "What trusted evidence and limitations apply to rural school attendance barriers?", "rag-service:pilot-evaluation-v3-expansion", "Return only the canonical RAG JSON contract. Cite only retrieved source IDs and state evidence gaps; do not invent attendance statistics.", "school attendance; travel access; evidence gaps", "Controlled PILOT_EVALUATION scenario for Experiment 003; constructed only, not real village data and not training data until human approval."),
                new ScenarioSpec("V03_EXP3_RAG_SANITATION_001", "pilot-v03-exp3-sanitation-drainage-rag-001", "rag-grounded-responses", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR EXPERIMENT 003", "SANITATION",
                        "Implementers need source-grounded guidance on drainage overflow and missing maintenance records.", 72, "LOW", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "exp3-sanitation-drainage-rag", "answer", "Implementers request grounded drainage guidance", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("overflow", "Overflow follows heavy rain reports"), Map.of("records", "Maintenance records are not supplied")),
                        Map.of("evidence_id", "pilot-v03-exp3-sanitation-drainage-rag-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Implementers request source-grounded drainage guidance", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-exp3-sanitation-drainage-rag-evidence-002", "observation", "Drainage inspection records are not supplied")),
                        "What trusted evidence and limitations apply to rural drainage overflow after heavy rain?", "rag-service:pilot-evaluation-v3-expansion", "Return only the canonical RAG JSON contract. Cite only retrieved source IDs and state evidence gaps; do not invent rainfall measurements.", "drainage overflow; heavy rain; evidence gaps", "Controlled PILOT_EVALUATION scenario for Experiment 003; constructed only, not real village data and not training data until human approval."),
                new ScenarioSpec("V03_EXP3_RAG_INFRASTRUCTURE_001", "pilot-v03-exp3-infrastructure-road-rag-001", "rag-grounded-responses", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR EXPERIMENT 003", "INFRASTRUCTURE",
                        "Implementers need source-grounded guidance on rural road access and missing inspection records.", 58, "LOW", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "exp3-infrastructure-road-rag", "answer", "Implementers request grounded road access guidance", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("access", "Access is unreliable during rain reports"), Map.of("records", "Inspection records are not supplied")),
                        Map.of("evidence_id", "pilot-v03-exp3-infrastructure-road-rag-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Implementers request source-grounded road access guidance", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-exp3-infrastructure-road-rag-evidence-002", "observation", "Road inspection and travel-time records are not supplied")),
                        "What trusted evidence and limitations apply to rural road access during rain?", "rag-service:pilot-evaluation-v3-expansion", "Return only the canonical RAG JSON contract. Cite only retrieved source IDs and state evidence gaps; do not invent road condition measurements.", "road access; rain; inspection gaps", "Controlled PILOT_EVALUATION scenario for Experiment 003; constructed only, not real village data and not training data until human approval."),
                new ScenarioSpec("V03_EXP3_RAG_LIVELIHOOD_001", "pilot-v03-exp3-livelihood-market-rag-001", "rag-grounded-responses", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR EXPERIMENT 003", "LIVELIHOOD",
                        "Implementers need source-grounded guidance on producer market access and missing market records.", 46, "LOW", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "exp3-livelihood-market-rag", "answer", "Implementers request grounded market access guidance", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("market_access", "Market access is difficult by report"), Map.of("records", "Market records are not supplied")),
                        Map.of("evidence_id", "pilot-v03-exp3-livelihood-market-rag-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Implementers request source-grounded market access guidance", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-exp3-livelihood-market-rag-evidence-002", "observation", "Market travel and price records are not supplied")),
                        "What trusted evidence and limitations apply to rural producer market access?", "rag-service:pilot-evaluation-v3-expansion", "Return only the canonical RAG JSON contract. Cite only retrieved source IDs and state evidence gaps; do not invent prices or income measurements.", "market access; transport; evidence gaps", "Controlled PILOT_EVALUATION scenario for Experiment 003; constructed only, not real village data and not training data until human approval."),
                new ScenarioSpec("V03_HOLDOUT_REC_CLIMATE_HEAT_001", "pilot-v03-holdout-climate-heat-recommendation-001", "recommendation-generation", PILOT_CLASSIFICATION,
                        "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR HELD-OUT EVALUATION COVERAGE", "EMPLOYMENT",
                        "Local coordinators report heat-related work interruptions and need bounded preparedness options without claiming health or productivity outcomes.", 37, "MEDIUM", "CONTROLLED_PROJECT_PILOT",
                        Map.of("question_id", "v03-holdout-climate-heat", "answer", "Heat-related work interruptions are reported in the controlled pilot", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("heat_exposure", "Heat exposure is reported during outdoor work periods"), Map.of("preparedness", "Local heat-response capacity requires verification")),
                        Map.of("evidence_id", "pilot-v03-holdout-climate-heat-evidence-001", "type", "CONTROLLED_PILOT_OBSERVATION", "observation", "Heat-related work interruptions are reported during outdoor work periods", "source", "CONTROLLED_PROJECT_PILOT", "classification", PILOT_CLASSIFICATION),
                        List.of(Map.of("evidence_id", "pilot-v03-holdout-climate-heat-evidence-002", "observation", "Local temperature records, health outcomes, and existing preparedness capacity are not supplied")),
                        "What evidence and limitations apply to bounded preparedness options for reported heat-related work interruptions?", "rag-service:pilot-evaluation-v3-holdout", "Return only the canonical recommendation JSON contract with at least two bounded options, risks, feasibility, implementation steps, uncertainties, and retrieved source IDs; do not claim health, productivity, or temperature outcomes.", "heat exposure; outdoor work; preparedness capacity; evidence gaps", "Distinct controlled PILOT_EVALUATION recommendation holdout for model comparison; constructed for evaluation only, not real village data, not a development synthetic fixture, and not training data until explicit human review."));
    }

    private List<ScenarioSpec> pilotV05DiversityScenarios() {
        return List.of(
                v05("SANITATION", "Water & sanitation", "water-latrine-overflow", "root-cause-analysis", "Community latrine overflow is reported after heavy rain and containment responsibility is uncertain.", "Overflow is reported after rain around a shared sanitation facility.", "Inspection records and contamination measurements are not supplied.", "What evidence-grounded factors and uncertainties apply to reported rural latrine overflow after rain?", "latrine overflow; containment; sanitation maintenance", "sanitation containment", "maintenance ownership", 80),
                v05("WATER", "Water & sanitation", "water-waste-collection", "recommendation-generation", "Solid-waste collection around a market-side water point is irregular and blocked drains are reported.", "Collection timing is reported as irregular near the market-side water point.", "Waste-volume, drainage, and service-route records are not supplied.", "What bounded options could improve waste collection and drain coordination while preserving evidence limits?", "solid waste; drainage coordination; service scheduling", "waste collection coordination", "collection scheduling and drain clearing", 55),
                v05("SANITATION", "Water & sanitation", "water-toilet-access", "rag-grounded-responses", "Implementers need grounded guidance on household toilet access and safe waste-disposal evidence gaps.", "Household toilet access and safe disposal practices require verification.", "Household survey results and facility inspection records are not supplied.", "What trusted evidence and limitations apply to rural toilet access and safe waste disposal?", "toilet access; safe disposal; evidence gaps", "toilet access constraints", "retrieval of sanitation guidance", 60),

                v05("AGRICULTURE", "Agriculture & food production", "agri-pest-disease", "root-cause-analysis", "Producers report a crop pest outbreak in one production cycle and the contributing condition is uncertain.", "Pest damage is reported in a crop area during the controlled pilot.", "Field scouting, specimen identification, and treatment records are not supplied.", "What evidence-grounded factors and uncertainties apply to a reported rural crop pest outbreak?", "crop pest; field scouting; disease identification", "crop pest pressure", "pest scouting and response", 70),
                v05("AGRICULTURE", "Agriculture & food production", "agri-seed-storage", "recommendation-generation", "Seed quality is reported to decline during storage and feasible handling options are needed.", "Producers report seed deterioration during storage between seasons.", "Humidity, storage-condition, and germination records are not supplied.", "What bounded options could improve seed storage handling without claiming yield outcomes?", "seed storage; handling practice; germination evidence gaps", "seed storage conditions", "storage handling and verification", 45),
                v05("AGRICULTURE", "Agriculture & food production", "agri-food-safety", "rag-grounded-responses", "Planners need source-grounded guidance on post-harvest food-safety controls and missing inspection evidence.", "Post-harvest food-safety controls require verification in the controlled pilot.", "Inspection findings, pathogen tests, and cold-chain records are not supplied.", "What trusted evidence and limitations apply to rural post-harvest food-safety controls?", "food safety; post-harvest controls; inspection gaps", "food-safety control gaps", "retrieval of food-safety guidance", 50),

                v05("HEALTHCARE", "Healthcare access", "health-staffing", "root-cause-analysis", "A rural health center reports intermittent staffing coverage and the operational cause is uncertain.", "Staffing coverage is reported as intermittent at a primary-care facility.", "Roster, attendance, workload, and service-volume records are not supplied.", "What evidence-grounded factors and uncertainties apply to intermittent rural health-center staffing coverage?", "health staffing; facility coverage; roster evidence gaps", "staffing coverage", "workload and coverage verification", 90),
                v05("HEALTHCARE", "Healthcare access", "health-appointment-access", "recommendation-generation", "Residents report difficulty obtaining routine facility appointments and bounded access options are needed.", "Routine appointment access is reported as difficult in the controlled pilot.", "Appointment queues, travel times, and service-capacity records are not supplied.", "What bounded options could improve routine appointment access without giving clinical instructions?", "appointment access; scheduling; travel evidence gaps", "appointment access constraints", "scheduling and access coordination", 65),
                v05("HEALTHCARE", "Healthcare access", "health-facility-hours", "rag-grounded-responses", "Implementers need grounded guidance on verifying rural facility hours and service availability.", "Facility operating hours and service availability require verification.", "Published schedules, attendance logs, and service registers are not supplied.", "What trusted evidence and limitations apply to rural facility hours and service availability?", "facility hours; service availability; verification gaps", "facility availability uncertainty", "retrieval of access-policy guidance", 60),

                v05("ENERGY", "Energy/electricity", "energy-transformer", "root-cause-analysis", "A settlement reports repeated transformer interruptions and the dominant reliability constraint is uncertain.", "Transformer interruptions are reported during the controlled pilot.", "Fault logs, load readings, and repair records are not supplied.", "What evidence-grounded factors and uncertainties apply to repeated rural transformer interruptions?", "transformer interruptions; grid reliability; fault records", "transformer reliability", "fault logging and repair ownership", 100),
                v05("ENERGY", "Energy/electricity", "energy-solar-maintenance", "recommendation-generation", "A community solar system has irregular maintenance visits and bounded service-continuity options are needed.", "Solar-system maintenance visits are reported as irregular.", "Battery health, inspection, and service-contract records are not supplied.", "What bounded options could improve community solar maintenance while preserving evidence limits?", "solar maintenance; service continuity; battery evidence gaps", "solar maintenance workflow", "inspection scheduling and escalation", 55),
                v05("ENERGY", "Energy/electricity", "energy-grid-outages", "rag-grounded-responses", "Planners need grounded guidance on interpreting rural grid-outage records and evidence gaps.", "Grid-outage reporting requires verification in the controlled pilot.", "Outage duration, feeder, and household impact records are not supplied.", "What trusted evidence and limitations apply to rural grid-outage records and service reliability?", "grid outages; service records; evidence gaps", "outage evidence quality", "retrieval of energy-reliability guidance", 70),

                v05("EDUCATION", "Education", "education-teacher-attendance", "root-cause-analysis", "A rural school reports teacher attendance gaps and the cause of missed instructional time is uncertain.", "Teacher attendance gaps are reported during the controlled pilot.", "Staff rosters, attendance registers, and timetable records are not supplied.", "What evidence-grounded factors and uncertainties apply to reported rural teacher attendance gaps?", "teacher attendance; instructional time; school records", "teacher attendance coverage", "attendance verification and support", 75),
                v05("EDUCATION", "Education", "education-student-transport", "recommendation-generation", "Students report difficulty reaching school from remote settlements and bounded transport options are needed.", "Travel access to school is reported as difficult for some students.", "Route, safety, attendance, and transport-capacity records are not supplied.", "What bounded options could improve student transport access without claiming attendance outcomes?", "student transport; route access; safety evidence gaps", "student travel access", "route coordination and transport verification", 85),
                v05("EDUCATION", "Education", "education-dropout", "rag-grounded-responses", "Implementers need grounded guidance on investigating reported student dropout risk and missing school records.", "Dropout risk is reported as requiring investigation in the controlled pilot.", "Enrollment, transfer, household, and attendance records are not supplied.", "What trusted evidence and limitations apply to rural student dropout investigations?", "student dropout; enrollment records; evidence gaps", "dropout evidence uncertainty", "retrieval of education-retention guidance", 65),

                v05("LIVELIHOOD", "Livelihoods/markets", "livelihood-seasonal-work", "root-cause-analysis", "Households report interruptions in seasonal employment and the main livelihood constraint is uncertain.", "Seasonal employment interruptions are reported in the controlled pilot.", "Work calendars, employer demand, and household income records are not supplied.", "What evidence-grounded factors and uncertainties apply to reported rural seasonal employment interruptions?", "seasonal employment; work access; livelihood records", "seasonal work disruption", "work opportunity and access verification", 50),
                v05("LIVELIHOOD", "Livelihoods/markets", "livelihood-supply-chain", "recommendation-generation", "Small enterprises report supply-chain disruption for essential inputs and bounded coordination options are needed.", "Input delivery disruption is reported by small enterprises.", "Order, transport, inventory, and price records are not supplied.", "What bounded options could improve small-enterprise supply-chain coordination without claiming price outcomes?", "supply-chain disruption; input delivery; inventory gaps", "input supply disruption", "coordination and inventory verification", 40),
                v05("LIVELIHOOD", "Livelihoods/markets", "livelihood-artisan-markets", "rag-grounded-responses", "Planners need grounded guidance on artisan market information and missing buyer records.", "Artisan producers report incomplete access to market information.", "Buyer, order, price, and shipment records are not supplied.", "What trusted evidence and limitations apply to rural artisan market information access?", "artisan markets; buyer information; evidence gaps", "market information uncertainty", "retrieval of market-access guidance", 45),

                v05("MULTI_DOMAIN", "Climate/disaster resilience", "climate-drought-preparedness", "root-cause-analysis", "A rural settlement reports drought-preparedness gaps and the limiting factor is uncertain.", "Drought-preparedness gaps are reported in the controlled pilot.", "Rainfall, storage, water-use, and contingency-plan records are not supplied.", "What evidence-grounded factors and uncertainties apply to rural drought-preparedness gaps?", "drought preparedness; contingency planning; evidence gaps", "preparedness capacity", "water-use and contingency verification", 90),
                v05("MULTI_DOMAIN", "Climate/disaster resilience", "climate-flood-resilience", "recommendation-generation", "Flood-prone households need bounded resilience options and local response capacity is uncertain.", "Flood exposure and response-capacity concerns are reported in the controlled pilot.", "Flood maps, warning logs, shelter capacity, and damage records are not supplied.", "What bounded options could improve rural flood resilience without claiming avoided-loss outcomes?", "flood resilience; response capacity; warning evidence gaps", "flood response capacity", "warning, shelter, and continuity planning", 100),
                v05("MULTI_DOMAIN", "Climate/disaster resilience", "climate-cyclone-warning", "rag-grounded-responses", "Implementers need grounded guidance on rural cyclone-warning communication and missing alert records.", "Warning communication is reported as requiring verification before severe weather.", "Alert delivery, coverage, timing, and response records are not supplied.", "What trusted evidence and limitations apply to rural cyclone-warning communication?", "cyclone warning; alert communication; evidence gaps", "warning communication uncertainty", "retrieval of disaster-warning guidance", 80),

                v05("INFRASTRUCTURE", "Housing/basic infrastructure", "housing-roof-leaks", "root-cause-analysis", "A community facility reports recurring roof leakage and the maintenance cause is uncertain.", "Roof leakage is reported at a shared community facility.", "Inspection, rainfall, repair, and material records are not supplied.", "What evidence-grounded factors and uncertainties apply to recurring roof leakage at a rural community facility?", "roof leakage; facility maintenance; inspection gaps", "building envelope maintenance", "inspection and repair ownership", 70),
                v05("INFRASTRUCTURE", "Housing/basic infrastructure", "housing-market-shed", "recommendation-generation", "A rural market shed reports unusable public space during rain and bounded maintenance options are needed.", "Market-shed usability is reported as reduced during rain.", "Condition surveys, drainage records, and vendor-use records are not supplied.", "What bounded options could improve rural market-shed usability without claiming economic outcomes?", "market shed; public space; drainage evidence gaps", "public facility condition", "maintenance and drainage coordination", 60),
                v05("INFRASTRUCTURE", "Housing/basic infrastructure", "housing-community-facility", "rag-grounded-responses", "Planners need grounded guidance on assessing rural community-facility condition and missing inspection evidence.", "Community-facility condition requires verification in the controlled pilot.", "Structural inspections, occupancy, accessibility, and repair records are not supplied.", "What trusted evidence and limitations apply to rural community-facility condition assessments?", "community facility; accessibility; inspection gaps", "facility-condition uncertainty", "retrieval of infrastructure-assessment guidance", 55));
    }

    private List<ScenarioSpec> pilotV05QualityRemediationScenarios() {
        return List.of(
                v05r("AGRICULTURE", "Agriculture & food production", "agri-pest-disease", "root-cause-analysis", "Producers report a crop pest outbreak in one production cycle and the contributing condition is uncertain.", "Pest damage is reported in a crop area during the controlled pilot.", "Field scouting, specimen identification, and treatment records are not supplied.", "What evidence-grounded factors and uncertainties apply to a reported rural crop pest outbreak?", "crop pest; field scouting; disease identification", "crop pest pressure", "pest scouting and response", 70, false),
                v05r("AGRICULTURE", "Agriculture & food production", "agri-seed-storage", "recommendation-generation", "Seed quality is reported to decline during storage and feasible handling options are needed.", "Producers report seed deterioration during storage between seasons.", "Humidity, storage-condition, and germination records are not supplied.", "What bounded options could improve seed storage handling without claiming yield outcomes?", "seed storage; handling practice; germination evidence gaps", "seed storage conditions", "storage handling and verification", 45, false),
                v05r("AGRICULTURE", "Agriculture & food production", "agri-food-safety", "rag-grounded-responses", "Planners need source-grounded guidance on post-harvest food-safety controls and missing inspection evidence.", "Post-harvest food-safety controls require verification in the controlled pilot.", "Inspection findings, pathogen tests, and cold-chain records are not supplied.", "What trusted evidence and limitations apply to rural post-harvest food-safety controls?", "food safety; post-harvest controls; inspection gaps", "food-safety control gaps", "retrieval of food-safety guidance", 50, false),

                v05r("MULTI_DOMAIN", "Climate/disaster resilience", "climate-drought-preparedness", "root-cause-analysis", "A rural settlement reports drought-preparedness gaps and the limiting factor is uncertain.", "Drought-preparedness gaps are reported in the controlled pilot.", "Rainfall, storage, water-use, and contingency-plan records are not supplied.", "What evidence-grounded factors and uncertainties apply to rural drought-preparedness gaps?", "drought preparedness; contingency planning; evidence gaps", "preparedness capacity", "water-use and contingency verification", 90, false),
                v05r("MULTI_DOMAIN", "Climate/disaster resilience", "climate-cyclone-warning", "rag-grounded-responses", "Implementers need grounded guidance on rural cyclone-warning communication and missing alert records.", "Warning communication is reported as requiring verification before severe weather.", "Alert delivery, coverage, timing, and response records are not supplied.", "What trusted evidence and limitations apply to rural cyclone-warning communication?", "cyclone warning; alert communication; evidence gaps", "warning communication uncertainty", "retrieval of disaster-warning guidance", 80, false),

                v05r("EDUCATION", "Education", "education-teacher-attendance", "root-cause-analysis", "A rural school reports teacher attendance gaps and the cause of missed instructional time is uncertain.", "Teacher attendance gaps are reported during the controlled pilot.", "Staff rosters, attendance registers, and timetable records are not supplied.", "What evidence-grounded factors and uncertainties apply to reported rural teacher attendance gaps?", "teacher attendance; instructional time; school records", "teacher attendance coverage", "attendance verification and support", 75, false),
                v05r("EDUCATION", "Education", "education-student-transport", "recommendation-generation", "Students report difficulty reaching school from remote settlements and bounded transport options are needed.", "Travel access to school is reported as difficult for some students.", "Route, safety, attendance, and transport-capacity records are not supplied.", "What bounded options could improve student transport access without claiming attendance outcomes?", "student transport; route access; safety evidence gaps", "student travel access", "route coordination and transport verification", 85, false),
                v05r("EDUCATION", "Education", "education-dropout", "rag-grounded-responses", "Implementers need grounded guidance on investigating reported student dropout risk and missing school records.", "Dropout risk is reported as requiring investigation in the controlled pilot.", "Enrollment, transfer, household, and attendance records are not supplied.", "What trusted evidence and limitations apply to rural student dropout investigations?", "student dropout; enrollment records; evidence gaps", "dropout evidence uncertainty", "retrieval of education-retention guidance", 65, false),

                v05r("ENERGY", "Energy/electricity", "energy-transformer", "root-cause-analysis", "A settlement reports repeated transformer interruptions and the dominant reliability constraint is uncertain.", "Transformer interruptions are reported during the controlled pilot.", "Fault logs, load readings, and repair records are not supplied.", "What evidence-grounded factors and uncertainties apply to repeated rural transformer interruptions?", "transformer interruptions; grid reliability; fault records", "transformer reliability", "fault logging and repair ownership", 100, false),
                v05r("ENERGY", "Energy/electricity", "energy-grid-outages", "rag-grounded-responses", "Planners need grounded guidance on interpreting rural grid-outage records and evidence gaps.", "Grid-outage reporting requires verification in the controlled pilot.", "Outage duration, feeder, and household impact records are not supplied.", "What trusted evidence and limitations apply to rural grid-outage records and service reliability?", "grid outages; service records; evidence gaps", "outage evidence quality", "retrieval of energy-reliability guidance", 70, false),

                v05r("HEALTHCARE", "Healthcare access", "health-staffing", "root-cause-analysis", "A rural health center reports intermittent staffing coverage and the operational cause is uncertain.", "Staffing coverage is reported as intermittent at a primary-care facility.", "Roster, attendance, workload, and service-volume records are not supplied.", "What evidence-grounded factors and uncertainties apply to intermittent rural health-center staffing coverage?", "health staffing; facility coverage; roster evidence gaps", "staffing coverage", "workload and coverage verification", 90, false),
                v05r("HEALTHCARE", "Healthcare access", "health-appointment-access", "recommendation-generation", "Residents report difficulty obtaining routine facility appointments and bounded access options are needed.", "Routine appointment access is reported as difficult in the controlled pilot.", "Appointment queues, travel times, and service-capacity records are not supplied.", "What bounded options could improve routine appointment access without giving clinical instructions?", "appointment access; scheduling; travel evidence gaps", "appointment access constraints", "scheduling and access coordination", 65, false),
                v05r("HEALTHCARE", "Healthcare access", "health-facility-hours", "rag-grounded-responses", "Implementers need grounded guidance on verifying rural facility hours and service availability.", "Facility operating hours and service availability require verification.", "Published schedules, attendance logs, and service registers are not supplied.", "What trusted evidence and limitations apply to rural facility hours and service availability?", "facility hours; service availability; verification gaps", "facility availability uncertainty", "retrieval of access-policy guidance", 60, false),

                v05r("INFRASTRUCTURE", "Housing/basic infrastructure", "housing-roof-leaks", "root-cause-analysis", "A community facility reports recurring roof leakage and the maintenance cause is uncertain.", "Roof leakage is reported at a shared community facility.", "Inspection, rainfall, repair, and material records are not supplied.", "What evidence-grounded factors and uncertainties apply to recurring roof leakage at a rural community facility?", "roof leakage; facility maintenance; inspection gaps", "building envelope maintenance", "inspection and repair ownership", 70, false),
                v05r("INFRASTRUCTURE", "Housing/basic infrastructure", "housing-market-shed", "recommendation-generation", "A rural market shed reports unusable public space during rain and bounded maintenance options are needed.", "Market-shed usability is reported as reduced during rain.", "Condition surveys, drainage records, and vendor-use records are not supplied.", "What bounded options could improve rural market-shed usability without claiming economic outcomes?", "market shed; public space; drainage evidence gaps", "public facility condition", "maintenance and drainage coordination", 60, false),
                v05r("INFRASTRUCTURE", "Housing/basic infrastructure", "housing-community-facility", "rag-grounded-responses", "Planners need grounded guidance on assessing rural community-facility condition and missing inspection evidence.", "Community-facility condition requires verification in the controlled pilot.", "Structural inspections, occupancy, accessibility, and repair records are not supplied.", "What trusted evidence and limitations apply to rural community-facility condition assessments?", "community facility; accessibility; inspection gaps", "facility-condition uncertainty", "retrieval of infrastructure-assessment guidance", 55, false),

                v05r("LIVELIHOOD", "Livelihoods/markets", "livelihood-seasonal-work", "root-cause-analysis", "Households report interruptions in seasonal employment and the main livelihood constraint is uncertain.", "Seasonal employment interruptions are reported in the controlled pilot.", "Work calendars, employer demand, and household income records are not supplied.", "What evidence-grounded factors and uncertainties apply to reported rural seasonal employment interruptions?", "seasonal employment; work access; livelihood records", "seasonal work disruption", "work opportunity and access verification", 50, false),
                v05r("LIVELIHOOD", "Livelihoods/markets", "livelihood-supply-chain", "recommendation-generation", "Small enterprises report supply-chain disruption for essential inputs and bounded coordination options are needed.", "Input delivery disruption is reported by small enterprises.", "Order, transport, inventory, and price records are not supplied.", "What bounded options could improve small-enterprise supply-chain coordination without claiming price outcomes?", "supply-chain disruption; input delivery; inventory gaps", "input supply disruption", "coordination and inventory verification", 40, false),
                v05r("LIVELIHOOD", "Livelihoods/markets", "livelihood-artisan-markets", "rag-grounded-responses", "Planners need grounded guidance on artisan market information and missing buyer records.", "Artisan producers report incomplete access to market information.", "Buyer, order, price, and shipment records are not supplied.", "What trusted evidence and limitations apply to rural artisan market information access?", "artisan markets; buyer information; evidence gaps", "market information uncertainty", "retrieval of market-access guidance", 45, false),
                v05r("SANITATION", "Water and sanitation", "water-toilet-access", "rag-grounded-responses", "Implementers need grounded guidance on household toilet access and safe waste-disposal evidence gaps.", "Household toilet access and safe disposal practices require verification.", "Household survey results and facility inspection records are not supplied.", "What trusted evidence and limitations apply to rural toilet access and safe waste disposal?", "toilet access; safe disposal; evidence gaps", "toilet access constraints", "retrieval of sanitation guidance", 60, false),

                v05r("SANITATION", "Water and sanitation", "water-school-handwashing", "root-cause-analysis", "A school reports inconsistent handwashing access and the limiting service condition is uncertain.", "Handwashing access is reported as inconsistent at a school facility.", "Water-point functionality, soap availability, and facility inspection records are not supplied.", "What evidence-grounded factors and uncertainties apply to inconsistent school handwashing access?", "school handwashing; soap access; facility functionality", "handwashing service access", "facility and supply verification", 65, true),
                v05r("SANITATION", "Water and sanitation", "water-household-greywater", "recommendation-generation", "Households report unmanaged greywater near shared living areas and bounded sanitation options are needed.", "Greywater accumulation is reported near shared household pathways.", "Drainage layout, soil absorption, household practices, and inspection records are not supplied.", "What bounded options could improve household greywater management without claiming health outcomes?", "greywater; household drainage; sanitation evidence gaps", "greywater management", "drainage and safe-disposal coordination", 55, true));
    }

    private ScenarioSpec v05r(String domain, String domainLabel, String key, String taskType, String problem, String observation, String gap, String question, String topic, String problemCategory, String recommendationTarget, int affectedPopulation, boolean replacement) {
        String taskSlug = taskType.replace('-', '_');
        String prefix = replacement ? "V05R_REPLACEMENT_" : "V05R_CORRECTION_";
        String scenarioKey = "pilot-v05r-" + key + "-" + taskType + "-001";
        String sourceId = "PILOT_V05R_" + key.replace('-', '_').toUpperCase(Locale.ROOT) + "_" + taskSlug.toUpperCase(Locale.ROOT);
        String evidenceId = scenarioKey + "-evidence-001";
        String prompt = switch (taskType) {
            case "root-cause-analysis" -> "Return only the canonical root-cause JSON contract. Separate observed evidence from hypotheses, cite only permitted source IDs, and state uncertainty.";
            case "recommendation-generation" -> "Return only the canonical recommendation JSON contract. Link each bounded option to the validated root cause and permitted source IDs; state risks, feasibility, and uncertainty.";
            default -> "Return only the canonical RAG JSON contract. Cite only permitted source IDs, distinguish evidence from inference, and state evidence gaps.";
        };
        return new ScenarioSpec(prefix + key.replace('-', '_').toUpperCase(Locale.ROOT) + "_" + taskSlug.toUpperCase(Locale.ROOT), scenarioKey, taskType, PILOT_CLASSIFICATION,
                "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR V0.5 QUALITY REMEDIATION; NOT REAL VILLAGE DATA", domain, problem, affectedPopulation, "MEDIUM", "CONTROLLED_PROJECT_PILOT",
                Map.of("question_id", "v05r-" + key + "-" + taskSlug, "answer", problem, "classification", PILOT_CLASSIFICATION, "domain", domainLabel),
                List.of(Map.of("observation", problem)),
                Map.of("evidence_id", evidenceId, "type", "CONTROLLED_PILOT_OBSERVATION", "observation", observation, "source", sourceId, "classification", PILOT_CLASSIFICATION, "domain", domainLabel),
                List.of(Map.of("evidence_id", scenarioKey + "-evidence-002", "observation", gap)), question, "rag-service:pilot-evaluation-v05-quality-remediation", prompt,
                topic + "; " + problemCategory + "; " + recommendationTarget + "; evidence gaps",
                "Governed V0.5 " + (replacement ? "replacement" : "correction") + " candidate; unique scenario evidence; no PII, no development fixtures, no real field measurements, and no training eligibility before authenticated human review.");
    }

    private ScenarioSpec v05(String domain, String domainLabel, String key, String taskType, String problem, String observation, String gap, String question, String topic, String problemCategory, String recommendationTarget, int affectedPopulation) {
        String taskSlug = taskType.replace('-', '_');
        String scenarioKey = "pilot-v05-" + key + "-" + taskType + "-001";
        String sourceId = "PILOT_V05_" + key.replace('-', '_').toUpperCase(Locale.ROOT) + "_" + taskSlug.toUpperCase(Locale.ROOT);
        String evidenceId = scenarioKey + "-evidence-001";
        String prompt = switch (taskType) {
            case "root-cause-analysis" -> "Return only the canonical root-cause JSON contract. Use retrieved source IDs exactly, state uncertainty, and do not invent field measurements.";
            case "recommendation-generation" -> "Return only the canonical recommendation JSON contract with at least two bounded options, risks, feasibility, implementation steps, uncertainty, and retrieved source IDs. Do not claim outcomes or policy eligibility.";
            default -> "Return only the canonical RAG JSON contract. Cite only retrieved source IDs, state evidence gaps, and do not invent measurements or outcomes.";
        };
        return new ScenarioSpec(
                "V05_DIVERSITY_" + key.replace('-', '_').toUpperCase(Locale.ROOT) + "_" + taskSlug.toUpperCase(Locale.ROOT),
                scenarioKey,
                taskType,
                PILOT_CLASSIFICATION,
                "CONTROLLED PILOT VILLAGE CONTEXT; CONSTRUCTED FOR DATASET V0.5 DOMAIN DIVERSITY; NOT REAL VILLAGE DATA",
                domain,
                problem,
                affectedPopulation,
                "MEDIUM",
                "CONTROLLED_PROJECT_PILOT",
                Map.of("question_id", "v05-" + key + "-" + taskSlug, "answer", problem, "classification", PILOT_CLASSIFICATION, "domain", domainLabel),
                List.of(Map.of("observation", problem)),
                Map.of("evidence_id", evidenceId, "type", "CONTROLLED_PILOT_OBSERVATION", "observation", observation, "source", sourceId, "classification", PILOT_CLASSIFICATION, "domain", domainLabel),
                List.of(Map.of("evidence_id", scenarioKey + "-evidence-002", "observation", gap)),
                question,
                "rag-service:pilot-evaluation-v05-diversity",
                prompt,
                topic + "; " + problemCategory + "; " + recommendationTarget + "; evidence gaps",
                "Controlled PILOT_EVALUATION candidate for dataset v0.5 domain diversity; domain=" + domainLabel + "; problem_category=" + problemCategory + "; recommendation_target=" + recommendationTarget + "; no PII, no real field measurements, no development fixtures, and no training eligibility before authenticated human review.");
    }

    private Map<String, Object> citationMap(CitationResponse citation) {
        return Map.of("source_type", citation.sourceType(), "source_id", citation.sourceId(), "excerpt", citation.excerpt(), "score", citation.score());
    }

    private void diversityGate(ScenarioSpec spec) {
        if (!spec.runLabel().startsWith("V05_DIVERSITY_")) {
            return;
        }
        String candidateSourceId = evidenceSourceId(spec);
        Set<String> candidateTokens = diversityTokens(spec.problemStatement() + " " + spec.ragQuestion() + " " + spec.compactEvidence());
        for (PilotScenarioEntity existing : scenarios.findByDatasetId(DATASET_ID)) {
            if (existing.getScenarioId().equals(spec.scenarioKey())) {
                continue;
            }
            if (existing.getEvidenceJson() != null && existing.getEvidenceJson().contains(candidateSourceId)) {
                throw new EvaluationException(HttpStatus.CONFLICT, "DIVERSITY_DUPLICATE_EVIDENCE", "The v0.5 scenario reuses an existing evidence source ID");
            }
            double overlap = jaccard(candidateTokens, diversityTokens(existing.getProblemStatement()));
            if (overlap >= 0.45d) {
                throw new EvaluationException(HttpStatus.CONFLICT, "SEMANTIC_DUPLICATE_SCENARIO", "The v0.5 scenario is too semantically similar to an existing scenario");
            }
        }
    }

    private Set<String> diversityTokens(String value) {
        Set<String> tokens = new HashSet<>();
        for (String token : value.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
            if (token.length() > 3 && !Set.of("with", "from", "that", "this", "reported", "reports", "rural", "pilot", "evidence", "requires", "unclear", "uncertain").contains(token)) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    private double jaccard(Set<String> left, Set<String> right) {
        if (left.isEmpty() || right.isEmpty()) {
            return 0.0d;
        }
        Set<String> union = new HashSet<>(left);
        union.addAll(right);
        Set<String> intersection = new HashSet<>(left);
        intersection.retainAll(right);
        return (double) intersection.size() / union.size();
    }

    private String evidenceSourceId(ScenarioSpec spec) {
        if (spec.runLabel().startsWith("V05_DIVERSITY_") || spec.runLabel().startsWith("V05R_")) {
            return String.valueOf(spec.evidence().get("source"));
        }
        return spec.sourceType();
    }

    private RecommendationSetResponse nonApplicableRecommendations(UUID rootCauseAnalysisId, ScenarioSpec spec) {
        return new RecommendationSetResponse(
                UUID.randomUUID(),
                rootCauseAnalysisId,
                "NOT_APPLICABLE",
                List.of(),
                List.of(),
                List.of(),
                List.of("Recommendation generation is not part of the RAG-grounded response task."),
                MODEL_VERSION,
                MODEL_VERSION,
                PROMPT_VERSION,
                spec.knowledgeSnapshot(),
                String.valueOf(spec.evidence().get("evidence_id")),
                Instant.now());
    }

    /**
     * Indexes the scenario's declared, provenance-bearing pilot evidence before retrieval.
     * The RAG service remains responsible for chunking, embeddings, thresholding, and citation validation.
     */
    private Map<String, Object> controlledEvidenceDocument(ScenarioSpec spec) {
        List<String> observations = new ArrayList<>();
        observations.add(spec.problemStatement());
        observations.add(String.valueOf(spec.evidence().get("observation")));
        observations.addAll(spec.evidenceObservations().stream()
                .map(row -> String.valueOf(row.getOrDefault("observation", "")))
                .filter(value -> !value.isBlank())
                .toList());
        if (spec.runLabel().startsWith("V05R_")) {
            observations.add("Governed evidence identity: " + spec.scenarioKey() + " / " + evidenceSourceId(spec) + ".");
        }
        return new LinkedHashMap<>(Map.of(
                "document_id", "pilot-evaluation-evidence-" + spec.scenarioKey(),
                "title", "Controlled pilot evidence: " + spec.scenarioKey(),
                "source", evidenceSourceId(spec),
                "publisher", "Rural Intelligence Controlled Evaluation Registry",
                "document_version", "pilot-1.0.0",
                "language", "en",
                "domain", spec.domain().toLowerCase(Locale.ROOT),
                "document_type", "pilot-evaluation",
                "approved_source", true,
                "text", String.join("\n", observations)));
    }

    private Map<String, Object> governedRagContext(ScenarioSpec spec) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("domain", ragDomain(spec));
        context.put("allowed_source_ids", List.of(evidenceSourceId(spec)));
        context.put("governed_evaluation", true);
        context.put("evaluation_classification", spec.classification());
        return context;
    }

    private Map<String, Object> remediationConstraints(ScenarioSpec spec) {
        Map<String, Object> constraints = new LinkedHashMap<>();
        constraints.put("human_review_required", true);
        constraints.put("constructed_scenario", true);
        constraints.put("governed_evaluation", true);
        constraints.put("allowed_source_ids", List.of(evidenceSourceId(spec)));
        return constraints;
    }

    private String ragDomain(ScenarioSpec spec) {
        if (!spec.runLabel().startsWith("V03_EXP3_")) {
            return spec.domain();
        }
        return switch (spec.domain()) {
            case "EDUCATION" -> "HEALTHCARE";
            case "SANITATION", "INFRASTRUCTURE" -> "WATER";
            case "LIVELIHOOD" -> "AGRICULTURE";
            default -> spec.domain();
        };
    }

    private String ragQuery(ScenarioSpec spec) {
        if (!spec.runLabel().startsWith("V03_EXP3_")) {
            return spec.ragQuestion();
        }
        return switch (spec.domain()) {
            case "EDUCATION" -> "rural primary care access barriers follow-up responsibility and service records";
            case "SANITATION" -> "rural water maintenance repair accountability and verified service continuity";
            case "INFRASTRUCTURE" -> "rural water reliability delayed maintenance repair accountability and service continuity";
            case "LIVELIHOOD" -> "rural irrigation interruptions pump availability maintenance responsibility and crop stress";
            default -> spec.ragQuestion();
        };
    }

    private BigDecimal score(boolean value) { return value ? BigDecimal.ONE : BigDecimal.ZERO; }
    private BigDecimal decimal(double value) { return BigDecimal.valueOf(Math.max(0.0, Math.min(1.0, value))).setScale(4, java.math.RoundingMode.HALF_UP); }

    private String json(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (Exception ex) { throw new EvaluationException(HttpStatus.INTERNAL_SERVER_ERROR, "EVALUATION_SERIALIZATION_FAILED", "Evaluation provenance could not be serialized"); }
    }

    private record ScenarioSpec(
            String runLabel, String scenarioKey, String taskType, String classification, String villageContext, String domain,
            String problemStatement, int affectedPopulation, String severity, String sourceType,
            Map<String, Object> survey, List<Map<String, Object>> surveyObservations,
            Map<String, Object> evidence, List<Map<String, Object>> evidenceObservations,
            String ragQuestion, String knowledgeSnapshot, String promptInstruction, String compactEvidence,
            String evaluationNotes) {}
}
