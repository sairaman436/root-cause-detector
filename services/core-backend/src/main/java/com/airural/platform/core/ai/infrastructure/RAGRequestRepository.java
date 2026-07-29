/*
 * Purpose: Provides persistence access to RAG request records.
 * Why it exists: RAG requests must be auditable and measurable.
 * Architecture fit: Repository adapter for RAG pipeline governance.
 */
package com.airural.platform.core.ai.infrastructure;

import com.airural.platform.core.ai.domain.RAGRequestEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for RAG requests. */
public interface RAGRequestRepository extends JpaRepository<RAGRequestEntity, UUID> {
}
