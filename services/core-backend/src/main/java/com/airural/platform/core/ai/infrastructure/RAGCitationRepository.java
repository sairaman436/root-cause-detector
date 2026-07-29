/*
 * Purpose: Provides persistence access to RAG citations.
 * Why it exists: Every generated answer must retain explainable source links.
 * Architecture fit: Repository adapter for citation records.
 */
package com.airural.platform.core.ai.infrastructure;

import com.airural.platform.core.ai.domain.RAGCitationEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for RAG citations. */
public interface RAGCitationRepository extends JpaRepository<RAGCitationEntity, UUID> {
    List<RAGCitationEntity> findByRagRequestId(UUID ragRequestId);
}
