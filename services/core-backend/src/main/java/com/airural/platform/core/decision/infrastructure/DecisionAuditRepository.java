/*
 * Purpose: Provides persistence access to decision audit records.
 * Why it exists: Decision operations need governance-grade audit trails.
 * Architecture fit: Repository adapter for decision audit events.
 */
package com.airural.platform.core.decision.infrastructure;

import com.airural.platform.core.decision.domain.DecisionAuditEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for decision audits. */
public interface DecisionAuditRepository extends JpaRepository<DecisionAuditEntity, UUID> {}
