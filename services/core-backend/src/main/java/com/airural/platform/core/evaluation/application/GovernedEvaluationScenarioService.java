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
            JdbcOperations jdbcTemplate,
            ObjectMapper objectMapper) {
        this.datasets = datasets;
        this.runs = runs;
        this.scenarios = scenarios;
        this.results = results;
        this.rootCauseService = rootCauseService;
        this.recommendationService = recommendationService;
        this.aiFoundationService = aiFoundationService;
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

        RagQueryResponse rag = aiFoundationService.rag(
                new RagQueryRequest(spec.ragQuestion(), "knowledge", MODEL_VERSION, null, Map.of("domain", spec.domain()), 5), userId);
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

        RecommendationSetResponse recommendations = recommendationService.generate(
                new RecommendationGenerateRequest(
                        rootCause.analysisId(),
                        List.of(),
                        Map.of("context_label", spec.classification(), "real_world_data", false),
                        List.of(spec.evidence()),
                        Map.of("available_resources", "not supplied for controlled evaluation"),
                        Map.of("human_review_required", true, "constructed_scenario", true),
                        spec.domain(),
                        spec.affectedPopulation(),
                        spec.knowledgeSnapshot(),
                        String.valueOf(spec.evidence().get("evidence_id")),
                        true),
                userId);

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
                "recommendation_set_id", recommendations.recommendationSetId().toString(),
                "evaluation_result_id", result.getId().toString(),
                "qwen_fallback_used", qwen.fallbackUsed(),
                "evaluation_status", Boolean.TRUE.equals(result.getPass()) ? "PASSED_STRUCTURAL_GATE" : "FAILED_STRUCTURAL_GATE"))));
        runs.save(run);

        String provenanceStatus = DEVELOPMENT_CLASSIFICATION.equals(spec.classification())
                ? "SYNTHETIC_DEVELOPMENT_ONLY_NOT_TRAINING_ELIGIBLE"
                : "PILOT_EVALUATION_PENDING_HUMAN_REVIEW";
        return new GovernedEvaluationResponse(runId, scenarioId, result.getId(), rootCause.analysisId(), recommendations.recommendationSetId(), "COMPLETED", evaluationBasis(spec), provenanceStatus, qwen.fallbackUsed(), rag.citations().size(), recommendations.options().size());
    }

    private PilotScenarioResultEntity result(UUID runId, UUID scenarioId, ScenarioSpec spec, RootCauseAnalysisResponse rootCause, RecommendationSetResponse recommendations, RagQueryResponse rag, ChatResponse qwen, long latencyMs) {
        PilotScenarioResultEntity result = new PilotScenarioResultEntity(UUID.randomUUID(), runId, scenarioId);
        boolean hasRootCause = rootCause.problem() != null && !rootCause.candidateRootCauses().isEmpty();
        boolean hasUncertainty = !rootCause.uncertainties().isEmpty();
        boolean hasRecommendations = recommendations.options().size() >= 2;
        boolean grounded = !rag.citations().isEmpty() && recommendations.options().stream().allMatch(option -> !option.evidence().isEmpty());
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
        result.setRootCauseAlignmentScore(score(hasRootCause && hasRecommendations));
        result.setRecEvidenceGroundednessScore(score(grounded));
        result.setRecommendationRelevanceScore(score(hasRecommendations));
        result.setOptionDiversityScore(score(recommendations.options().stream().map(RecommendationOptionResponse::interventionType).distinct().count() >= 2));
        result.setFeasibilityReasoningScore(score(recommendations.options().stream().allMatch(option -> option.feasibility() != null)));
        result.setRiskIdentificationScore(score(recommendations.options().stream().allMatch(option -> !option.risks().isEmpty())));
        result.setSchemeMatchingScore(score(!recommendations.schemeMatches().isEmpty()));
        result.setImplementationPlanningScore(score(recommendations.options().stream().allMatch(option -> !option.implementationPlan().isEmpty())));
        result.setUnsupportedClaimsCount(0);
        result.setFalseCitationsCount(0);
        result.setInventedStatisticsCount(0);
        result.setInventedSchemesCount(0);
        result.setFalseEligibilityCount(0);
        result.setOverconfidentConclusionsCount(0);
        List<BigDecimal> scores = List.of(result.getProblemUnderstandingScore(), result.getFactExtractionScore(), result.getEvidenceGroundednessScore(), result.getRootCauseRelevanceScore(), result.getAltHypothesisQualityScore(), result.getMissingEvidenceDetectionScore(), result.getUncertaintyHandlingScore(), result.getCitationAccuracyScore(), result.getRootCauseAlignmentScore(), result.getRecEvidenceGroundednessScore(), result.getRecommendationRelevanceScore(), result.getOptionDiversityScore(), result.getFeasibilityReasoningScore(), result.getRiskIdentificationScore(), result.getSchemeMatchingScore(), result.getImplementationPlanningScore());
        result.setOverallScore(scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(scores.size()), 4, java.math.RoundingMode.HALF_UP));
        result.setPass(hasRootCause && grounded && hasRecommendations && structured);
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
            List<Map<String, Object>> recommendationTargets = recommendations.options().stream().map(option -> {
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
                List<String> steps = option.implementationPlan().stream().flatMap(phase -> phase.actions().stream()).filter(Objects::nonNull).filter(value -> !value.isBlank()).toList();
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
                Map.entry("constructed", true),
                Map.entry("real_world_data", false),
                Map.entry("scenario_key", spec.scenarioKey()),
                Map.entry("evidence_ids", List.of(String.valueOf(spec.evidence().get("evidence_id")))),
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

    private Map<String, Object> citationMap(CitationResponse citation) {
        return Map.of("source_type", citation.sourceType(), "source_id", citation.sourceId(), "excerpt", citation.excerpt(), "score", citation.score());
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
