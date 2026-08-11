/*
 * Purpose: Implements evidence-grounded recommendation decision support.
 * Why it exists: Validated root causes need transparent intervention options, trade-offs, risks, resources, implementation plans, and human approval.
 * Architecture fit: Application service inside the Decision Intelligence bounded context using existing RAG/Qwen and root-cause analysis boundaries.
 */
package com.airural.platform.core.decision.application;

import com.airural.platform.core.ai.web.dto.AiDtos.*;
import com.airural.platform.core.decision.web.dto.RecommendationIntelligenceDtos.*;
import com.airural.platform.core.decision.web.dto.RootCauseDtos.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Generates and governs recommendation options for human decision support. */
@Service
public class RecommendationIntelligenceService {
    private static final String MODEL_ID = "qwen2.5-local";
    private static final String MODEL_VERSION = "qwen2.5:0.5b";
    private static final String PROMPT_VERSION = "RECOMMENDATION_GENERATION@1.0.0";

    private final JdbcOperations jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final RootCauseIntelligenceService rootCauseService;
    private final RootCauseRagClient ragClient;

    public RecommendationIntelligenceService(JdbcOperations jdbcTemplate, ObjectMapper objectMapper, RootCauseIntelligenceService rootCauseService, RootCauseRagClient ragClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.rootCauseService = rootCauseService;
        this.ragClient = ragClient;
    }

    /** Generates recommendation options linked to validated root causes. */
    @Transactional
    public RecommendationSetResponse generate(RecommendationGenerateRequest request, UUID userId) {
        RootCauseAnalysisResponse rootCauseAnalysis = request.rootCauseAnalysisId() == null ? null : rootCauseService.get(request.rootCauseAnalysisId());
        List<RootCauseInput> rootCauses = rootCauses(request, rootCauseAnalysis);
        if (rootCauses.isEmpty()) {
            throw new DecisionException("VALIDATED_ROOT_CAUSE_REQUIRED", "At least one validated root cause is required to generate recommendations", HttpStatus.BAD_REQUEST);
        }
        String domain = value(request.domain(), rootCauses.get(0).domain() == null ? "OTHER" : rootCauses.get(0).domain()).toUpperCase(Locale.ROOT);
        RagQueryResponse rag = ragClient.rag(new RagQueryRequest(ragQuestion(rootCauses, request), "knowledge", MODEL_ID, null, Map.of("domain", domain), 5), userId);
        List<RecommendationOptionResponse> options = options(rootCauses, request, rag, domain);
        List<OptionComparisonResponse> comparison = compare(options, request);
        List<SchemeMatchResponse> schemes = schemeMatches(options, rag, request);
        UUID setId = UUID.randomUUID();
        RecommendationSetResponse response = new RecommendationSetResponse(
                setId,
                request.rootCauseAnalysisId(),
                "AI_GENERATED",
                options,
                comparison,
                schemes,
                methodology(),
                MODEL_ID,
                MODEL_VERSION,
                PROMPT_VERSION,
                value(request.knowledgeSnapshot(), "rag-service:latest"),
                value(request.evidenceSnapshot(), "current-evidence"),
                Instant.now());
        persist(response, request, userId, Boolean.TRUE.equals(request.requireHumanApproval()));
        return response;
    }

    /** Gets one generated recommendation set. */
    @Transactional(readOnly = true)
    public RecommendationSetResponse get(UUID id) {
        Map<String, Object> snapshot;
        try {
            snapshot = jdbcTemplate.queryForMap("select response_json::text as response_json, status from decision.recommendation_sets where id = ?", id);
        } catch (org.springframework.dao.EmptyResultDataAccessException ex) {
            snapshot = null;
        }
        if (snapshot == null) {
            throw new DecisionException("RECOMMENDATION_NOT_FOUND", "Recommendation set was not found", HttpStatus.NOT_FOUND);
        }
        return withStatus(read(String.valueOf(snapshot.get("response_json")), RecommendationSetResponse.class), String.valueOf(snapshot.get("status")));
    }

    /** Gets generated options for comparison. */
    @Transactional(readOnly = true)
    public Map<String, Object> options(UUID id) {
        RecommendationSetResponse response = get(id);
        return Map.of("recommendationSetId", id, "options", response.options(), "comparison", response.comparison());
    }

