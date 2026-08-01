/*
 * Purpose: Provides persistence access to MCP-style tool definitions.
 * Why it exists: Tool discovery and permissions require durable metadata.
 * Architecture fit: Repository adapter for the tool registry.
 */
package com.airural.platform.core.agents.infrastructure;

import com.airural.platform.core.agents.domain.ToolDefinitionEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for tool definitions. */
public interface ToolDefinitionRepository extends JpaRepository<ToolDefinitionEntity, UUID> {
    Optional<ToolDefinitionEntity> findByToolKey(String toolKey);
}
