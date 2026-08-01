/*
 * Purpose: Persists governed AI agent definitions.
 * Why it exists: Agent autonomy and tool permissions need durable governance metadata.
 * Architecture fit: JPA adapter for AI-9 agent governance.
 */
package com.airural.platform.core.governance.infrastructure;

import com.airural.platform.core.governance.domain.AgentRegistryEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for agent registry records. */
public interface AgentRegistryRepository extends JpaRepository<AgentRegistryEntity, UUID> {}
