/*
 * Purpose: Persists evaluation metrics.
 * Why it exists: Accuracy, recall, F1, hallucination, citation, latency, VRAM, token, and reasoning metrics must be queryable.
 * Architecture fit: Infrastructure adapter for AI-5 observability.
 */
package com.airural.platform.core.evaluation.infrastructure;

import com.airural.platform.core.evaluation.domain.EvaluationMetricEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for evaluation metrics. */
public interface EvaluationMetricRepository extends JpaRepository<EvaluationMetricEntity, UUID> {
}
