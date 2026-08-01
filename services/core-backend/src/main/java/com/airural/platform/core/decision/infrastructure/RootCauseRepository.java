/*
 * Purpose: Provides persistence access to ranked root causes.
 * Why it exists: Explanation and root-cause APIs need decision-specific root causes.
 * Architecture fit: Repository adapter for root-cause records.
 */
package com.airural.platform.core.decision.infrastructure;

import com.airural.platform.core.decision.domain.RootCauseEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for root causes. */
public interface RootCauseRepository extends JpaRepository<RootCauseEntity, UUID> {
    List<RootCauseEntity> findByDecisionIdOrderByRankAsc(UUID decisionId);
}
