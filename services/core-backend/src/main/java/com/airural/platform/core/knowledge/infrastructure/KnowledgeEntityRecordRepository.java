/*
 * Purpose: Persists extracted knowledge entity records.
 * Why it exists: Entity extraction enables search facets, knowledge graph assembly, and retrieval filters.
 * Architecture fit: Infrastructure adapter for AI-2 knowledge enrichment.
 */
package com.airural.platform.core.knowledge.infrastructure;

import com.airural.platform.core.knowledge.domain.KnowledgeEntityRecordEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for extracted knowledge entities. */
public interface KnowledgeEntityRecordRepository extends JpaRepository<KnowledgeEntityRecordEntity, UUID> {
}
