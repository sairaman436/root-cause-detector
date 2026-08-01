/*
 * Purpose: Persists knowledge dataset versions.
 * Why it exists: RAG index rebuilds and rollback require immutable version records.
 * Architecture fit: Infrastructure adapter for AI-2 version tracking.
 */
package com.airural.platform.core.knowledge.infrastructure;

import com.airural.platform.core.knowledge.domain.KnowledgeVersionEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for knowledge versions. */
public interface KnowledgeVersionRepository extends JpaRepository<KnowledgeVersionEntity, UUID> {
}
