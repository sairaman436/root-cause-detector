/*
 * Purpose: Persists scheduled training run records.
 * Why it exists: Run state, resume readiness, and scheduler decisions must be tracked separately from job requests.
 * Architecture fit: Infrastructure adapter for AI-3 scheduler records.
 */
package com.airural.platform.core.training.infrastructure;

import com.airural.platform.core.training.domain.TrainingRunEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for training runs. */
public interface TrainingRunRepository extends JpaRepository<TrainingRunEntity, UUID> {
}
