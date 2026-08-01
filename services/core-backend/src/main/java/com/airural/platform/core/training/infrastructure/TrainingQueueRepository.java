/*
 * Purpose: Persists training queue entries.
 * Why it exists: Queue scheduling needs durable priority and retry state.
 * Architecture fit: Infrastructure adapter for AI-3 training queue.
 */
package com.airural.platform.core.training.infrastructure;

import com.airural.platform.core.training.domain.TrainingQueueEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for training queue records. */
public interface TrainingQueueRepository extends JpaRepository<TrainingQueueEntity, UUID> {
}
