/*
 * Purpose: Provides persistence access to immutable event log records.
 * Why it exists: Event APIs need searchable event history independent of Kafka retention.
 * Architecture fit: Infrastructure repository for the Eventing module.
 */
package com.airural.platform.core.events.infrastructure;

import com.airural.platform.core.events.domain.EventLogEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Spring Data repository for event logs. */
public interface EventLogRepository extends JpaRepository<EventLogEntity, UUID>, JpaSpecificationExecutor<EventLogEntity> {
    Optional<EventLogEntity> findByEventId(UUID eventId);
    boolean existsByEventId(UUID eventId);
}
