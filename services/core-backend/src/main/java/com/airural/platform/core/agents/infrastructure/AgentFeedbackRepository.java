/*
 * Purpose: Provides persistence access to agent feedback records.
 * Why it exists: Human approval and evaluation workflows require feedback lookup.
 * Architecture fit: Repository adapter for agent evaluation records.
 */
package com.airural.platform.core.agents.infrastructure;

import com.airural.platform.core.agents.domain.AgentFeedbackEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for agent feedback. */
public interface AgentFeedbackRepository extends JpaRepository<AgentFeedbackEntity, UUID> {
}
