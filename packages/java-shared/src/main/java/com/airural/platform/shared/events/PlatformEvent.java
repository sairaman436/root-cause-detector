/*
 * Purpose: Defines the immutable event envelope shared by all platform producers and consumers.
 * Why it exists: Event consumers need versioned, traceable, backward-compatible metadata around module-specific payloads.
 * Architecture fit: Shared Java event contract for Kafka and outbox records.
 */
package com.airural.platform.shared.events;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** Immutable platform event envelope. */
public record PlatformEvent(
        UUID eventId,
        String eventType,
        UUID aggregateId,
        String aggregateType,
        Instant timestamp,
        String producerService,
        String correlationId,
        String traceId,
        UUID userId,
        int version,
        Map<String, Object> payload,
        Map<String, Object> metadata,
        int schemaVersion,
        String topic,
        UUID organizationId,
        UUID actorUserId,
        Instant occurredAt) {

    public static final int CURRENT_SCHEMA_VERSION = 1;

    public PlatformEvent(
            UUID eventId,
            String eventType,
            int schemaVersion,
            String topic,
            String aggregateType,
            UUID aggregateId,
            UUID organizationId,
            UUID actorUserId,
            String correlationId,
            Instant occurredAt,
            Map<String, Object> payload) {
        this(
                eventId,
                eventType,
                aggregateId,
                aggregateType,
                occurredAt,
                "core-backend",
                correlationId,
                correlationId,
                actorUserId,
                schemaVersion,
                payload,
                Map.of(),
                schemaVersion,
                topic,
                organizationId,
                actorUserId,
                occurredAt);
    }
}
