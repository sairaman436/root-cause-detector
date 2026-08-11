/*
 * Purpose: JPA repository for pilot evaluation runs.
 */
package com.airural.platform.core.evaluation.infrastructure;

import com.airural.platform.core.evaluation.domain.PilotRunEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PilotRunRepository extends JpaRepository<PilotRunEntity, UUID> {
    Page<PilotRunEntity> findByDatasetId(UUID datasetId, Pageable pageable);
    List<PilotRunEntity> findByDatasetIdOrderByCreatedAtDesc(UUID datasetId);
    List<PilotRunEntity> findByDatasetIdAndRunLabel(UUID datasetId, String runLabel);
}
