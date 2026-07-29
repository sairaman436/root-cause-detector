/*
 * Purpose: Maps eventing entities to API DTOs.
 * Why it exists: REST contracts must remain decoupled from persistence entities.
 * Architecture fit: Application mapper for Milestone 7 event operations.
 */
package com.airural.platform.core.events.application;

import com.airural.platform.core.events.domain.*;
import com.airural.platform.core.events.web.dto.EventDtos.*;
import org.springframework.stereotype.Component;

/** Maps eventing entities into API responses. */
@Component
public class EventMapper {
    public EventResponse event(EventLogEntity entity) {
        return new EventResponse(entity.id(), entity.eventId(), entity.eventType(), entity.topic(), entity.aggregateType(), entity.aggregateId(), entity.organizationId(), entity.actorUserId(), entity.producerService(), entity.correlationId(), entity.traceId(), entity.schemaVersion(), entity.payloadJson(), entity.metadataJson(), entity.occurredAt(), entity.loggedAt());
    }

    public OutboxResponse outbox(OutboxEventEntity entity) {
        return new OutboxResponse(entity.id(), entity.eventType(), entity.topic(), entity.aggregateType(), entity.aggregateId(), entity.organizationId(), entity.actorUserId(), entity.producerService(), entity.correlationId(), entity.traceId(), entity.status(), entity.attempts(), entity.nextAttemptAt(), entity.lastError(), entity.occurredAt(), entity.publishedAt(), entity.createdAt(), entity.updatedAt());
    }

    public DeadLetterResponse deadLetter(DeadLetterEventEntity entity) {
        return new DeadLetterResponse(entity.id(), entity.eventId(), entity.eventType(), entity.topic(), entity.aggregateType(), entity.aggregateId(), entity.payloadJson(), entity.errorMessage(), entity.attempts(), entity.failedAt(), entity.replayedAt());
    }

    public EventSubscriptionResponse subscription(EventSubscriptionEntity entity) {
        return new EventSubscriptionResponse(entity.id(), entity.consumerName(), entity.topic(), entity.status(), entity.description(), entity.createdAt(), entity.updatedAt());
    }
}
