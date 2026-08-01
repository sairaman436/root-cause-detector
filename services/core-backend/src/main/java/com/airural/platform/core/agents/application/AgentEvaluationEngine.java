/*
 * Purpose: Evaluates safety and confidence of agent outputs.
 * Why it exists: Hallucination hooks, citation enforcement, and confidence thresholds must run before responses are returned.
 * Architecture fit: Agent safety and evaluation component.
 */
package com.airural.platform.core.agents.application;

import com.airural.platform.core.agents.web.dto.AgentDtos.Citation;
import java.util.List;
import org.springframework.stereotype.Service;

/** Evaluates agent output readiness. */
@Service
public class AgentEvaluationEngine {
    public boolean citationCompliant(List<Citation> citations) {
        return citations != null && !citations.isEmpty();
    }

    public boolean humanApprovalRequired(boolean consequential, double confidence, List<Citation> citations) {
        return consequential || confidence < 0.75 || !citationCompliant(citations);
    }
}
