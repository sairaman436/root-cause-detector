/*
 * Purpose: Persists citation verification reports.
 * Why it exists: Citation accuracy, relevance, broken citation, and unsupported claim checks must be queryable.
 * Architecture fit: Infrastructure adapter for AI-5 citation evaluation.
 */
package com.airural.platform.core.evaluation.infrastructure;

import com.airural.platform.core.evaluation.domain.CitationReportEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for citation reports. */
public interface CitationReportRepository extends JpaRepository<CitationReportEntity, UUID> {
}
