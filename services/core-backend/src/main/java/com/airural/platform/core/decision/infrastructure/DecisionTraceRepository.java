/*
 * Purpose: Provides persistence access to decision traces.
 * Why it exists: Explainability APIs need ordered reasoning and confidence evolution steps.
 * Architecture fit: Repository adapter for decision trace records.
 */
package com.airural.platform.core.decision.infrastructure;

import com.airural.platform.core.decision.domain.DecisionTraceEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for decision traces. */
public interface DecisionTraceRepository extends JpaRepository<DecisionTraceEntity, UUID> {
    List<DecisionTraceEntity> findByDecisionIdOrderByStepNumberAsc(UUID decisionId);
}
