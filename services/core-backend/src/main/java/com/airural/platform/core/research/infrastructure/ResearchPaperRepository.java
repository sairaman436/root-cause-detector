/*
 * Purpose: Persists research paper records.
 * Why it exists: Paper discovery and review require a durable registry.
 * Architecture fit: JPA adapter for Research-1 paper registry.
 */
package com.airural.platform.core.research.infrastructure;

import com.airural.platform.core.research.domain.ResearchPaperEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for research papers. */
public interface ResearchPaperRepository extends JpaRepository<ResearchPaperEntity, UUID> {}
