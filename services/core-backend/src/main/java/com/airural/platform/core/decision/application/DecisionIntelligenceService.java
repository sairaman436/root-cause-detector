/*
 * Purpose: Orchestrates root-cause discovery, recommendation generation, confidence scoring, and explainability.
 * Why it exists: The platform needs one production decision intelligence facade that transforms evidence into reviewable decisions.
 * Architecture fit: Application service for Milestone 10 Decision Intelligence Engine.
 */
package com.airural.platform.core.decision.application;

import com.airural.platform.core.decision.domain.*;
import com.airural.platform.core.decision.infrastructure.*;
import com.airural.platform.core.decision.web.dto.DecisionDtos.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Facade service for decision intelligence workflows. */
@Service
public class DecisionIntelligenceService {
    private final DecisionRepository decisionRepository;
    private final DecisionVersionRepository versionRepository;
    private final RootCauseRepository rootCauseRepository;
    private final HypothesisRepository hypothesisRepository;
    private final RecommendationRepository recommendationRepository;
    private final RecommendationEvidenceRepository recommendationEvidenceRepository;
    private final DecisionTraceRepository traceRepository;
    private final ConfidenceScoreRepository confidenceRepository;
    private final DecisionAuditRepository auditRepository;
    private final EvidenceFusionEngine evidenceFusionEngine;
    private final DecisionRuleEngine ruleEngine;
    private final ConfidenceEngine confidenceEngine;
    private final HypothesisGenerator hypothesisGenerator;
    private final RecommendationEngine recommendationEngine;
    private final DecisionTraceEngine traceEngine;
    private final ObjectMapper objectMapper;

    public DecisionIntelligenceService(DecisionRepository decisionRepository, DecisionVersionRepository versionRepository, RootCauseRepository rootCauseRepository, HypothesisRepository hypothesisRepository, RecommendationRepository recommendationRepository, RecommendationEvidenceRepository recommendationEvidenceRepository, DecisionTraceRepository traceRepository, ConfidenceScoreRepository confidenceRepository, DecisionAuditRepository auditRepository, EvidenceFusionEngine evidenceFusionEngine, DecisionRuleEngine ruleEngine, ConfidenceEngine confidenceEngine, HypothesisGenerator hypothesisGenerator, RecommendationEngine recommendationEngine, DecisionTraceEngine traceEngine, ObjectMapper objectMapper) {
        this.decisionRepository = decisionRepository;
        this.versionRepository = versionRepository;
        this.rootCauseRepository = rootCauseRepository;
        this.hypothesisRepository = hypothesisRepository;
        this.recommendationRepository = recommendationRepository;
        this.recommendationEvidenceRepository = recommendationEvidenceRepository;
        this.traceRepository = traceRepository;
        this.confidenceRepository = confidenceRepository;
        this.auditRepository = auditRepository;
        this.evidenceFusionEngine = evidenceFusionEngine;
        this.ruleEngine = ruleEngine;
        this.confidenceEngine = confidenceEngine;
        this.hypothesisGenerator = hypothesisGenerator;
        this.recommendationEngine = recommendationEngine;
        this.traceEngine = traceEngine;
        this.objectMapper = objectMapper;
    }

    /** Runs the full decision analysis pipeline. */
    @Transactional
    public DecisionResponse analyze(DecisionAnalyzeRequest request, UUID userId) {
        Map<String, Object> context = evidenceFusionEngine.fuse(request);
        DecisionEntity decision = decisionRepository.save(new DecisionEntity(request.surveyId(), request.organizationId(), userId, "FULL_ANALYSIS", json(context)));
        RuleEvaluationResult rules = ruleEngine.evaluate(context);
        ConfidenceResult confidence = confidenceEngine.score(context, rules);
        List<HypothesisEntity> hypotheses = hypothesisGenerator.generate(decision.id(), context, confidence);
        hypothesisRepository.saveAll(hypotheses);
        List<RootCauseEntity> rootCauses = hypotheses.stream()
                .filter(hypothesis -> !hypothesis.alternative())
                .map(hypothesis -> new RootCauseEntity(decision.id(), hypothesis.title(), hypothesis.rationale(), hypothesis.rank(), hypothesis.confidence(), json(Map.of("surveyEvidence", context.get("surveyEvidence"), "knowledgeSources", context.get("knowledgeSources")))))
                .toList();
        rootCauseRepository.saveAll(rootCauses);
        List<RecommendationEntity> recommendations = recommendationEngine.recommend(decision.id(), hypotheses, confidence, rules);
        recommendationRepository.saveAll(recommendations);
        for (RecommendationEntity recommendation : recommendations) {
            recommendationEvidenceRepository.save(new RecommendationEvidenceEntity(recommendation.id(), "POLICY_AND_EVIDENCE", "decision:" + decision.id(), "Recommendation supported by fused survey evidence, policy context, and generated hypotheses.", recommendation.confidence()));
        }
        traceRepository.saveAll(traceEngine.traces(decision.id(), context, rules, confidence));
        confidenceRepository.save(new ConfidenceScoreEntity(decision.id(), confidence.overall(), confidence.evidenceCompleteness(), confidence.knowledgeCoverage(), confidence.mlConfidence(), confidence.ruleConsistency(), confidence.historicalSimilarity(), confidence.agentAgreement(), confidence.contradictoryEvidence(), json(confidence.reasonCodes()), json(confidence.missingEvidence()), json(confidence.requiredFollowups())));
        boolean approval = Boolean.TRUE.equals(request.requireHumanApproval()) || confidence.overall() < 0.85 || recommendations.stream().anyMatch(RecommendationEntity::humanApprovalRequired);
        String finalDecision = "Decision generated from fused evidence with " + rootCauses.size() + " primary root cause(s), " + hypotheses.size() + " hypotheses, and " + recommendations.size() + " recommendation(s). Human review required: " + approval;
        decision.complete(finalDecision, confidence.overall(), approval);
        decisionRepository.save(decision);
        versionRepository.save(new DecisionVersionEntity(decision.id(), 1, json(Map.of("decision", finalDecision, "hypotheses", hypotheses.size(), "recommendations", recommendations.size())), confidence.overall()));
        auditRepository.save(new DecisionAuditEntity(userId, "DECISION_ANALYZED", "DECISION", decision.id(), "SUCCESS", json(Map.of("confidence", confidence.overall(), "approval", approval))));
        return response(decision, rootCauseRepository.findByDecisionIdOrderByRankAsc(decision.id()), recommendationRepository.findByDecisionIdOrderByPriorityAsc(decision.id()));
    }

