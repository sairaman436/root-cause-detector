/*
 * Purpose: Persists crawler connector configuration.
 * Why it exists: Scheduled and incremental acquisition need resumable connector state.
 * Architecture fit: Infrastructure adapter for AI-2 crawler registry.
 */
package com.airural.platform.core.knowledge.infrastructure;

import com.airural.platform.core.knowledge.domain.KnowledgeCrawlerEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for knowledge crawlers. */
public interface KnowledgeCrawlerRepository extends JpaRepository<KnowledgeCrawlerEntity, UUID> {
}
