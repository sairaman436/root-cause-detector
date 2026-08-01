/*
 * Purpose: Persists quality engine outputs for datasets.
 * Why it exists: Dataset release gates need evidence of duplicates, PII masking, validation, and approval readiness.
 * Architecture fit: Infrastructure adapter for dataset quality governance.
 */
package com.airural.platform.core.datasets.infrastructure;

import com.airural.platform.core.datasets.domain.DatasetQualityEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for dataset quality reports. */
public interface DatasetQualityRepository extends JpaRepository<DatasetQualityEntity, UUID> {
    Optional<DatasetQualityEntity> findTopByDatasetIdOrderByCreatedAtDesc(UUID datasetId);
}
