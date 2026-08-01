/*
 * Purpose: Persists dataset lineage edges.
 * Why it exists: Dataset consumers need source, transformation, and derived artifact traceability.
 * Architecture fit: Infrastructure adapter for enterprise data lineage.
 */
package com.airural.platform.core.datasets.infrastructure;

import com.airural.platform.core.datasets.domain.DatasetLineageEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for dataset lineage records. */
public interface DatasetLineageRepository extends JpaRepository<DatasetLineageEntity, UUID> {
}
