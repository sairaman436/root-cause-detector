/*
 * Purpose: Provides persistence access to analytical event records.
 * Why it exists: Event ingestion needs idempotent normalized records for future warehouse and feature-store pipelines.
 * Architecture fit: Infrastructure repository for event-derived analytical records.
 */
package com.airural.platform.core.events.infrastructure;

import com.airural.platform.core.events.domain.AnalyticsEventRecordEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for analytical event records. */
public interface AnalyticsEventRecordRepository extends JpaRepository<AnalyticsEventRecordEntity, UUID> {
    boolean existsByEventId(UUID eventId);
}
