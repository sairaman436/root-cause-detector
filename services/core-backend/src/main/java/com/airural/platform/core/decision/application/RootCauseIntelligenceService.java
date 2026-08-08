/*
 * Purpose: Implements transparent, evidence-grounded root-cause intelligence.
 * Why it exists: Rural decision support needs strict fact/inference separation, causal graph reasoning, confidence scoring, and human review.
 * Architecture fit: Application service inside the Decision Intelligence bounded context using existing RAG/Qwen integration.
 */
package com.airural.platform.core.decision.application;

import com.airural.platform.core.ai.web.dto.AiDtos.*;
import com.airural.platform.core.decision.web.dto.RootCauseDtos.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Root-cause engine that transforms rural data, evidence, and RAG knowledge into reviewable causal analysis. */
@Service
public class RootCauseIntelligenceService {
    private static final String MODEL_ID = "qwen2.5-local";
    private static final String MODEL_VERSION = "qwen2.5:0.5b";
    private static final String PROMPT_VERSION = "ROOT_CAUSE_INTELLIGENCE@1.0.0";
    private static final List<String> DOMAINS = List.of("AGRICULTURE", "WATER", "HEALTHCARE", "EDUCATION", "EMPLOYMENT", "INFRASTRUCTURE", "NUTRITION", "LIVELIHOOD", "CLIMATE", "GOVERNANCE", "SANITATION", "OTHER");

    private final JdbcOperations jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final RootCauseRagClient ragClient;

    public RootCauseIntelligenceService(JdbcOperations jdbcTemplate, ObjectMapper objectMapper, RootCauseRagClient ragClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.ragClient = ragClient;
    }

    /** Generates and persists a new structured root-cause analysis. */
    @Transactional
    public RootCauseAnalysisResponse analyze(RootCauseAnalysisRequest request, UUID userId) {
        ProblemResponse problem = normalizeProblem(request.problem());
        List<FactResponse> observedFacts = extractFacts(request, problem);
        RagQueryResponse rag = ragClient.rag(new RagQueryRequest(ragQuestion(problem), "knowledge", MODEL_ID, null, Map.of("domain", problem.domain()), 5), userId);
        List<FactResponse> retrievedFacts = retrievedEvidenceFacts(rag);
        List<FactResponse> allFacts = new ArrayList<>();
        allFacts.addAll(observedFacts);
        allFacts.addAll(retrievedFacts);
        List<EvidenceAssessmentResponse> assessments = assessEvidence(allFacts, problem);
        List<FactorResponse> factors = factors(problem, allFacts, assessments);
        List<String> contradictions = contradictions(allFacts);
        List<CandidateRootCauseResponse> candidates = candidates(problem, factors, allFacts, contradictions, rag);
        List<CandidateRootCauseResponse> validated = candidates.stream().filter(candidate -> candidate.confidence() >= 0.55 && candidate.contradictingEvidence().isEmpty()).toList();
        List<AlternativeHypothesisResponse> alternatives = alternatives(problem, candidates, allFacts);
        List<CausalRelationshipResponse> graph = graph(problem, factors, candidates);
        List<UncertaintyResponse> uncertainties = uncertainties(problem, candidates, contradictions, allFacts);
        List<String> followUps = uncertainties.stream().flatMap(item -> item.followUpQuestions().stream()).distinct().toList();
        ConfidenceResponse confidence = confidence(assessments, contradictions, validated, uncertainties);
        List<String> limitations = limitations(confidence, rag, contradictions);

        UUID analysisId = UUID.randomUUID();
        RootCauseAnalysisResponse response = new RootCauseAnalysisResponse(
                analysisId,
                1,
                problem,
                allFacts,
                factors,
                candidates,
                validated,
                alternatives,
                assessments,
                uncertainties,
                confidence,
                limitations,
                followUps,
                graph,
                MODEL_ID,
                MODEL_VERSION,
                PROMPT_VERSION,
                value(request.knowledgeSnapshot(), "rag-service:latest"),
                value(request.surveyVersion(), "unversioned"),
                Instant.now());
        validate(response);
        persist(response, request, userId, Boolean.TRUE.equals(request.requireHumanReview()) || confidence.overall() < 0.75);
        return response;
    }

