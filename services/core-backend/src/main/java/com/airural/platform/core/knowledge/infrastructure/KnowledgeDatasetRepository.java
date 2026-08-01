/*
 * Purpose: Persists knowledge dataset registry records.
 * Why it exists: Acquired documents must be grouped into governed corpora for RAG and evaluation.
 * Architecture fit: Infrastructure adapter for AI-2 dataset registry.
 */
package com.airural.platform.core.knowledge.infrastructure;

import com.airural.platform.core.knowledge.domain.KnowledgeDatasetEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for knowledge datasets. */
public interface KnowledgeDatasetRepository extends JpaRepository<KnowledgeDatasetEntity, UUID> {
}
