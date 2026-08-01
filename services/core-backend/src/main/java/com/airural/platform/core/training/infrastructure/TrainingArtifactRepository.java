/*
 * Purpose: Persists training artifact metadata.
 * Why it exists: Artifact tracking and integrity status must be durable across runs.
 * Architecture fit: Infrastructure adapter for AI-3 artifact store.
 */
package com.airural.platform.core.training.infrastructure;

import com.airural.platform.core.training.domain.TrainingArtifactEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for training artifacts. */
public interface TrainingArtifactRepository extends JpaRepository<TrainingArtifactEntity, UUID> {
}
