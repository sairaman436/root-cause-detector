/*
 * Purpose: Persists knowledge classification labels.
 * Why it exists: Multi-label classification supports retrieval, coverage scoring, and dataset slicing.
 * Architecture fit: Infrastructure adapter for AI-2 classification.
 */
package com.airural.platform.core.knowledge.infrastructure;

import com.airural.platform.core.knowledge.domain.KnowledgeClassificationEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for knowledge classifications. */
public interface KnowledgeClassificationRepository extends JpaRepository<KnowledgeClassificationEntity, UUID> {
}
