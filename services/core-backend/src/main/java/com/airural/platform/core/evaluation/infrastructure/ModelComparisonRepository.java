/*
 * Purpose: Persists model comparison results.
 * Why it exists: Promotion decisions require comparisons against base, previous, production, and experimental adapters.
 * Architecture fit: Infrastructure adapter for AI-5 model comparison.
 */
package com.airural.platform.core.evaluation.infrastructure;

import com.airural.platform.core.evaluation.domain.ModelComparisonEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for model comparisons. */
public interface ModelComparisonRepository extends JpaRepository<ModelComparisonEntity, UUID> {
}
