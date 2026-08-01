/*
 * Purpose: Persists training metrics for dashboards.
 * Why it exists: GPU utilization, VRAM, throughput, loss curves, and cost tracking require queryable metrics.
 * Architecture fit: Infrastructure adapter for AI-3 observability.
 */
package com.airural.platform.core.training.infrastructure;

import com.airural.platform.core.training.domain.TrainingMetricsEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for training metrics. */
public interface TrainingMetricsRepository extends JpaRepository<TrainingMetricsEntity, UUID> {
}
