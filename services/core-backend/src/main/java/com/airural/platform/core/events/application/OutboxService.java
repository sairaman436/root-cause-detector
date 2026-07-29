/*
 * Purpose: Creates transactional outbox records for domain events.
 * Why it exists: Domain services must publish events atomically with their database transactions.
 * Architecture fit: Application service for reliable event publication.
 */
package com.airural.platform.core.events.application;

import com.airural.platform.core.events.domain.OutboxEventEntity;
import com.airural.platform.core.events.domain.EventLogEntity;
import com.airural.platform.core.events.infrastructure.EventLogRepository;
import com.airural.platform.core.events.infrastructure.OutboxEventRepository;
import com.airural.platform.shared.events.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for creating outbox events. */
@Service
public class OutboxService {
    private final OutboxEventRepository outboxRepository;
    private final EventLogRepository eventLogRepository;
    private final EventPayloadMapper payloadMapper;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxEventRepository outboxRepository, EventLogRepository eventLogRepository, EventPayloadMapper payloadMapper, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.eventLogRepository = eventLogRepository;
        this.payloadMapper = payloadMapper;
        this.objectMapper = objectMapper;
    }

    /** Enqueues a versioned platform event in the current transaction. */
    @Transactional
    public UUID enqueue(
            EventTopic topic,
            String aggregateType,
            UUID aggregateId,
            UUID organizationId,
            UUID actorUserId,
            Object payload) {
        UUID eventId = UUID.randomUUID();
        PlatformEvent event = new PlatformEvent(
                eventId,
                topic.name(),
                PlatformEvent.CURRENT_SCHEMA_VERSION,
                topic.topicName(),
                aggregateType,
                aggregateId,
                organizationId,
                actorUserId,
                eventId.toString(),
                Instant.now(),
                payloadMapper.toMap(payload));
        try {
            OutboxEventEntity outbox = outboxRepository.save(new OutboxEventEntity(
                    event.eventId(),
                    event.eventType(),
                    event.schemaVersion(),
                    event.topic(),
                    event.aggregateType(),
                    event.aggregateId(),
                    event.organizationId(),
                    event.actorUserId(),
                    event.correlationId(),
                    event.producerService(),
                    event.traceId(),
                    objectMapper.writeValueAsString(event.payload()),
                    objectMapper.writeValueAsString(event.metadata()),
                    event.occurredAt()));
            eventLogRepository.save(new EventLogEntity(outbox));
            return eventId;
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not serialize event payload", ex);
        }
    }
}
