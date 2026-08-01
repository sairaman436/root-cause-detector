/*
 * Purpose: Persists post-training evaluation metrics.
 * Why it exists: AI-4 release review needs durable reasoning, safety, citation, policy, formatting, latency, and memory scores.
 * Architecture fit: Infrastructure adapter for fine-tuning evaluation gates.
 */
package com.airural.platform.core.finetuning.infrastructure;

import com.airural.platform.core.finetuning.domain.EvaluationMetricsEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for evaluation metrics. */
public interface EvaluationMetricsRepository extends JpaRepository<EvaluationMetricsEntity, UUID> {
}
