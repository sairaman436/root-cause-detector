/*
 * Purpose: Provides persistence access to decision records.
 * Why it exists: APIs need decision history and lookup.
 * Architecture fit: Repository adapter for the decision aggregate.
 */
package com.airural.platform.core.decision.infrastructure;

import com.airural.platform.core.decision.domain.DecisionEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for decisions. */
public interface DecisionRepository extends JpaRepository<DecisionEntity, UUID> {}
