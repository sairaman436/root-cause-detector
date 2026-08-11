/*
 * Purpose: JPA repository for pilot evaluation datasets.
 */
package com.airural.platform.core.evaluation.infrastructure;

import com.airural.platform.core.evaluation.domain.PilotDatasetEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PilotDatasetRepository extends JpaRepository<PilotDatasetEntity, UUID> {
    Optional<PilotDatasetEntity> findByDatasetKey(String datasetKey);
}
