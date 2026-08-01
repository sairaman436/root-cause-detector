/*
 * Purpose: Provides persistence access to agent audit records.
 * Why it exists: Governance and compliance need durable audit trails.
 * Architecture fit: Repository adapter for multi-agent audit events.
 */
package com.airural.platform.core.agents.infrastructure;

import com.airural.platform.core.agents.domain.AgentAuditEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for agent audit events. */
public interface AgentAuditRepository extends JpaRepository<AgentAuditEntity, UUID> {
}
