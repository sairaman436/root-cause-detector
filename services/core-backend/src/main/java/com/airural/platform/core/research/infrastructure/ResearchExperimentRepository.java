/*
 * Purpose: Persists research experiment records.
 * Why it exists: Experiment registry APIs require durable experiment and approval metadata.
 * Architecture fit: JPA adapter for Research-1 experiment registry.
 */
package com.airural.platform.core.research.infrastructure;

import com.airural.platform.core.research.domain.ResearchExperimentEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for research experiments. */
public interface ResearchExperimentRepository extends JpaRepository<ResearchExperimentEntity, UUID> {
    Optional<ResearchExperimentEntity> findByExperimentKey(String experimentKey);
}
