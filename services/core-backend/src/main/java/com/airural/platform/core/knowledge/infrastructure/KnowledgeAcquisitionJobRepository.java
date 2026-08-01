/*
 * Purpose: Persists knowledge acquisition job records.
 * Why it exists: Acquisition, crawl, and reindex workflows require durable job state and observability.
 * Architecture fit: Infrastructure adapter for AI-2 ingestion jobs.
 */
package com.airural.platform.core.knowledge.infrastructure;

import com.airural.platform.core.knowledge.domain.KnowledgeAcquisitionJobEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for knowledge acquisition jobs. */
public interface KnowledgeAcquisitionJobRepository extends JpaRepository<KnowledgeAcquisitionJobEntity, UUID> {
}
