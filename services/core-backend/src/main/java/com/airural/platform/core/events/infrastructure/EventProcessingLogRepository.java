/*
 * Purpose: Provides persistence access to event processing logs.
 * Why it exists: Consumers need durable idempotency and observability records.
 * Architecture fit: Infrastructure repository for event consumer processing state.
 */
package com.airural.platform.core.events.infrastructure;

import com.airural.platform.core.events.domain.EventProcessingLogEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for event processing logs. */
public interface EventProcessingLogRepository extends JpaRepository<EventProcessingLogEntity, UUID> {
    boolean existsByEventIdAndConsumerName(UUID eventId, String consumerName);
}
