/*
 * Purpose: Persists trusted knowledge sources.
 * Why it exists: Acquisition jobs need source lookup by registry key.
 * Architecture fit: Infrastructure adapter for AI-2 source registry.
 */
package com.airural.platform.core.knowledge.infrastructure;

import com.airural.platform.core.knowledge.domain.KnowledgeSourceEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for knowledge sources. */
public interface KnowledgeSourceRepository extends JpaRepository<KnowledgeSourceEntity, UUID> {
    Optional<KnowledgeSourceEntity> findBySourceKey(String sourceKey);
}