    /** Gets evidence and scheme matches. */
    @Transactional(readOnly = true)
    public Map<String, Object> evidence(UUID id) {
        RecommendationSetResponse response = get(id);
        return Map.of("recommendationSetId", id, "optionEvidence", response.options().stream().map(option -> Map.of("recommendationId", option.recommendationId(), "evidence", option.evidence())).toList(), "schemeMatches", response.schemeMatches());
    }

    /** Gets option risk records. */
    @Transactional(readOnly = true)
    public Map<String, Object> risks(UUID id) {
        RecommendationSetResponse response = get(id);
        return Map.of("recommendationSetId", id, "risks", response.options().stream().map(option -> Map.of("recommendationId", option.recommendationId(), "risks", option.risks())).toList());
    }

    /** Records human recommendation review. */
    @Transactional
    public RecommendationReviewResponse review(UUID id, RecommendationReviewRequest request, UUID userId) {
        RecommendationSetResponse current = get(id);
        String action = request.action().toUpperCase(Locale.ROOT);
        if (!Set.of("ACCEPT", "EDIT", "REJECT", "REQUEST_MORE_EVIDENCE", "APPROVE").contains(action)) {
            throw new DecisionException("INVALID_RECOMMENDATION_REVIEW_ACTION", "Unsupported recommendation review action", HttpStatus.BAD_REQUEST);
        }
        if (Set.of("APPROVED", "REJECTED", "SUPERSEDED").contains(current.status())) {
            throw new DecisionException("RECOMMENDATION_REVIEW_NOT_ALLOWED", "Recommendation set cannot be reviewed from status " + current.status(), HttpStatus.CONFLICT);
        }
        RecommendationSetResponse reviewedResponse = current;
        if ("EDIT".equals(action)) {
            reviewedResponse = applyEdit(current, request.modifiedRecommendation());
        }
        String nextStatus = switch (action) {
            case "REJECT" -> "REJECTED";
            case "REQUEST_MORE_EVIDENCE" -> "MORE_EVIDENCE_REQUESTED";
            case "APPROVE" -> "APPROVED";
            case "ACCEPT", "EDIT" -> "UNDER_REVIEW";
            default -> "UNDER_REVIEW";
        };
        reviewedResponse = withStatus(reviewedResponse, nextStatus);
        UUID reviewId = UUID.randomUUID();
        jdbcTemplate.update(
                "insert into decision.recommendation_reviews (id, recommendation_set_id, reviewer_user_id, action, reviewer_notes, modified_recommendation_json, correction) values (?, ?, ?, ?, ?, ?::jsonb, ?)",
                reviewId, id, userId, action, request.reviewerNotes(), json(request.modifiedRecommendation()), request.correction());
        jdbcTemplate.update("update decision.recommendation_sets set status = ?, response_json = ?::jsonb, updated_at = now() where id = ?",
                nextStatus, json(reviewedResponse), id);
        if ("EDIT".equals(action)) {
            jdbcTemplate.update(
                    "insert into decision.recommendation_versions (recommendation_set_id, version_number, root_cause_analysis_version, response_json, model, model_version, prompt_version, knowledge_snapshot, evidence_snapshot, reviewer_user_id) select ?, coalesce(max(version_number), 0) + 1, ?, ?::jsonb, ?, ?, ?, ?, ?, ? from decision.recommendation_versions where recommendation_set_id = ?",
                    id, "human-edit", json(reviewedResponse), current.model(), current.modelVersion(), current.promptVersion(), current.knowledgeSnapshot(), current.evidenceSnapshot(), userId, id);
        }
        return new RecommendationReviewResponse(reviewId, id, action, nextStatus, Instant.now());
    }

    /** Approves recommendation set for implementation tracking. */
    @Transactional
    public RecommendationReviewResponse approve(UUID id, RecommendationReviewRequest request, UUID userId) {
        RecommendationReviewResponse review = review(id, new RecommendationReviewRequest("APPROVE", request == null ? null : request.reviewerNotes(), request == null ? null : request.modifiedRecommendation(), request == null ? null : request.correction()), userId);
        return review;
    }

    /** Rejects recommendation set. */
    @Transactional
    public RecommendationReviewResponse reject(UUID id, RecommendationReviewRequest request, UUID userId) {
        RecommendationReviewResponse review = review(id, new RecommendationReviewRequest("REJECT", request == null ? null : request.reviewerNotes(), request == null ? null : request.modifiedRecommendation(), request == null ? null : request.correction()), userId);
        return review;
    }

