/*
 * Purpose: Persists synthetic dataset generation metadata.
 * Why it exists: Synthetic samples must be explicitly labeled and traceable to their generation rationale.
 * Architecture fit: Infrastructure adapter for governed synthetic data workflows.
 */
package com.airural.platform.core.datasets.infrastructure;

import com.airural.platform.core.datasets.domain.SyntheticDatasetEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for synthetic dataset runs. */
public interface SyntheticDatasetRepository extends JpaRepository<SyntheticDatasetEntity, UUID> {
}
