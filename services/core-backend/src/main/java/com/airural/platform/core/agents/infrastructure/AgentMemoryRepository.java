/*
 * Purpose: Provides persistence access to agent memory records.
 * Why it exists: Memory APIs and context managers need queryable memory state.
 * Architecture fit: Repository adapter for shared agent memory.
 */
package com.airural.platform.core.agents.infrastructure;

import com.airural.platform.core.agents.domain.AgentMemoryEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for agent memory. */
public interface AgentMemoryRepository extends JpaRepository<AgentMemoryEntity, UUID> {
    List<AgentMemoryEntity> findTop20ByConversationIdOrderByCreatedAtDesc(UUID conversationId);
}