    /** Regenerates recommendations from the stored request context. */
    @Transactional
    public RecommendationSetResponse regenerate(UUID id, UUID userId) {
        RecommendationSetResponse previous = get(id);
        jdbcTemplate.update("update decision.recommendation_sets set status = 'SUPERSEDED', updated_at = now() where id = ?", id);
        RecommendationGenerateRequest request = new RecommendationGenerateRequest(previous.rootCauseAnalysisId(), previous.options().stream().map(option -> new RootCauseInput(option.recommendationId(), option.targetRootCause(), option.domain(), option.confidence().evidenceStrength(), option.evidence())).toList(), Map.of(), List.of(), Map.of(), Map.of(), previous.options().isEmpty() ? "OTHER" : previous.options().get(0).domain(), previous.options().isEmpty() ? null : previous.options().get(0).targetPopulation(), previous.knowledgeSnapshot(), previous.evidenceSnapshot(), true);
        return generate(request, userId);
    }

    private List<RootCauseInput> rootCauses(RecommendationGenerateRequest request, RootCauseAnalysisResponse rootCauseAnalysis) {
        if (rootCauseAnalysis != null && !rootCauseAnalysis.validatedRootCauses().isEmpty()) {
            return rootCauseAnalysis.validatedRootCauses().stream()
                    .map(cause -> new RootCauseInput(cause.rootCauseId(), cause.description(), cause.affectedDomain(), cause.confidence(), cause.supportingEvidence()))
                    .toList();
        }
        return request.validatedRootCauses() == null ? List.of() : request.validatedRootCauses();
    }

    private List<RecommendationOptionResponse> options(List<RootCauseInput> rootCauses, RecommendationGenerateRequest request, RagQueryResponse rag, String domain) {
        List<RecommendationOptionResponse> options = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(1);
        for (RootCauseInput cause : rootCauses) {
            for (String intervention : interventions(domain, cause.description()).stream().limit(3).toList()) {
                String id = "recommendation-" + counter.getAndIncrement();
                List<String> evidence = new ArrayList<>();
                if (cause.evidence() != null) evidence.addAll(cause.evidence());
                rag.citations().stream().map(CitationResponse::sourceId).limit(3).forEach(evidence::add);
                boolean resourceMissing = request.availableResources() == null || request.availableResources().isEmpty();
                ConfidenceBreakdownResponse confidence = confidence(cause, evidence, resourceMissing);
                RecommendationOptionResponse option = new RecommendationOptionResponse(
                        id,
                        title(intervention, domain),
                        intervention + " to address: " + cause.description(),
                        cause.description(),
                        request.targetPopulation(),
                        domain,
                        interventionType(domain, intervention),
                        0,
                        expectedOutcomes(domain, intervention),
                        resources(intervention, resourceMissing),
                        effort(intervention),
                        timeframe(intervention),
                        feasibility(confidence, request.constraints(), resourceMissing),
                        risks(domain, intervention, resourceMissing),
                        dependencies(resourceMissing),
                        evidence.isEmpty() ? List.of("INSUFFICIENT_EVIDENCE") : evidence.stream().distinct().toList(),
                        confidence,
                        assumptions(resourceMissing),
                        limitations(rag, resourceMissing),
                        plan(intervention, domain),
                        metrics(domain),
                        "AI_GENERATED");
                options.add(option);
            }
        }
        return prioritize(options);
    }

    private List<RecommendationOptionResponse> prioritize(List<RecommendationOptionResponse> options) {
        List<OptionComparisonResponse> comparison = compare(options, null);
        Map<String, Integer> priorities = new HashMap<>();
        for (int i = 0; i < comparison.size(); i++) priorities.put(comparison.get(i).recommendationId(), i + 1);
        return options.stream().map(option -> copyWithPriority(option, priorities.getOrDefault(option.recommendationId(), 99))).toList();
    }

