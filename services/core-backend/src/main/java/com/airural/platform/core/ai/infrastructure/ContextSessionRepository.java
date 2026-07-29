/*
 * Purpose: Provides persistence access to context sessions.
 * Why it exists: Chat and RAG calls require context continuity and governance.
 * Architecture fit: Repository adapter for AI context management.
 */
package com.airural.platform.core.ai.infrastructure;

import com.airural.platform.core.ai.domain.ContextSessionEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for context sessions. */
public interface ContextSessionRepository extends JpaRepository<ContextSessionEntity, UUID> {
}
