/*
 * Purpose: Applies deterministic semantic checks between observations, governed evidence,
 * root-cause hypotheses, and recommendations.
 * Why it exists: A valid source ID alone does not prove that a citation supports a decision.
 * Architecture fit: Shared application-layer policy for the Decision Intelligence workflow;
 * it does not retrieve, generate, or persist data.
 */
package com.airural.platform.core.decision.application;

import com.airural.platform.core.ai.web.dto.AiDtos.CitationResponse;
import java.util.*;

/** Conservative, explainable grounding checks used before decision outputs are marked supported. */
public final class SemanticGroundingValidator {
    private static final double MIN_RETRIEVAL_SCORE = 0.18;
    private static final Set<String> STOP_WORDS = Set.of(
            "the", "and", "for", "with", "that", "this", "from", "into", "are", "was", "were", "will", "shall", "may",
            "image", "shows", "visible", "condition", "actual", "evidence", "source", "knowledge", "trusted", "rural",
            "support", "supports", "supported", "cause", "causal", "likely", "contribute", "contributes", "problem", "question",
            "user", "field", "scenario", "data", "information", "issue", "analysis", "recommendation", "recommendations",
            "intervention", "interventions", "required", "missing", "unknown", "uncertainty", "available", "need", "needs",
            "monitoring", "verification", "verify", "service", "access", "improve", "improvement", "gap");

    /** Returns citations whose score and excerpt provide meaningful support for the context. */
    public List<CitationResponse> relevantCitations(String context, List<CitationResponse> citations) {
        if (context == null || context.isBlank() || citations == null) return List.of();
        return citations.stream()
                .filter(Objects::nonNull)
                .filter(citation -> citation.sourceId() != null && !citation.sourceId().isBlank())
                .filter(citation -> citation.excerpt() != null && !citation.excerpt().isBlank())
                .filter(citation -> citation.score() == null || citation.score() >= MIN_RETRIEVAL_SCORE)
                .filter(citation -> sharesMeaningfulTerm(context, citation.excerpt()))
                .toList();
    }

    /** Checks whether an inferred cause is supported by observed or retrieved evidence. */
    public boolean supportsRootCause(String rootCause, List<String> supportingFactIds, List<FactText> facts, List<CitationResponse> citations) {
        if (rootCause == null || rootCause.isBlank()) return false;
        Set<String> ids = supportingFactIds == null ? Set.of() : new LinkedHashSet<>(supportingFactIds);
        boolean factSupport = facts != null && facts.stream()
                .filter(fact -> ids.contains(fact.id()))
                .anyMatch(fact -> sharesMeaningfulTerm(rootCause, fact.statement()));
        boolean citationSupport = citations != null && citations.stream()
                .anyMatch(citation -> sharesMeaningfulTerm(rootCause, citation.excerpt()));
        return factSupport || citationSupport;
    }

    /** Checks whether an intervention addresses the root cause and is supported by retrieved evidence. */
    public boolean supportsRecommendation(String intervention, String rootCause, List<CitationResponse> citations) {
        if (intervention == null || intervention.isBlank() || rootCause == null || rootCause.isBlank()) return false;
        boolean causeAlignment = sharesMeaningfulTerm(intervention, rootCause);
        boolean rootCauseEvidence = citations != null && citations.stream()
                .anyMatch(citation -> sharesMeaningfulTerm(rootCause, citation.excerpt()));
        boolean evidenceAlignment = citations != null && citations.stream()
                .anyMatch(citation -> sharesMeaningfulTerm(intervention, citation.excerpt()));
        return rootCauseEvidence && (causeAlignment || evidenceAlignment);
    }

    /** Provides a small immutable fact projection without coupling this policy to response DTOs. */
    public record FactText(String id, String statement) {}

    private boolean sharesMeaningfulTerm(String left, String right) {
        Set<String> leftTokens = tokens(left);
        Set<String> rightTokens = tokens(right);
        if (leftTokens.isEmpty() || rightTokens.isEmpty()) return false;
        long overlap = leftTokens.stream().filter(rightTokens::contains).count();
        return overlap >= 2 || (overlap == 1 && leftTokens.size() <= 3 && rightTokens.size() <= 8);
    }

    private Set<String> tokens(String value) {
        Set<String> result = new LinkedHashSet<>();
        for (String raw : value.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
            if (raw.length() <= 2 || STOP_WORDS.contains(raw)) continue;
            String token = raw.endsWith("s") && raw.length() > 4 ? raw.substring(0, raw.length() - 1) : raw;
            if (token.length() > 2 && !STOP_WORDS.contains(token)) result.add(token);
        }
        return result;
    }
}
