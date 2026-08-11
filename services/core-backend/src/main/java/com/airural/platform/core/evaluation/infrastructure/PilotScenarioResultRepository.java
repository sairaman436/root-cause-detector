/*
 * Purpose: JPA repository for pilot scenario results.
 */
package com.airural.platform.core.evaluation.infrastructure;

import com.airural.platform.core.evaluation.domain.PilotScenarioResultEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PilotScenarioResultRepository extends JpaRepository<PilotScenarioResultEntity, UUID> {
    List<PilotScenarioResultEntity> findByPilotRunId(UUID pilotRunId);
    List<PilotScenarioResultEntity> findByPilotRunIdAndPass(UUID pilotRunId, boolean pass);
}
