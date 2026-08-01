/*
 * Purpose: Persists hyperparameter registry records.
 * Why it exists: Reproducible experiments require immutable hyperparameter snapshots.
 * Architecture fit: Infrastructure adapter for AI-3 hyperparameter tracking.
 */
package com.airural.platform.core.training.infrastructure;

import com.airural.platform.core.training.domain.HyperparameterSetEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for hyperparameter sets. */
public interface HyperparameterSetRepository extends JpaRepository<HyperparameterSetEntity, UUID> {
}
