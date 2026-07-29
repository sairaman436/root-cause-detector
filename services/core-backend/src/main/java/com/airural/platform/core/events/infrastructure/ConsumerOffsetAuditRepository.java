/*
 * Purpose: Provides persistence access to consumer offset audit records.
 * Why it exists: Consumer lag and offset snapshots need durable operational history.
 * Architecture fit: Infrastructure repository for event observability.
 */
package com.airural.platform.core.events.infrastructure;

import com.airural.platform.core.events.domain.ConsumerOffsetAuditEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for consumer offset audits. */
public interface ConsumerOffsetAuditRepository extends JpaRepository<ConsumerOffsetAuditEntity, UUID> {
}