    /** Gets a previously generated analysis by ID. */
    @Transactional(readOnly = true)
    public RootCauseAnalysisResponse get(UUID analysisId) {
        String json = jdbcTemplate.query(
                "select analysis_json::text from decision.root_cause_analyses where id = ?",
                ps -> ps.setObject(1, analysisId),
                rs -> rs.next() ? rs.getString(1) : null);
        if (json == null) {
            throw new DecisionException("ROOT_CAUSE_ANALYSIS_NOT_FOUND", "Root-cause analysis was not found", HttpStatus.NOT_FOUND);
        }
        return read(json, RootCauseAnalysisResponse.class);
    }

    /** Gets evidence records for an analysis. */
    @Transactional(readOnly = true)
    public Map<String, Object> evidence(UUID analysisId) {
        RootCauseAnalysisResponse analysis = get(analysisId);
        return Map.of("analysisId", analysisId, "evidence", analysis.evidence(), "observedFacts", analysis.observedFacts());
    }

    /** Gets causal graph records for an analysis. */
    @Transactional(readOnly = true)
    public Map<String, Object> causalGraph(UUID analysisId) {
        RootCauseAnalysisResponse analysis = get(analysisId);
        return Map.of("analysisId", analysisId, "nodes", graphNodes(analysis), "edges", analysis.causalGraph());
    }

    /** Records human validation without overwriting the generated analysis. */
    @Transactional
    public HumanReviewResponse review(UUID analysisId, HumanReviewRequest request, UUID userId) {
        get(analysisId);
        String action = request.action().toUpperCase(Locale.ROOT);
        if (!Set.of("ACCEPT", "REJECT", "MODIFY", "ADD_EVIDENCE", "FLAG_INCORRECT_REASONING").contains(action)) {
            throw new DecisionException("INVALID_REVIEW_ACTION", "Unsupported human review action", HttpStatus.BAD_REQUEST);
        }
        UUID reviewId = UUID.randomUUID();
        jdbcTemplate.update(
                "insert into decision.root_cause_human_reviews (id, analysis_id, reviewer_user_id, action, reviewer_notes, modified_analysis_json, additional_evidence_json, correction) values (?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?)",
                reviewId,
                analysisId,
                userId,
                action,
                request.reviewerNotes(),
                json(request.modifiedAnalysis()),
                json(request.additionalEvidence() == null ? List.of() : request.additionalEvidence()),
                request.correction());
        jdbcTemplate.update("update decision.root_cause_analyses set status = ?, updated_at = now() where id = ?", action.equals("REJECT") ? "REJECTED" : "REVIEWED", analysisId);
        return new HumanReviewResponse(reviewId, analysisId, action, "RECORDED", Instant.now());
    }

    /** Regenerates an analysis using the previous request context where available. */
    @Transactional
    public RootCauseAnalysisResponse regenerate(UUID analysisId, UUID userId) {
        RootCauseAnalysisResponse prior = get(analysisId);
        jdbcTemplate.update("update decision.root_cause_analyses set status = 'SUPERSEDED', updated_at = now() where id = ?", analysisId);
        RootCauseAnalysisRequest request = new RootCauseAnalysisRequest(
                new ProblemRequest(prior.problem().problemId(), prior.problem().village(), prior.problem().domain(), prior.problem().description(), prior.problem().affectedPopulation(), prior.problem().severity(), prior.problem().evidence(), prior.problem().timestamp(), prior.problem().source()),
                List.of(),
                List.of(),
                Map.of(),
                List.of(),
                null,
                null,
                prior.surveyVersion(),
                prior.knowledgeSnapshot(),
                true);
        RootCauseAnalysisResponse regenerated = analyze(request, userId);
        jdbcTemplate.update("insert into decision.root_cause_analysis_versions (analysis_id, version_number, analysis_json, model, model_version, prompt_version, knowledge_snapshot, survey_version) values (?, ?, ?::jsonb, ?, ?, ?, ?, ?)",
                regenerated.analysisId(), regenerated.versionNumber() + 1, json(regenerated), regenerated.model(), regenerated.modelVersion(), regenerated.promptVersion(), regenerated.knowledgeSnapshot(), regenerated.surveyVersion());
        return regenerated;
    }

