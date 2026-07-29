/*
 * Purpose: Defines REST DTOs for evidence APIs.
 * Why it exists: Controllers need stable request and response contracts independent of JPA entities.
 * Architecture fit: API contract boundary for the Evidence and Asset Management module.
 */
package com.airural.platform.core.evidence.web.dto;

import com.airural.platform.core.evidence.domain.*;
import com.airural.platform.core.identity.domain.AuditOutcome;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/** Namespace for evidence API DTO records. */
public final class EvidenceDtos {
    private EvidenceDtos() {
    }

    /** Request for updating editable evidence metadata. */
    public record UpdateEvidenceMetadataRequest(
            @Size(max = 220) String title,
            @Size(max = 1000) String description,
            String customMetadataJson,
            Set<String> tags) {
    }

    /** Response for evidence metadata and lifecycle state. */
    public record EvidenceResponse(
            UUID id,
            UUID organizationId,
            UUID surveyId,
            UUID questionId,
            UUID uploadedByUserId,
            EvidenceType evidenceType,
            String originalFileName,
            String storedFileName,
            String mimeType,
            Long sizeBytes,
            String sha256Checksum,
            StorageProvider storageProvider,
            String storageKey,
            Integer currentVersion,
            String title,
            String description,
            String customMetadataJson,
            Set<String> tags,
            boolean active,
            Instant deletedAt,
            Instant createdAt,
            Instant updatedAt) {
    }

    /** Response for evidence version history. */
    public record EvidenceVersionResponse(
            UUID id,
            UUID evidenceId,
            Integer versionNumber,
            String originalFileName,
            String mimeType,
            Long sizeBytes,
            String sha256Checksum,
            StorageProvider storageProvider,
            String storageKey,
            String metadataSnapshotJson,
            UUID createdByUserId,
            Instant createdAt) {
    }

    /** Response for evidence audit history. */
    public record EvidenceAuditResponse(
            UUID id,
            UUID evidenceId,
            UUID actorUserId,
            EvidenceAuditAction action,
            AuditOutcome outcome,
            String details,
            Instant createdAt) {
    }

    /** Response for future-ready signed URL requests. */
    public record SignedUrlResponse(UUID evidenceId, String url, Instant expiresAt, boolean providerSignedUrlSupported) {
    }
}
