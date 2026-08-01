/*
 * Purpose: Persists immutable dataset version records.
 * Why it exists: Dataset exports and approval gates require reproducible version history.
 * Architecture fit: Infrastructure adapter for dataset registry versioning.
 */
package com.airural.platform.core.datasets.infrastructure;

import com.airural.platform.core.datasets.domain.DatasetVersionEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for dataset versions. */
public interface DatasetVersionRepository extends JpaRepository<DatasetVersionEntity, UUID> {
}