    private ProblemResponse normalizeProblem(ProblemRequest input) {
        if (input == null) {
            throw new DecisionException("PROBLEM_REQUIRED", "Problem representation is required", HttpStatus.BAD_REQUEST);
        }
        String domain = value(input.domain(), "OTHER").toUpperCase(Locale.ROOT);
        if (!DOMAINS.contains(domain)) {
            domain = "OTHER";
        }
        return new ProblemResponse(
                value(input.problemId(), "problem-" + UUID.randomUUID()),
                value(input.village(), "UNKNOWN"),
                domain,
                input.description(),
                input.affectedPopulation(),
                value(input.severity(), "UNKNOWN"),
                input.evidence() == null ? List.of() : input.evidence(),
                input.timestamp() == null ? Instant.now() : input.timestamp(),
                value(input.source(), "user"));
    }

    private List<FactResponse> extractFacts(RootCauseAnalysisRequest request, ProblemResponse problem) {
        List<FactResponse> facts = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(1);
        facts.add(new FactResponse("fact-" + counter.getAndIncrement(), problem.description(), problem.source(), "PROBLEM_STATEMENT", "OBSERVED_FACT", 0.78, problem.timestamp()));
        flattenFacts(request.surveyResponses(), "survey-response", "SURVEY_RESPONSE", counter, facts);
        flattenFacts(request.evidence(), "uploaded-evidence", "UPLOADED_EVIDENCE", counter, facts);
        flattenFacts(List.of(request.structuredData() == null ? Map.of() : request.structuredData()), "structured-data", "STRUCTURED_DATA", counter, facts);
        flattenFacts(request.retrievedDocuments(), "caller-retrieved-document", "RETRIEVED_DOCUMENT", counter, facts);
        return facts.stream().filter(item -> item.statement() != null && !item.statement().isBlank()).limit(80).toList();
    }

