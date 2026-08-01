/*
 * Purpose: Persists policy bindings for agents.
 * Why it exists: Agent execution policies and tool permissions need auditable storage.
 * Architecture fit: JPA adapter for AI-9 agent policy enforcement.
 */
package com.airural.platform.core.governance.infrastructure;

import com.airural.platform.core.governance.domain.AgentPolicyEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for agent policies. */
public interface AgentPolicyRepository extends JpaRepository<AgentPolicyEntity, UUID> {}
