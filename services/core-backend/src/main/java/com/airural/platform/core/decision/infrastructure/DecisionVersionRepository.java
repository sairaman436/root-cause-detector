/*
 * Purpose: Provides persistence access to decision versions.
 * Why it exists: Decision memory requires immutable version history.
 * Architecture fit: Repository adapter for decision version records.
 */
package com.airural.platform.core.decision.infrastructure;

import com.airural.platform.core.decision.domain.DecisionVersionEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for decision versions. */
public interface DecisionVersionRepository extends JpaRepository<DecisionVersionEntity, UUID> {}