    private List<OptionComparisonResponse> compare(List<RecommendationOptionResponse> options, RecommendationGenerateRequest request) {
        Map<String, Double> weights = Map.of("impact", 0.22, "urgency", 0.16, "feasibility", 0.18, "evidenceStrength", 0.18, "resources", 0.10, "affectedPopulation", 0.08, "risk", 0.05, "dependency", 0.03);
        return options.stream().map(option -> {
            double resourceScore = option.feasibility().resourceStatus().equals("RESOURCE_INFORMATION_MISSING") ? 0.35 : 0.70;
            double populationScore = option.targetPopulation() == null ? 0.45 : Math.min(0.85, option.targetPopulation() / 300.0);
            double riskScore = option.risks().stream().anyMatch(risk -> risk.severity().equals("HIGH")) ? 0.42 : 0.70;
            double priority = round((0.76 * weights.get("impact")) + (0.70 * weights.get("urgency")) + (option.confidence().implementationFeasibility() * weights.get("feasibility")) + (option.confidence().evidenceStrength() * weights.get("evidenceStrength")) + (resourceScore * weights.get("resources")) + (populationScore * weights.get("affectedPopulation")) + (riskScore * weights.get("risk")) + (0.62 * weights.get("dependency")));
            return new OptionComparisonResponse(option.recommendationId(), priority, advantages(option), disadvantages(option), option.estimatedEffort(), option.feasibility().rating(), unintended(option), weights);
        }).sorted(Comparator.comparing(OptionComparisonResponse::priorityScore).reversed()).toList();
    }

    private List<SchemeMatchResponse> schemeMatches(List<RecommendationOptionResponse> options, RagQueryResponse rag, RecommendationGenerateRequest request) {
        if (rag.citations().isEmpty()) {
            return List.of(new SchemeMatchResponse("No verified scheme identified", "RAG", List.of(), "Unknown", "Unknown", List.of("ELIGIBILITY_REQUIRES_VERIFICATION", "No verified scheme citation was retrieved."), value(request.knowledgeSnapshot(), "unknown"), "ELIGIBILITY_REQUIRES_VERIFICATION"));
        }
        return options.stream().filter(option -> option.interventionType().contains("Government") || option.description().toLowerCase(Locale.ROOT).contains("scheme") || option.domain().equals("WATER") || option.domain().equals("AGRICULTURE")).limit(3)
                .map(option -> new SchemeMatchResponse("Scheme match requires verification for " + option.interventionType(), rag.citations().get(0).sourceId(), option.evidence().stream().limit(3).toList(), option.targetPopulation() == null ? "Applicable population unknown" : option.targetPopulation() + " affected residents, subject to eligibility verification", "Potential government-program linkage from verified knowledge context", List.of("ELIGIBILITY_REQUIRES_VERIFICATION", "Do not claim eligibility until required conditions are confirmed."), value(request.knowledgeSnapshot(), "rag-service:latest"), "ELIGIBILITY_REQUIRES_VERIFICATION"))
                .toList();
    }

    private void persist(RecommendationSetResponse response, RecommendationGenerateRequest request, UUID userId, boolean humanApprovalRequired) {
        jdbcTemplate.update("insert into decision.recommendation_sets (id, root_cause_analysis_id, requested_by, request_json, response_json, model, model_version, prompt_version, knowledge_snapshot, evidence_snapshot, human_approval_required) values (?, ?, ?, ?::jsonb, ?::jsonb, ?, ?, ?, ?, ?, ?)", response.recommendationSetId(), response.rootCauseAnalysisId(), userId, json(request), json(response), response.model(), response.modelVersion(), response.promptVersion(), response.knowledgeSnapshot(), response.evidenceSnapshot(), humanApprovalRequired);
        jdbcTemplate.update("insert into decision.recommendation_versions (recommendation_set_id, version_number, root_cause_analysis_version, response_json, model, model_version, prompt_version, knowledge_snapshot, evidence_snapshot, reviewer_user_id) values (?, 1, ?, ?::jsonb, ?, ?, ?, ?, ?, ?)", response.recommendationSetId(), "current", json(response), response.model(), response.modelVersion(), response.promptVersion(), response.knowledgeSnapshot(), response.evidenceSnapshot(), userId);
        response.options().forEach(option -> persistOption(response.recommendationSetId(), option));
        response.schemeMatches().forEach(scheme -> jdbcTemplate.update("insert into decision.recommendation_scheme_matches (recommendation_set_id, scheme_name, source, eligibility_evidence_json, applicable_population, relevant_benefit, limitations_json, source_date_version, status) values (?, ?, ?, ?::jsonb, ?, ?, ?::jsonb, ?, ?)", response.recommendationSetId(), scheme.schemeName(), scheme.source(), json(scheme.eligibilityEvidence()), scheme.applicablePopulation(), scheme.relevantBenefit(), json(scheme.limitations()), scheme.sourceDateVersion(), scheme.status()));
    }

