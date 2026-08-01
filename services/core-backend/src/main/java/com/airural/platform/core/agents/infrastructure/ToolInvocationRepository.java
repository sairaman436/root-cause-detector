/*
 * Purpose: Provides persistence access to tool invocation records.
 * Why it exists: Tool usage observability requires persistent invocation telemetry.
 * Architecture fit: Repository adapter for the MCP invocation log.
 */
package com.airural.platform.core.agents.infrastructure;

import com.airural.platform.core.agents.domain.ToolInvocationEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for tool invocations. */
public interface ToolInvocationRepository extends JpaRepository<ToolInvocationEntity, UUID> {
}
