/*
 * Purpose: Persists normalized, validated, and synthetic dataset samples.
 * Why it exists: Dataset quality, export, and approval workflows depend on sample-level state.
 * Architecture fit: Infrastructure adapter for the dataset processing pipeline.
 */
package com.airural.platform.core.datasets.infrastructure;

import com.airural.platform.core.datasets.domain.DatasetSampleEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for dataset samples. */
public interface DatasetSampleRepository extends JpaRepository<DatasetSampleEntity, UUID> {
    long countByDatasetId(UUID datasetId);

    long countByDatasetIdAndSyntheticTrue(UUID datasetId);

    boolean existsByFingerprint(String fingerprint);

    List<DatasetSampleEntity> findByDatasetId(UUID datasetId);
}
