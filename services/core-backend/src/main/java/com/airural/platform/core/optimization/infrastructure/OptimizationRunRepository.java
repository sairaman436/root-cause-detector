/*
 * Purpose: Persists optimization run records.
 * Why it exists: AI-6 APIs need pageable run history and release auditability.
 * Architecture fit: JPA infrastructure adapter for optimization aggregates.
 */
package com.airural.platform.core.optimization.infrastructure;

import com.airural.platform.core.optimization.domain.OptimizationRunEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for optimization runs. */
public interface OptimizationRunRepository extends JpaRepository<OptimizationRunEntity, UUID> {}
