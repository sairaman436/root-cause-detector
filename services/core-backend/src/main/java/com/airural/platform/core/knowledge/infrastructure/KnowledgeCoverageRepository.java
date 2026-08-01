/*
 * Purpose: Persists knowledge coverage score records.
 * Why it exists: Coverage APIs need durable quality evidence for corpus gaps.
 * Architecture fit: Infrastructure adapter for AI-2 coverage governance.
 */
package com.airural.platform.core.knowledge.infrastructure;

import com.airural.platform.core.knowledge.domain.KnowledgeCoverageEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for knowledge coverage records. */
public interface KnowledgeCoverageRepository extends JpaRepository<KnowledgeCoverageEntity, UUID> {
}
