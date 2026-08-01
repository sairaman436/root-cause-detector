/*
 * Purpose: Persists training job manager records.
 * Why it exists: Training job creation, queueing, cancellation, and dashboard APIs require durable state.
 * Architecture fit: Infrastructure adapter for AI-3 training jobs.
 */
package com.airural.platform.core.training.infrastructure;

import com.airural.platform.core.training.domain.TrainingJobEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for training jobs. */
public interface TrainingJobRepository extends JpaRepository<TrainingJobEntity, UUID> {
}