    private void flattenFacts(List<Map<String, Object>> rows, String source, String sourceType, AtomicInteger counter, List<FactResponse> facts) {
        if (rows == null) {
            return;
        }
        for (Map<String, Object> row : rows) {
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                Object value = entry.getValue();
                if (value == null || String.valueOf(value).isBlank()) {
                    continue;
                }
                String statement = entry.getKey() + ": " + summarize(String.valueOf(value), 260);
                facts.add(new FactResponse("fact-" + counter.getAndIncrement(), statement, source, sourceType, sourceType.equals("RETRIEVED_DOCUMENT") ? "RETRIEVED_EVIDENCE" : "OBSERVED_FACT", sourceType.equals("UPLOADED_EVIDENCE") ? 0.82 : 0.74, Instant.now()));
            }
        }
    }

    private List<FactResponse> retrievedEvidenceFacts(RagQueryResponse rag) {
        AtomicInteger counter = new AtomicInteger(1);
        return rag.citations().stream()
                .map(citation -> new FactResponse("retrieved-" + counter.getAndIncrement(), citation.excerpt(), citation.sourceId(), citation.sourceType(), "RETRIEVED_EVIDENCE", clamp(citation.score()), Instant.now()))
                .toList();
    }

    private List<EvidenceAssessmentResponse> assessEvidence(List<FactResponse> facts, ProblemResponse problem) {
        AtomicInteger counter = new AtomicInteger(1);
        return facts.stream().map(fact -> {
            double reliability = switch (fact.sourceType()) {
                case "SURVEY_RESPONSE", "STRUCTURED_DATA" -> 0.78;
                case "UPLOADED_EVIDENCE" -> 0.84;
                case "RETRIEVED_DOCUMENT", "rag-service" -> 0.72;
                default -> 0.62;
            };
            double relevance = relevance(fact.statement(), problem.description() + " " + problem.domain());
            double freshness = fact.timestamp() == null ? 0.55 : 0.78;
            double consistency = hasContradictionMarker(fact.statement()) ? 0.38 : 0.76;
            double confidence = round((reliability * 0.32) + (relevance * 0.34) + (freshness * 0.14) + (consistency * 0.20));
            return new EvidenceAssessmentResponse("evidence-" + counter.getAndIncrement(), fact.statement(), fact.source(), fact.sourceType(), reliability, relevance, freshness, consistency, confidence, fact.category());
        }).toList();
    }

    private List<FactorResponse> factors(ProblemResponse problem, List<FactResponse> facts, List<EvidenceAssessmentResponse> assessments) {
        List<String> factorNames = domainFactors(problem.domain(), problem.description());
        List<FactorResponse> result = new ArrayList<>();
        for (String factor : factorNames) {
            List<String> support = facts.stream().filter(fact -> relevance(fact.statement(), factor) > 0.12).map(FactResponse::factId).limit(6).toList();
            List<String> contradict = facts.stream().filter(fact -> isContradiction(fact.statement(), factor)).map(FactResponse::factId).limit(4).toList();
            double evidenceBoost = support.isEmpty() ? 0.18 : support.size() / 8.0;
            double confidence = round(Math.min(0.86, 0.32 + evidenceBoost - (contradict.size() * 0.12)));
            result.add(new FactorResponse(factor, support, contradict, confidence, support.isEmpty() ? "MODEL_INFERENCE" : "EVIDENCE_FUSION"));
        }
        return result.stream().sorted(Comparator.comparing(FactorResponse::confidence).reversed()).toList();
    }

    private List<CandidateRootCauseResponse> candidates(ProblemResponse problem, List<FactorResponse> factors, List<FactResponse> facts, List<String> contradictions, RagQueryResponse rag) {
        List<CandidateRootCauseResponse> candidates = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(1);
        for (FactorResponse factor : factors.stream().limit(4).toList()) {
            String id = "root-cause-" + counter.getAndIncrement();
            List<String> supportingEvidence = new ArrayList<>(factor.supportingEvidence());
            rag.citations().stream().map(CitationResponse::sourceId).limit(3).forEach(supportingEvidence::add);
            List<String> assumptions = factor.supportingEvidence().isEmpty()
                    ? List.of("This is a hypothesis requiring more direct village evidence.")
                    : List.of("Survey or evidence statements are treated as direct observations, not causal proof.");
            String description = factor.factor() + " may contribute to " + problem.description();
            String uncertainty = factor.contradictingEvidence().isEmpty() ? "Causality is not scientifically established; evidence supports a likely contributing association." : "Contradictory evidence reduces confidence.";
            double confidence = round(Math.max(0.15, factor.confidence() + (supportingEvidence.isEmpty() ? 0.0 : 0.06) - (contradictions.isEmpty() ? 0.0 : 0.08)));
            candidates.add(new CandidateRootCauseResponse(id, description, factor.supportingEvidence(), supportingEvidence, factor.contradictingEvidence(), confidence, problem.domain(), assumptions, uncertainty, "Identified because observed facts and retrieved evidence align with the candidate factor. This is a decision-support inference, not a scientific causal claim."));
        }
        return candidates;
    }

    private List<AlternativeHypothesisResponse> alternatives(ProblemResponse problem, List<CandidateRootCauseResponse> candidates, List<FactResponse> facts) {
        AtomicInteger counter = new AtomicInteger(1);
        Set<String> used = new LinkedHashSet<>(candidates.stream().map(CandidateRootCauseResponse::description).toList());
        return domainFactors(problem.domain(), problem.description()).stream()
                .filter(factor -> used.stream().noneMatch(text -> text.toLowerCase(Locale.ROOT).contains(factor.toLowerCase(Locale.ROOT))))
                .limit(4)
                .map(factor -> new AlternativeHypothesisResponse("hypothesis-" + counter.getAndIncrement(), factor + " could also explain the observed problem.", facts.stream().filter(fact -> relevance(fact.statement(), factor) > 0.10).map(FactResponse::factId).limit(4).toList(), List.of("Targeted village measurements for " + factor, "Time-series evidence linking " + factor + " to the outcome"), 0.34))
                .toList();
    }

    private List<CausalRelationshipResponse> graph(ProblemResponse problem, List<FactorResponse> factors, List<CandidateRootCauseResponse> candidates) {
        List<CausalRelationshipResponse> edges = new ArrayList<>();
        for (FactorResponse factor : factors.stream().limit(5).toList()) {
            edges.add(new CausalRelationshipResponse(factor.factor(), problem.description(), "FACTOR_ASSOCIATED_WITH_OUTCOME", factor.confidence(), factor.supportingEvidence(), factor.source()));
        }
        for (CandidateRootCauseResponse candidate : candidates) {
            edges.add(new CausalRelationshipResponse(candidate.description(), problem.description(), "CANDIDATE_CAUSE_MAY_CONTRIBUTE_TO_OUTCOME", candidate.confidence(), candidate.supportingEvidence(), "ROOT_CAUSE_ENGINE"));
        }
        return edges;
    }

    private List<UncertaintyResponse> uncertainties(ProblemResponse problem, List<CandidateRootCauseResponse> candidates, List<String> contradictions, List<FactResponse> facts) {
        List<UncertaintyResponse> result = new ArrayList<>();
        if (facts.size() < 4) {
            result.add(new UncertaintyResponse("uncertainty-evidence-volume", "Insufficient direct village evidence to distinguish root causes confidently.", List.of("More survey responses", "Recent uploaded evidence", "Local administrative records"), followUps(problem.domain()), "HIGH"));
        }
        if (!contradictions.isEmpty()) {
            result.add(new UncertaintyResponse("uncertainty-contradictions", "Contradictory evidence was found and should be reviewed by a human.", contradictions, List.of("Which source is more recent?", "Can field staff verify the conflicting statements?"), "HIGH"));
        }
        if (candidates.stream().noneMatch(candidate -> candidate.confidence() >= 0.65)) {
            result.add(new UncertaintyResponse("uncertainty-causal-strength", "Evidence supports association but not strong causal attribution.", List.of("No controlled causal evidence", "Limited longitudinal evidence"), followUps(problem.domain()), "MEDIUM"));
        }
        return result;
    }

    private ConfidenceResponse confidence(List<EvidenceAssessmentResponse> evidence, List<String> contradictions, List<CandidateRootCauseResponse> validated, List<UncertaintyResponse> uncertainties) {
        double sourceReliability = average(evidence.stream().map(EvidenceAssessmentResponse::reliability).toList());
        double relevance = average(evidence.stream().map(EvidenceAssessmentResponse::relevance).toList());
        double freshness = average(evidence.stream().map(EvidenceAssessmentResponse::freshness).toList());
        double quantity = Math.min(0.9, evidence.size() / 8.0);
        double consistency = average(evidence.stream().map(EvidenceAssessmentResponse::consistency).toList());
        double contradictionPenalty = Math.min(0.35, contradictions.size() * 0.12);
        double validatedBoost = validated.isEmpty() ? 0.0 : 0.08;
        double uncertaintyPenalty = Math.min(0.25, uncertainties.size() * 0.06);
        double overall = round(Math.max(0.05, (sourceReliability * 0.22) + (relevance * 0.24) + (freshness * 0.10) + (quantity * 0.15) + (consistency * 0.21) + validatedBoost - contradictionPenalty - uncertaintyPenalty));
        return new ConfidenceResponse(overall, sourceReliability, relevance, freshness, quantity, consistency, contradictionPenalty, "Confidence is a transparent decision-support score, not a calibrated scientific probability.");
    }

    private void persist(RootCauseAnalysisResponse analysis, RootCauseAnalysisRequest request, UUID userId, boolean humanReviewRequired) {
        jdbcTemplate.update(
                "insert into decision.root_cause_analyses (id, survey_id, organization_id, requested_by, problem_json, analysis_json, model, model_version, prompt_version, knowledge_snapshot, survey_version, confidence_score, human_review_required) values (?, ?, ?, ?, ?::jsonb, ?::jsonb, ?, ?, ?, ?, ?, ?, ?)",
                analysis.analysisId(), request.surveyId(), request.organizationId(), userId, json(analysis.problem()), json(analysis), analysis.model(), analysis.modelVersion(), analysis.promptVersion(), analysis.knowledgeSnapshot(), analysis.surveyVersion(), analysis.confidence().overall(), humanReviewRequired);
        jdbcTemplate.update("insert into decision.root_cause_analysis_versions (analysis_id, version_number, analysis_json, model, model_version, prompt_version, knowledge_snapshot, survey_version) values (?, ?, ?::jsonb, ?, ?, ?, ?, ?)", analysis.analysisId(), 1, json(analysis), analysis.model(), analysis.modelVersion(), analysis.promptVersion(), analysis.knowledgeSnapshot(), analysis.surveyVersion());
        jdbcTemplate.update("insert into decision.root_cause_problems (analysis_id, problem_id, village, domain, description, affected_population, severity, source, problem_timestamp) values (?, ?, ?, ?, ?, ?, ?, ?, ?)", analysis.analysisId(), analysis.problem().problemId(), analysis.problem().village(), analysis.problem().domain(), analysis.problem().description(), analysis.problem().affectedPopulation(), analysis.problem().severity(), analysis.problem().source(), Timestamp.from(analysis.problem().timestamp()));
        analysis.observedFacts().forEach(fact -> jdbcTemplate.update("insert into decision.observed_facts (analysis_id, fact_id, statement, source, source_type, category, confidence, fact_timestamp) values (?, ?, ?, ?, ?, ?, ?, ?)", analysis.analysisId(), fact.factId(), fact.statement(), fact.source(), fact.sourceType(), fact.category(), fact.confidence(), Timestamp.from(fact.timestamp())));
        analysis.contributingFactors().forEach(factor -> jdbcTemplate.update("insert into decision.contributing_factors (analysis_id, factor, supporting_evidence_json, contradicting_evidence_json, confidence, source) values (?, ?, ?::jsonb, ?::jsonb, ?, ?)", analysis.analysisId(), factor.factor(), json(factor.supportingEvidence()), json(factor.contradictingEvidence()), factor.confidence(), factor.source()));
        analysis.candidateRootCauses().forEach(candidate -> jdbcTemplate.update("insert into decision.root_cause_candidates (analysis_id, root_cause_id, description, supporting_facts_json, supporting_evidence_json, contradicting_evidence_json, confidence, affected_domain, assumptions_json, uncertainty, validated, reasoning_summary) values (?, ?, ?, ?::jsonb, ?::jsonb, ?::jsonb, ?, ?, ?::jsonb, ?, ?, ?)", analysis.analysisId(), candidate.rootCauseId(), candidate.description(), json(candidate.supportingFacts()), json(candidate.supportingEvidence()), json(candidate.contradictingEvidence()), candidate.confidence(), candidate.affectedDomain(), json(candidate.assumptions()), candidate.uncertainty(), analysis.validatedRootCauses().stream().anyMatch(valid -> valid.rootCauseId().equals(candidate.rootCauseId())), candidate.reasoningSummary()));
        analysis.alternativeHypotheses().forEach(hypothesis -> jdbcTemplate.update("insert into decision.alternative_hypotheses (analysis_id, hypothesis_id, description, supporting_evidence_json, missing_evidence_json, confidence) values (?, ?, ?, ?::jsonb, ?::jsonb, ?)", analysis.analysisId(), hypothesis.hypothesisId(), hypothesis.description(), json(hypothesis.supportingEvidence()), json(hypothesis.missingEvidence()), hypothesis.confidence()));
        analysis.causalGraph().forEach(edge -> jdbcTemplate.update("insert into decision.causal_relationships (analysis_id, from_node, to_node, relationship_type, confidence, evidence_json, source) values (?, ?, ?, ?, ?, ?::jsonb, ?)", analysis.analysisId(), edge.from(), edge.to(), edge.relationshipType(), edge.confidence(), json(edge.evidence()), edge.source()));
        analysis.evidence().forEach(evidence -> jdbcTemplate.update("insert into decision.evidence_assessments (analysis_id, evidence_id, statement, source, source_type, reliability, relevance, freshness, consistency, confidence, category) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", analysis.analysisId(), evidence.evidenceId(), evidence.statement(), evidence.source(), evidence.sourceType(), evidence.reliability(), evidence.relevance(), evidence.freshness(), evidence.consistency(), evidence.confidence(), evidence.category()));
        analysis.uncertainties().forEach(uncertainty -> jdbcTemplate.update("insert into decision.root_cause_uncertainties (analysis_id, uncertainty_id, statement, missing_evidence_json, follow_up_questions_json, severity) values (?, ?, ?, ?::jsonb, ?::jsonb, ?)", analysis.analysisId(), uncertainty.uncertaintyId(), uncertainty.statement(), json(uncertainty.missingEvidence()), json(uncertainty.followUpQuestions()), uncertainty.severity()));
    }

    private void validate(RootCauseAnalysisResponse response) {
        if (response.problem() == null || response.observedFacts().isEmpty() || response.candidateRootCauses().isEmpty() || response.confidence() == null) {
            throw new DecisionException("ROOT_CAUSE_SCHEMA_INVALID", "Root-cause analysis failed schema validation", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        boolean invalidCategory = response.observedFacts().stream().anyMatch(fact -> !Set.of("OBSERVED_FACT", "RETRIEVED_EVIDENCE", "MODEL_INFERENCE", "HYPOTHESIS", "RECOMMENDATION", "CONTRADICTORY_EVIDENCE").contains(fact.category()));
        if (invalidCategory) {
            throw new DecisionException("ROOT_CAUSE_CATEGORY_INVALID", "Root-cause analysis contains an invalid fact category", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private List<String> domainFactors(String domain, String description) {
        String lower = (domain + " " + description).toLowerCase(Locale.ROOT);
        if (lower.contains("water") || domain.equals("WATER")) return List.of("poor water-source reliability", "maintenance accountability gap", "seasonal water availability", "delayed repair response", "household access inequality");
        if (lower.contains("crop") || lower.contains("agri") || domain.equals("AGRICULTURE")) return List.of("poor irrigation access", "soil health constraints", "market access limitation", "high input cost", "crop disease or climate stress");
        if (lower.contains("school") || domain.equals("EDUCATION")) return List.of("household economic pressure", "school access barrier", "teacher availability gap", "health or nutrition issue", "seasonal migration");
        if (lower.contains("employment") || domain.equals("EMPLOYMENT")) return List.of("limited local job availability", "skill mismatch", "market access limitation", "seasonal work dependency", "credit access constraint");
        return List.of("service availability gap", "infrastructure reliability issue", "governance accountability gap", "household access barrier", "resource constraint");
    }

    private List<String> contradictions(List<FactResponse> facts) {
        List<String> statements = facts.stream().map(FactResponse::statement).toList();
        List<String> contradictions = new ArrayList<>();
        for (String left : statements) {
            for (String right : statements) {
                if (!left.equals(right) && polarityConflict(left, right)) {
                    contradictions.add("CONTRADICTORY_EVIDENCE: '" + summarize(left, 120) + "' conflicts with '" + summarize(right, 120) + "'");
                }
            }
        }
        return contradictions.stream().distinct().limit(6).toList();
    }

    private boolean polarityConflict(String left, String right) {
        String l = left.toLowerCase(Locale.ROOT);
        String r = right.toLowerCase(Locale.ROOT);
        boolean sharedWater = l.contains("water") && r.contains("water");
        boolean available = Pattern.compile("\\b(available|throughout|adequate|sufficient)\\b").matcher(l).find() && Pattern.compile("\\b(shortage|scarce|unavailable|failure|downtime|severe)\\b").matcher(r).find();
        boolean reverse = Pattern.compile("\\b(shortage|scarce|unavailable|failure|downtime|severe)\\b").matcher(l).find() && Pattern.compile("\\b(available|throughout|adequate|sufficient)\\b").matcher(r).find();
        return sharedWater && (available || reverse);
    }

    private boolean isContradiction(String statement, String factor) {
        return polarityConflict(statement, factor) || hasContradictionMarker(statement);
    }

    private boolean hasContradictionMarker(String text) {
        return text.toLowerCase(Locale.ROOT).contains("conflict") || text.toLowerCase(Locale.ROOT).contains("contradict");
    }

    private String ragQuestion(ProblemResponse problem) {
        return "Find trusted rural knowledge about likely contributing factors, evidence requirements, and limitations for this problem: " + problem.description() + " in domain " + problem.domain();
    }

    private List<String> followUps(String domain) {
        return switch (domain) {
            case "WATER" -> List.of("How many households experience seasonal water shortage?", "Which water source fails most often?", "How long does repair usually take?");
            case "AGRICULTURE" -> List.of("How many households have irrigation access?", "Which crops are most affected?", "What input cost changed recently?");
            case "EDUCATION" -> List.of("Which age groups have declining attendance?", "What is the distance to school?", "Are absences seasonal?");
            case "EMPLOYMENT" -> List.of("How many households report job availability issues?", "What skills are locally in demand?", "Is unemployment seasonal?");
            default -> List.of("Which households are most affected?", "What changed recently?", "What direct evidence can field staff verify?");
        };
    }

    private List<String> limitations(ConfidenceResponse confidence, RagQueryResponse rag, List<String> contradictions) {
        List<String> limitations = new ArrayList<>();
        limitations.add("The analysis provides decision-support confidence, not calibrated scientific causal probability.");
        if (rag.citations().isEmpty()) limitations.add("No trusted RAG citations were available for the problem.");
        if (!contradictions.isEmpty()) limitations.add("Contradictory evidence requires human validation before action.");
        if (confidence.overall() < 0.65) limitations.add("Evidence is insufficient for high-confidence root-cause validation.");
        return limitations;
    }

    private List<String> graphNodes(RootCauseAnalysisResponse analysis) {
        LinkedHashSet<String> nodes = new LinkedHashSet<>();
        nodes.add(analysis.problem().description());
        analysis.contributingFactors().forEach(factor -> nodes.add(factor.factor()));
        analysis.candidateRootCauses().forEach(candidate -> nodes.add(candidate.description()));
        return List.copyOf(nodes);
    }

    private double relevance(String text, String query) {
        Set<String> left = tokens(text);
        Set<String> right = tokens(query);
        if (left.isEmpty() || right.isEmpty()) return 0.0;
        long overlap = left.stream().filter(right::contains).count();
        return round(Math.min(1.0, overlap / (double) Math.max(1, right.size())));
    }

    private Set<String> tokens(String value) {
        Set<String> stop = Set.of("the", "and", "for", "with", "that", "this", "from", "into", "are", "was", "were", "will", "shall", "may");
        Set<String> tokens = new LinkedHashSet<>();
        for (String token : value.toLowerCase(Locale.ROOT).split("[^a-z0-9_-]+")) {
            if (token.length() > 2 && !stop.contains(token)) tokens.add(token);
        }
        return tokens;
    }

    private double average(List<Double> values) {
        return values.isEmpty() ? 0.0 : round(values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
    }

    private double clamp(Double value) {
        return value == null ? 0.0 : Math.max(0.0, Math.min(1.0, value));
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    private String summarize(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 3) + "...";
    }

    private String value(String text, String fallback) {
        return text == null || text.isBlank() ? fallback : text;
    }

    private String json(Object value) {
        try { return objectMapper.writeValueAsString(value); } catch (Exception ex) { return "{}"; }
    }

    private <T> T read(String json, Class<T> type) {
        try { return objectMapper.readValue(json, type); } catch (Exception ex) { throw new DecisionException("ROOT_CAUSE_JSON_INVALID", "Stored root-cause analysis could not be read", HttpStatus.INTERNAL_SERVER_ERROR); }
    }

    @SuppressWarnings("unused")
    private Map<String, Object> map(String json) {
        try { return objectMapper.readValue(json, new TypeReference<>() {}); } catch (Exception ex) { return Map.of(); }
    }
}
