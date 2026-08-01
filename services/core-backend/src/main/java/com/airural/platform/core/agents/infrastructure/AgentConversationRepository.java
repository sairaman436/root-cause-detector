/*
 * Purpose: Provides persistence access to agent conversations.
 * Why it exists: Conversation history and context need durable storage.
 * Architecture fit: Repository adapter for conversation memory.
 */
package com.airural.platform.core.agents.infrastructure;

import com.airural.platform.core.agents.domain.AgentConversationEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for agent conversations. */
public interface AgentConversationRepository extends JpaRepository<AgentConversationEntity, UUID> {
}
