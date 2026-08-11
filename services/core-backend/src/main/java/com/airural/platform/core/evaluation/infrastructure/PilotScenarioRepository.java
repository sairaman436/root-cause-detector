/*
 * Purpose: JPA repository for pilot evaluation scenarios.
 */
package com.airural.platform.core.evaluation.infrastructure;

import com.airural.platform.core.evaluation.domain.PilotScenarioEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PilotScenarioRepository extends JpaRepository<PilotScenarioEntity, UUID> {
    List<PilotScenarioEntity> findByDatasetId(UUID datasetId);
    List<PilotScenarioEntity> findByDatasetIdAndAdversarial(UUID datasetId, boolean adversarial);
    boolean existsByDatasetIdAndScenarioId(UUID datasetId, String scenarioId);
    Optional<PilotScenarioEntity> findByDatasetIdAndScenarioId(UUID datasetId, String scenarioId);
}
