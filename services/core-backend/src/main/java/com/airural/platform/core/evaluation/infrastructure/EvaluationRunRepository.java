/*
 * Purpose: Persists immutable model evaluation runs.
 * Why it exists: Evaluation APIs need durable run history for audit and promotion decisions.
 * Architecture fit: Infrastructure adapter for AI-5 evaluation lifecycle.
 */
package com.airural.platform.core.evaluation.infrastructure;

import com.airural.platform.core.evaluation.domain.EvaluationRunEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for evaluation runs. */
public interface EvaluationRunRepository extends JpaRepository<EvaluationRunEntity, UUID> {
}
