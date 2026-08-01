/*
 * Purpose: Provides persistence access to agent executions.
 * Why it exists: History, observability, and feedback workflows need execution lookup.
 * Architecture fit: Repository adapter for agent execution records.
 */
package com.airural.platform.core.agents.infrastructure;

import com.airural.platform.core.agents.domain.AgentExecutionEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for agent executions. */
public interface AgentExecutionRepository extends JpaRepository<AgentExecutionEntity, UUID> {
}
