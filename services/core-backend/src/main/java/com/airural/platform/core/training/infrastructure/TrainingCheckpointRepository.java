/*
 * Purpose: Persists checkpoint metadata.
 * Why it exists: Checkpoint restore, validation, cleanup, and comparison need durable metadata.
 * Architecture fit: Infrastructure adapter for AI-3 checkpoint management.
 */
package com.airural.platform.core.training.infrastructure;

import com.airural.platform.core.training.domain.TrainingCheckpointEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for training checkpoints. */
public interface TrainingCheckpointRepository extends JpaRepository<TrainingCheckpointEntity, UUID> {
    List<TrainingCheckpointEntity> findByJobId(UUID jobId);
}
