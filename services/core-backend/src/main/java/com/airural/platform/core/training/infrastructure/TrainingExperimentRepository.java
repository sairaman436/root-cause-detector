/*
 * Purpose: Persists experiment registry records.
 * Why it exists: Experiment metadata and comparison views require queryable experiment state.
 * Architecture fit: Infrastructure adapter for AI-3 experiment tracking.
 */
package com.airural.platform.core.training.infrastructure;

import com.airural.platform.core.training.domain.TrainingExperimentEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for training experiments. */
public interface TrainingExperimentRepository extends JpaRepository<TrainingExperimentEntity, UUID> {
}
