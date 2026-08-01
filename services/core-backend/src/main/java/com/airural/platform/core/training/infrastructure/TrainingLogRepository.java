/*
 * Purpose: Persists structured training logs.
 * Why it exists: Scheduler, security, dataset, and checkpoint decisions must be auditable.
 * Architecture fit: Infrastructure adapter for AI-3 training logs.
 */
package com.airural.platform.core.training.infrastructure;

import com.airural.platform.core.training.domain.TrainingLogEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for training logs. */
public interface TrainingLogRepository extends JpaRepository<TrainingLogEntity, UUID> {
}
