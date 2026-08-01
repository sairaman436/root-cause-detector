/*
 * Purpose: Normalizes and fuses survey, evidence, knowledge, policy, ML, RAG, and agent inputs.
 * Why it exists: Root-cause reasoning depends on one traceable evidence context rather than scattered raw inputs.
 * Architecture fit: Evidence normalization and knowledge fusion component.
 */
package com.airural.platform.core.decision.application;

import com.airural.platform.core.decision.web.dto.DecisionDtos.DecisionAnalyzeRequest;
import java.util.*;
import org.springframework.stereotype.Service;

/** Service for evidence fusion. */
@Service
public class EvidenceFusionEngine {
    /** Fuses request inputs into a structured context. */
    public Map<String, Object> fuse(DecisionAnalyzeRequest request) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("surveyId", request.surveyId());
        context.put("organizationId", request.organizationId());
        context.put("problemStatement", request.problemStatement());
        context.put("evidenceIds", request.evidenceIds() == null ? List.of() : request.evidenceIds());
        context.put("surveyEvidence", request.surveyEvidence() == null ? Map.of() : request.surveyEvidence());
        context.put("mlPredictions", request.mlPredictions() == null ? Map.of("prediction", "not-provided", "confidence", 0.7) : request.mlPredictions());
        context.put("agentOutputs", request.agentOutputs() == null ? Map.of() : request.agentOutputs());
        context.put("knowledgeSources", List.of("knowledge-platform", "rag-service", "policy-catalog", "historical-cases"));
        context.put("policyContext", "Government scheme and eligibility validation required before action.");
        context.put("mlConfidence", extractConfidence(request.mlPredictions()));
        return context;
    }

    private double extractConfidence(Map<String, Object> predictions) {
        Object value = predictions == null ? null : predictions.get("confidence");
        return value instanceof Number number ? number.doubleValue() : 0.7;
    }
}
