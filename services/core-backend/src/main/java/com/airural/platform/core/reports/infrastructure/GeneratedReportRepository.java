/*
 * Purpose: Provides persistence access to generated reports.
 * Why it exists: Report APIs need lookup, listing, and decision-linked retrieval.
 * Architecture fit: Repository adapter for the Reports bounded context.
 */
package com.airural.platform.core.reports.infrastructure;

import com.airural.platform.core.reports.domain.GeneratedReportEntity;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for generated reports. */
public interface GeneratedReportRepository extends JpaRepository<GeneratedReportEntity, UUID> {
    Page<GeneratedReportEntity> findByDeletedAtIsNull(Pageable pageable);
    List<GeneratedReportEntity> findByDecisionIdAndDeletedAtIsNullOrderByGeneratedAtDesc(UUID decisionId);
}
