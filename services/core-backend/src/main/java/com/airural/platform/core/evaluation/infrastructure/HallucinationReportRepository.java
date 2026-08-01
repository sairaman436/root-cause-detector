/*
 * Purpose: Persists hallucination reports.
 * Why it exists: Factuality gates need durable unsupported-claim and hallucination evidence.
 * Architecture fit: Infrastructure adapter for AI-5 hallucination reporting.
 */
package com.airural.platform.core.evaluation.infrastructure;

import com.airural.platform.core.evaluation.domain.HallucinationReportEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for hallucination reports. */
public interface HallucinationReportRepository extends JpaRepository<HallucinationReportEntity, UUID> {
}
