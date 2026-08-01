/*
 * Purpose: Provides persistence access to agent registry records.
 * Why it exists: Orchestration and discovery need durable agent lookup.
 * Architecture fit: Repository adapter for the agent registry.
 */
package com.airural.platform.core.agents.infrastructure;

import com.airural.platform.core.agents.domain.AgentEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for agents. */
public interface AgentRepository extends JpaRepository<AgentEntity, UUID> {
    Optional<AgentEntity> findByAgentKey(String agentKey);
}
