/*
 * Purpose: Provides persistence access to hypotheses.
 * Why it exists: Alternative explanations and ranking need durable hypothesis records.
 * Architecture fit: Repository adapter for hypothesis records.
 */
package com.airural.platform.core.decision.infrastructure;

import com.airural.platform.core.decision.domain.HypothesisEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for hypotheses. */
public interface HypothesisRepository extends JpaRepository<HypothesisEntity, UUID> {
    List<HypothesisEntity> findByDecisionIdOrderByRankAsc(UUID decisionId);
}