    private void persistOption(UUID setId, RecommendationOptionResponse option) {
        jdbcTemplate.update("insert into decision.recommendation_options (recommendation_set_id, recommendation_id, title, description, target_root_cause, target_population, domain, intervention_type, priority, expected_outcomes_json, required_resources_json, estimated_effort, estimated_timeframe, feasibility_json, dependencies_json, assumptions_json, limitations_json, confidence_json, status) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?, ?, ?::jsonb, ?::jsonb, ?::jsonb, ?::jsonb, ?::jsonb, ?)", setId, option.recommendationId(), option.title(), option.description(), option.targetRootCause(), option.targetPopulation(), option.domain(), option.interventionType(), option.priority(), json(option.expectedOutcomes()), json(option.requiredResources()), option.estimatedEffort(), option.estimatedTimeframe(), json(option.feasibility()), json(option.dependencies()), json(option.assumptions()), json(option.limitations()), json(option.confidence()), option.status());
        option.evidence().forEach(evidence -> jdbcTemplate.update("insert into decision.recommendation_evidence_links (recommendation_set_id, recommendation_id, evidence_ref, evidence_type, grounding_type, confidence) values (?, ?, ?, ?, ?, ?)", setId, option.recommendationId(), evidence, evidence.equals("INSUFFICIENT_EVIDENCE") ? "MISSING" : "SUPPORTING", "EVIDENCE_BACKED_RECOMMENDATION", option.confidence().evidenceStrength()));
        option.risks().forEach(risk -> jdbcTemplate.update("insert into decision.recommendation_risks (recommendation_set_id, recommendation_id, risk_type, description, severity, likelihood, mitigation, evidence_or_assumption) values (?, ?, ?, ?, ?, ?, ?, ?)", setId, option.recommendationId(), risk.riskType(), risk.description(), risk.severity(), risk.likelihood(), risk.mitigation(), risk.evidenceOrAssumption()));
        option.requiredResources().forEach(resource -> jdbcTemplate.update("insert into decision.recommendation_resources (recommendation_set_id, recommendation_id, resource_name, resource_status, evidence_or_gap) values (?, ?, ?, ?, ?)", setId, option.recommendationId(), resource, resource.contains("RESOURCE_INFORMATION_MISSING") ? "RESOURCE_INFORMATION_MISSING" : "REQUIRED", resource));
        option.successIndicators().forEach(metric -> jdbcTemplate.update("insert into decision.recommendation_metrics (recommendation_set_id, recommendation_id, metric_name, baseline, target, measurement_method, measurement_frequency, data_gap) values (?, ?, ?, ?, ?, ?, ?, ?)", setId, option.recommendationId(), metric.name(), metric.baseline(), metric.target(), metric.measurementMethod(), metric.measurementFrequency(), metric.dataGap()));
        option.implementationPlan().forEach(phase -> jdbcTemplate.update("insert into decision.implementation_plans (recommendation_set_id, recommendation_id, phase, actions_json, responsible_role, required_inputs_json, dependencies_json, success_indicators_json) values (?, ?, ?, ?::jsonb, ?, ?::jsonb, ?::jsonb, ?::jsonb)", setId, option.recommendationId(), phase.phase(), json(phase.actions()), phase.responsibleRole(), json(phase.requiredInputs()), json(phase.dependencies()), json(phase.successIndicators())));
    }

    private String ragQuestion(List<RootCauseInput> rootCauses, RecommendationGenerateRequest request) {
        return "Find trusted government or domain knowledge, scheme considerations, implementation risks, and evidence limits for interventions addressing: " + rootCauses.stream().map(RootCauseInput::description).toList() + ". Village context: " + request.villageContext();
    }

    private List<String> interventions(String domain, String cause) {
        String lower = (domain + " " + cause).toLowerCase(Locale.ROOT);
        if (lower.contains("water")) return List.of("Repair and maintenance accountability workflow", "Water source reliability monitoring", "Government scheme facilitation for water infrastructure", "Community water-use reporting");
        if (lower.contains("agri") || lower.contains("crop") || lower.contains("irrigation")) return List.of("Irrigation access improvement", "Soil and crop advisory support", "Market-access support", "Input-cost planning support");
        if (lower.contains("education") || lower.contains("school")) return List.of("Attendance follow-up outreach", "School access barrier reduction", "Household support referral", "Teacher availability monitoring");
        if (lower.contains("employment")) return List.of("Skill development referral", "Local employer linkage", "Government employment scheme facilitation", "Market-oriented training support");
        return List.of("Administrative coordination", "Community outreach", "Service access monitoring", "Government scheme facilitation");
    }

