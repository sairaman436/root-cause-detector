/*
 * Purpose: Persists extracted knowledge metadata.
 * Why it exists: Downstream search, RAG, citations, and governance depend on normalized metadata.
 * Architecture fit: Infrastructure adapter for AI-2 metadata extraction.
 */
package com.airural.platform.core.knowledge.infrastructure;

import com.airural.platform.core.knowledge.domain.KnowledgeMetadataEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for knowledge metadata records. */
public interface KnowledgeMetadataRepository extends JpaRepository<KnowledgeMetadataEntity, UUID> {
}
