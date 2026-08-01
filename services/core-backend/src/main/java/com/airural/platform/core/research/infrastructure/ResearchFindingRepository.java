/*
 * Purpose: Persists research findings.
 * Why it exists: Findings need evidence, confidence, and replication status for scientific governance.
 * Architecture fit: JPA adapter for Research-1 finding registry.
 */
package com.airural.platform.core.research.infrastructure;

import com.airural.platform.core.research.domain.ResearchFindingEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for research findings. */
public interface ResearchFindingRepository extends JpaRepository<ResearchFindingEntity, UUID> {
    List<ResearchFindingEntity> findTop20ByOrderByCreatedAtDesc();
}