    /** Runs root-cause-only analysis. */
    @Transactional
    public DecisionResponse rootCause(RootCauseRequest request, UUID userId) {
        return analyze(new DecisionAnalyzeRequest(request.surveyId(), request.organizationId(), List.of(), request.problemStatement(), request.evidenceContext(), Map.of("confidence", 0.72), Map.of("agentAgreement", "prepared"), true), userId);
    }

    /** Runs recommendation-only analysis, creating a new decision if needed. */
    @Transactional
    public DecisionResponse recommend(RecommendationRequest request, UUID userId) {
        DecisionAnalyzeRequest analyzeRequest = new DecisionAnalyzeRequest(request.surveyId(), request.organizationId(), List.of(), request.objective(), request.context(), Map.of("confidence", 0.74), Map.of("recommendationAgent", "prepared"), true);
        return analyze(analyzeRequest, userId);
    }

    @Transactional(readOnly = true)
    public Page<DecisionResponse> history(Pageable pageable) {
        return decisionRepository.findAll(pageable).map(decision -> response(decision, rootCauseRepository.findByDecisionIdOrderByRankAsc(decision.id()), recommendationRepository.findByDecisionIdOrderByPriorityAsc(decision.id())));
    }

    @Transactional(readOnly = true)
    public DecisionResponse decision(UUID id) {
        DecisionEntity decision = decisionRepository.findById(id).orElseThrow(() -> new DecisionException("DECISION_NOT_FOUND", "Decision was not found", HttpStatus.NOT_FOUND));
        return response(decision, rootCauseRepository.findByDecisionIdOrderByRankAsc(id), recommendationRepository.findByDecisionIdOrderByPriorityAsc(id));
    }

    @Transactional(readOnly = true)
    public ExplanationResponse explanation(UUID id) {
        if (!decisionRepository.existsById(id)) throw new DecisionException("DECISION_NOT_FOUND", "Decision was not found", HttpStatus.NOT_FOUND);
        return new ExplanationResponse(
                id,
                traceRepository.findByDecisionIdOrderByStepNumberAsc(id).stream().map(trace -> new DecisionTraceResponse(trace.id(), trace.stepName(), trace.detailsJson(), trace.confidenceAfterStep())).toList(),
                hypothesisRepository.findByDecisionIdOrderByRankAsc(id).stream().map(h -> new HypothesisResponse(h.id(), h.title(), h.rationale(), h.confidence(), h.rank(), h.alternative())).toList(),
                rootCauseRepository.findByDecisionIdOrderByRankAsc(id).stream().map(this::rootCauseResponse).toList(),
                recommendationRepository.findByDecisionIdOrderByPriorityAsc(id).stream().map(this::recommendationResponse).toList(),
                List.of("survey-evidence", "uploaded-evidence", "knowledge-platform", "policy-catalog", "historical-cases", "agent-outputs"));
    }

    @Transactional(readOnly = true)
    public ConfidenceResponse confidence(UUID id) {
        ConfidenceScoreEntity score = confidenceRepository.findByDecisionId(id).orElseThrow(() -> new DecisionException("CONFIDENCE_NOT_FOUND", "Confidence score was not found", HttpStatus.NOT_FOUND));
        return new ConfidenceResponse(score.id(), score.overallConfidence(), list(score.reasonCodesJson()), list(score.missingEvidenceJson()), list(score.requiredFollowupsJson()));
    }

    private DecisionResponse response(DecisionEntity decision, List<RootCauseEntity> rootCauses, List<RecommendationEntity> recommendations) {
        return new DecisionResponse(decision.id(), decision.status(), decision.decisionType(), decision.finalDecision(), decision.overallConfidence(), decision.humanApprovalRequired(), rootCauses.stream().map(this::rootCauseResponse).toList(), recommendations.stream().map(this::recommendationResponse).toList(), List.of("survey-evidence", "uploaded-evidence", "policy-catalog", "rag-context", "agent-output"), decision.createdAt());
    }

    private RootCauseResponse rootCauseResponse(RootCauseEntity rootCause) {
        return new RootCauseResponse(rootCause.id(), rootCause.title(), rootCause.description(), rootCause.rank(), rootCause.confidence(), rootCause.evidenceJson());
    }

    private RecommendationResponse recommendationResponse(RecommendationEntity recommendation) {
        return new RecommendationResponse(recommendation.id(), recommendation.title(), recommendation.description(), recommendation.priority(), recommendation.impactScore(), recommendation.confidence(), recommendation.humanApprovalRequired());
    }

    private String json(Object value) {
        try { return objectMapper.writeValueAsString(value); } catch (Exception ex) { return "{}"; }
    }

    private List<String> list(String json) {
        try { return objectMapper.readValue(json, new TypeReference<List<String>>() {}); } catch (Exception ex) { return List.of(); }
    }
}
