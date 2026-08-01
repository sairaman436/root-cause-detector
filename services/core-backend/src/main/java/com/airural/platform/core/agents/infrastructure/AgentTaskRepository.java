/*
 * Purpose: Provides persistence access to routed agent tasks.
 * Why it exists: Task browsing, retry, and traceability depend on durable tasks.
 * Architecture fit: Repository adapter for planner output.
 */
package com.airural.platform.core.agents.infrastructure;

import com.airural.platform.core.agents.domain.AgentTaskEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for agent tasks. */
public interface AgentTaskRepository extends JpaRepository<AgentTaskEntity, UUID> {
    List<AgentTaskEntity> findByExecutionIdOrderByPriorityAsc(UUID executionId);
}
