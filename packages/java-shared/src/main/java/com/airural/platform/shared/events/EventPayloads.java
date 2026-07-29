/*
 * Purpose: Defines stable v1 payload shapes for platform domain events.
 * Why it exists: Event contracts must be versioned and shared outside producer implementation classes.
 * Architecture fit: Shared package for backward-compatible event schemas.
 */
package com.airural.platform.shared.events;

import java.time.Instant;
import java.util.UUID;

/** Namespace for v1 event payload contracts. */
public final class EventPayloads {
    private EventPayloads() {
    }

    public record SurveyPayload(UUID surveyId, UUID organizationId, String name, String status, Integer version, Instant updatedAt) {
    }

    public record EvidencePayload(UUID evidenceId, UUID organizationId, UUID surveyId, UUID questionId, String evidenceType, String mimeType, Long sizeBytes, String checksum, Instant updatedAt) {
    }

    public record UserPayload(UUID userId, UUID organizationId, String username, String email, String status, Instant updatedAt) {
    }

    public record GeographyPayload(UUID entityId, UUID organizationId, String entityType, String name, String code, Instant updatedAt) {
    }

    public record AuditPayload(UUID auditEventId, UUID actorUserId, String eventType, String outcome, Instant createdAt) {
    }

    public record KnowledgeDocumentPayload(UUID documentId, UUID organizationId, String title, String status, Instant updatedAt) {
    }

    public record NotificationPayload(UUID notificationId, UUID organizationId, String channel, String status, Instant createdAt) {
    }
}
