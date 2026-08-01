/*
 * Purpose: Persists optimized artifact metadata.
 * Why it exists: Artifact registry reads and release checks need durable optimized artifact state.
 * Architecture fit: JPA infrastructure adapter for optimization artifacts.
 */
package com.airural.platform.core.optimization.infrastructure;

import com.airural.platform.core.optimization.domain.OptimizationArtifactEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for optimization artifacts. */
public interface OptimizationArtifactRepository extends JpaRepository<OptimizationArtifactEntity, UUID> {
    List<OptimizationArtifactEntity> findByOptimizationRunId(UUID optimizationRunId);
}