    private ConfidenceBreakdownResponse confidence(RootCauseInput cause, List<String> evidence, boolean resourceMissing) {
        double evidenceStrength = evidence.isEmpty() ? 0.22 : Math.min(0.84, 0.32 + evidence.size() * 0.10);
        double recommendationConfidence = round((clamp(cause.confidence()) * 0.45) + (evidenceStrength * 0.55));
        double feasibility = resourceMissing ? 0.38 : 0.66;
        return new ConfidenceBreakdownResponse(round(evidenceStrength), recommendationConfidence, feasibility, "Evidence strength, recommendation confidence, and implementation feasibility are separate decision-support metrics.");
    }

    private FeasibilityResponse feasibility(ConfidenceBreakdownResponse confidence, Map<String, Object> constraints, boolean resourceMissing) {
        List<String> constraintList = constraints == null || constraints.isEmpty() ? List.of("No explicit constraints supplied") : constraints.entrySet().stream().map(entry -> entry.getKey() + ": " + entry.getValue()).toList();
        String rating = confidence.implementationFeasibility() >= 0.65 ? "MODERATE" : "REQUIRES_RESOURCE_VERIFICATION";
        return new FeasibilityResponse(rating, List.of("Root cause is linked to proposed intervention", "Human approval workflow is required"), constraintList, resourceMissing ? "RESOURCE_INFORMATION_MISSING" : "RESOURCE_INFORMATION_PROVIDED");
    }

    private List<RiskResponse> risks(String domain, String intervention, boolean resourceMissing) {
        List<RiskResponse> risks = new ArrayList<>();
        risks.add(new RiskResponse("Implementation Risk", "Intervention may be delayed if responsible roles and inputs are not confirmed.", "MEDIUM", "POSSIBLE", "Confirm ownership during preparation phase.", "Implementation planning assumption"));
        risks.add(new RiskResponse("Adoption Risk", "Households or local stakeholders may not adopt the intervention without outreach.", "MEDIUM", "POSSIBLE", "Include community communication and feedback loops.", "Common rural rollout risk"));
        if (resourceMissing) risks.add(new RiskResponse("Data Risk", "Budget, personnel, or infrastructure availability is unknown.", "HIGH", "LIKELY", "Collect resource inventory before approval.", "RESOURCE_INFORMATION_MISSING"));
        if (domain.equals("HEALTHCARE")) risks.add(new RiskResponse("Policy Risk", "Recommendations must not be treated as medical diagnosis.", "HIGH", "POSSIBLE", "Require qualified health review.", "Safety policy"));
        return risks;
    }

    private List<ImplementationPhaseResponse> plan(String intervention, String domain) {
        return List.of(
                new ImplementationPhaseResponse("Phase 1 - Preparation", List.of("Validate evidence", "Confirm resource availability", "Assign accountable role"), "Program coordinator", List.of("Reviewed root-cause analysis", "Resource inventory"), List.of("Human approval"), List.of("Evidence reviewed", "Resource gaps documented")),
                new ImplementationPhaseResponse("Phase 2 - Implementation", List.of("Execute approved intervention tasks", "Track blockers", "Record field updates"), "Field implementation team", List.of("Approved recommendation", "Inputs from preparation"), List.of("Confirmed resources"), List.of("Implementation started", "Blockers logged")),
                new ImplementationPhaseResponse("Phase 3 - Monitoring", List.of("Measure success indicators", "Collect beneficiary feedback", "Check unintended consequences"), "Monitoring officer", List.of("Indicator definitions", "Field reports"), List.of("Implementation progress"), List.of("Monitoring records available")),
                new ImplementationPhaseResponse("Phase 4 - Evaluation", List.of("Compare outcomes against baseline where available", "Review evidence gaps", "Decide continuation or revision"), "Review board", List.of("Monitoring data", "Human review notes"), List.of("Completed monitoring"), List.of("Evaluation decision recorded")));
    }

