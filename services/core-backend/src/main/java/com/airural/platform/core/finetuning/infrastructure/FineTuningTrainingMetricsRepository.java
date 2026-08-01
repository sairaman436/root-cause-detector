/*
 * Purpose: Persists fine-tuning training metrics.
 * Why it exists: Loss, validation loss, learning rate, GPU, VRAM, checkpoints, and timing must be queryable.
 * Architecture fit: Infrastructure adapter for AI-4 observability metrics.
 */
package com.airural.platform.core.finetuning.infrastructure;

import com.airural.platform.core.finetuning.domain.FineTuningTrainingMetricsEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for fine-tuning training metrics. */
public interface FineTuningTrainingMetricsRepository extends JpaRepository<FineTuningTrainingMetricsEntity, UUID> {
}
