/*
 * Purpose: Persists trust and freshness quality scores.
 * Why it exists: Source trust decisions must be auditable for enterprise AI governance.
 * Architecture fit: Infrastructure adapter for AI-2 quality scoring.
 */
package com.airural.platform.core.knowledge.infrastructure;

import com.airural.platform.core.knowledge.domain.KnowledgeTrustEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for knowledge trust scores. */
public interface KnowledgeTrustRepository extends JpaRepository<KnowledgeTrustEntity, UUID> {
}