    private List<MetricResponse> metrics(String domain) {
        if (domain.equals("WATER")) return List.of(new MetricResponse("Water availability", "BASELINE_DATA_REQUIRED", "TARGET_REQUIRES_HUMAN_APPROVAL", "Household survey and service records", "Monthly", "Baseline water availability not supplied"), new MetricResponse("Repair turnaround", "BASELINE_DATA_REQUIRED", "TARGET_REQUIRES_HUMAN_APPROVAL", "Maintenance logs", "Monthly", "Repair baseline not supplied"));
        if (domain.equals("AGRICULTURE")) return List.of(new MetricResponse("Irrigation access", "BASELINE_DATA_REQUIRED", "TARGET_REQUIRES_HUMAN_APPROVAL", "Household survey", "Seasonal", "Irrigation baseline not supplied"));
        return List.of(new MetricResponse("Households reached", "BASELINE_DATA_REQUIRED", "TARGET_REQUIRES_HUMAN_APPROVAL", "Field report", "Monthly", "Baseline not supplied"));
    }

    private List<String> resources(String intervention, boolean resourceMissing) {
        List<String> resources = new ArrayList<>(List.of("Human reviewer approval", "Field verification records", "Monitoring capacity"));
        if (resourceMissing) resources.add("RESOURCE_INFORMATION_MISSING: budget/personnel/infrastructure availability must be verified");
        else resources.add("Provided local resource context");
        return resources;
    }

    private List<String> expectedOutcomes(String domain, String intervention) {
        return List.of("Improved response to validated root cause", "Reduced uncertainty through monitoring", "Decision makers receive evidence-grounded intervention option");
    }

    private List<String> dependencies(boolean resourceMissing) {
        List<String> deps = new ArrayList<>(List.of("Human approval", "Field evidence validation"));
        if (resourceMissing) deps.add("Resource inventory collection");
        return deps;
    }

    private List<String> assumptions(boolean resourceMissing) {
        return resourceMissing ? List.of("Resource availability is unknown and must be verified before approval.") : List.of("Provided resources are available subject to human validation.");
    }

    private List<String> limitations(RagQueryResponse rag, boolean resourceMissing) {
        List<String> limitations = new ArrayList<>(List.of("Recommendation is decision support and does not execute intervention automatically.", "Outcomes are expected, not guaranteed."));
        if (rag.citations().isEmpty()) limitations.add("No verified RAG citation was available.");
        if (resourceMissing) limitations.add("RESOURCE_INFORMATION_MISSING");
        return limitations;
    }

    private List<String> advantages(RecommendationOptionResponse option) {
        return List.of("Directly linked to validated root cause", "Has explicit evidence and review requirements");
    }

    private List<String> disadvantages(RecommendationOptionResponse option) {
        return option.feasibility().resourceStatus().equals("RESOURCE_INFORMATION_MISSING") ? List.of("Resource availability is not confirmed", "Requires additional verification before approval") : List.of("Requires coordination and monitoring capacity");
    }

    private List<String> unintended(RecommendationOptionResponse option) {
        return List.of("May divert attention from alternative root causes if reviewed in isolation", "May underperform if local adoption barriers are missed");
    }

    private String title(String intervention, String domain) {
        return intervention + " (" + domain.toLowerCase(Locale.ROOT) + ")";
    }

    private String interventionType(String domain, String intervention) {
        String lower = intervention.toLowerCase(Locale.ROOT);
        if (lower.contains("scheme")) return "Government Schemes";
        if (lower.contains("repair") || lower.contains("maintenance") || lower.contains("monitoring")) return "Service Operations";
        if (lower.contains("irrigation") || lower.contains("infrastructure") || lower.contains("access improvement")) return "Infrastructure Access";
        if (lower.contains("soil") || lower.contains("crop") || lower.contains("input")) return "Technical Advisory";
        if (lower.contains("market") || lower.contains("employer")) return "Market Linkage";
        if (lower.contains("referral") || lower.contains("medicine") || lower.contains("health")) return "Health Service Coordination";
        if (lower.contains("outreach") || lower.contains("community")) return "Community Engagement";
        if (domain.equals("WATER")) return "Water Management";
        if (domain.equals("AGRICULTURE")) return "Agriculture";
        if (domain.equals("EDUCATION")) return "Education";
        if (domain.equals("EMPLOYMENT")) return "Skill Development";
        return "Policy/Administrative Action";
    }

