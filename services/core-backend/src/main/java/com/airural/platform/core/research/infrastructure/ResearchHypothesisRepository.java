/*
 * Purpose: Persists research hypotheses.
 * Why it exists: Hypothesis tracking links scientific questions to experiments and findings.
 * Architecture fit: JPA adapter for Research-1 hypothesis tracker.
 */
package com.airural.platform.core.research.infrastructure;

import com.airural.platform.core.research.domain.ResearchHypothesisEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for research hypotheses. */
public interface ResearchHypothesisRepository extends JpaRepository<ResearchHypothesisEntity, UUID> {}
