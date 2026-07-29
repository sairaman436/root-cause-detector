/*
 * Purpose: Defines REST contracts for event streaming operations.
 * Why it exists: Operators need stable DTOs for event browsing, outbox management, dead-letter inspection, and replay.
 * Architecture fit: Web adapter contracts for Milestone 7 Event Streaming and Data Integration.
 */
package com.airural.platform.core.events.web.dto;

import com.airural.platform.core.events.domain.OutboxStatus;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

/** Namespace for event API DTO records. */
public final class EventDtos {
    private EventDtos() {
    }

    /** Event log response. */
    public record EventResponse(UUID id, UUID eventId, String eventType, String topic, String aggregateType, UUID aggregateId, UUID organizationId, UUID actorUserId, String producerService, String correlationId, String traceId, Integer schemaVersion, String payloadJson, String metadataJson, Instant occurredAt, Instant loggedAt) {
    }

    /** Outbox response. */
    public record OutboxResponse(UUID id, String eventType, String topic, String aggregateType, UUID aggregateId, UUID organizationId, UUID actorUserId, String producerService, String correlationId, String traceId, OutboxStatus status, Integer attempts, Instant nextAttemptAt, String lastError, Instant occurredAt, Instant publishedAt, Instant createdAt, Instant updatedAt) {
    }

    /** Dead-letter response. */
    public record DeadLetterResponse(UUID id, UUID eventId, String eventType, String topic, String aggregateType, UUID aggregateId, String payloadJson, String errorMessage, Integer attempts, Instant failedAt, Instant replayedAt) {
    }

    /** Event subscription response. */
    public record EventSubscriptionResponse(UUID id, String consumerName, String topic, String status, String description, Instant createdAt, Instant updatedAt) {
    }

    /** Replay request. */
    public record ReplayEventRequest(@NotNull UUID eventId) {
    }
}