    private String effort(String intervention) {
        return intervention.toLowerCase(Locale.ROOT).contains("infrastructure") ? "HIGH" : "MEDIUM";
    }

    private String timeframe(String intervention) {
        return intervention.toLowerCase(Locale.ROOT).contains("monitoring") ? "SHORT_TO_MEDIUM_TERM" : "MEDIUM_TERM";
    }

    private RecommendationOptionResponse copyWithPriority(RecommendationOptionResponse option, int priority) {
        return new RecommendationOptionResponse(option.recommendationId(), option.title(), option.description(), option.targetRootCause(), option.targetPopulation(), option.domain(), option.interventionType(), priority, option.expectedOutcomes(), option.requiredResources(), option.estimatedEffort(), option.estimatedTimeframe(), option.feasibility(), option.risks(), option.dependencies(), option.evidence(), option.confidence(), option.assumptions(), option.limitations(), option.implementationPlan(), option.successIndicators(), option.status());
    }

    private List<String> methodology() {
        return List.of("Options are generated only from validated root causes.", "Priority score is a weighted decision-support score, not an objective best decision.", "Scheme matches require verified eligibility evidence before use.", "Human approval is required before implementation.");
    }

    private double clamp(Double value) {
        return value == null ? 0.0 : Math.max(0.0, Math.min(1.0, value));
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    private String value(String text, String fallback) {
        return text == null || text.isBlank() ? fallback : text;
    }

    private String json(Object value) {
        try { return objectMapper.writeValueAsString(value); } catch (Exception ex) { return "{}"; }
    }

    private RecommendationSetResponse applyEdit(RecommendationSetResponse current, Map<String, Object> modifiedRecommendation) {
        if (modifiedRecommendation == null || modifiedRecommendation.isEmpty() || !modifiedRecommendation.containsKey("options")) {
            throw new DecisionException("EDITED_RECOMMENDATION_REQUIRED", "An EDIT review must include an options array", HttpStatus.BAD_REQUEST);
        }
        try {
            List<RecommendationOptionResponse> editedOptions = objectMapper.convertValue(
                    modifiedRecommendation.get("options"), new TypeReference<List<RecommendationOptionResponse>>() {});
            if (editedOptions == null || editedOptions.isEmpty() || editedOptions.stream().anyMatch(option -> option == null || option.recommendationId() == null || option.recommendationId().isBlank())) {
                throw new DecisionException("EDITED_RECOMMENDATION_INVALID", "Edited recommendation options must contain at least one identified option", HttpStatus.BAD_REQUEST);
            }
            Set<String> ids = new HashSet<>();
            if (editedOptions.stream().anyMatch(option -> !ids.add(option.recommendationId()))) {
                throw new DecisionException("EDITED_RECOMMENDATION_INVALID", "Edited recommendation option identifiers must be unique", HttpStatus.BAD_REQUEST);
            }
            return new RecommendationSetResponse(current.recommendationSetId(), current.rootCauseAnalysisId(), current.status(), prioritize(editedOptions), current.comparison(), current.schemeMatches(), current.methodology(), current.model(), current.modelVersion(), current.promptVersion(), current.knowledgeSnapshot(), current.evidenceSnapshot(), current.createdAt());
        } catch (DecisionException ex) {
            throw ex;
        } catch (IllegalArgumentException ex) {
            throw new DecisionException("EDITED_RECOMMENDATION_INVALID", "Edited recommendation options do not match the recommendation contract", HttpStatus.BAD_REQUEST);
        }
    }

    private RecommendationSetResponse withStatus(RecommendationSetResponse response, String status) {
        return new RecommendationSetResponse(response.recommendationSetId(), response.rootCauseAnalysisId(), status, response.options(), response.comparison(), response.schemeMatches(), response.methodology(), response.model(), response.modelVersion(), response.promptVersion(), response.knowledgeSnapshot(), response.evidenceSnapshot(), response.createdAt());
    }

    private <T> T read(String json, Class<T> type) {
        try { return objectMapper.readValue(json, type); } catch (Exception ex) { throw new DecisionException("RECOMMENDATION_JSON_INVALID", "Stored recommendation could not be read", HttpStatus.INTERNAL_SERVER_ERROR); }
    }

    @SuppressWarnings("unused")
    private Map<String, Object> map(String json) {
        try { return objectMapper.readValue(json, new TypeReference<>() {}); } catch (Exception ex) { return Map.of(); }
    }
}
