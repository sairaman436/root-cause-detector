/*
 * Purpose: Maps evidence entities to API DTOs.
 * Why it exists: REST controllers should not expose JPA entities or persistence internals.
 * Architecture fit: Application mapper for the Evidence module.
 */
package com.airural.platform.core.evidence.application;

import com.airural.platform.core.evidence.domain.*;
import com.airural.platform.core.evidence.web.dto.EvidenceDtos.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Maps evidence domain entities into API response records. */
@Component
public class EvidenceMapper {
    /** Maps an evidence aggregate into a response DTO. */
    public EvidenceResponse evidence(EvidenceEntity entity) {
        EvidenceMetadataEntity metadata = entity.metadata();
        return new EvidenceResponse(
                entity.id(),
                entity.organizationId(),
                entity.surveyId(),
                entity.questionId(),
                entity.uploadedByUserId(),
                entity.evidenceType(),
                entity.originalFileName(),
                entity.storedFileName(),
                entity.mimeType(),
                entity.sizeBytes(),
                entity.sha256Checksum(),
                entity.storageProvider(),
                entity.storageKey(),
                entity.currentVersion(),
                metadata == null ? null : metadata.title(),
                metadata == null ? null : metadata.description(),
                metadata == null ? null : metadata.customMetadataJson(),
                entity.tags().stream().map(EvidenceTagEntity::name).collect(Collectors.toCollection(java.util.LinkedHashSet::new)),
                entity.isActive(),
                entity.deletedAt(),
                entity.createdAt(),
                entity.updatedAt());
    }

    /** Maps an evidence version into a response DTO. */
    public EvidenceVersionResponse version(EvidenceVersionEntity entity) {
        return new EvidenceVersionResponse(
                entity.id(),
                entity.evidenceId(),
                entity.versionNumber(),
                entity.originalFileName(),
                entity.mimeType(),
                entity.sizeBytes(),
                entity.sha256Checksum(),
                entity.storageProvider(),
                entity.storageKey(),
                entity.metadataSnapshotJson(),
                entity.createdByUserId(),
                entity.createdAt());
    }

    /** Maps an evidence audit event into a response DTO. */
    public EvidenceAuditResponse audit(EvidenceAuditEntity entity) {
        return new EvidenceAuditResponse(
                entity.id(),
                entity.evidenceId(),
                entity.actorUserId(),
                entity.action(),
                entity.outcome(),
                entity.details(),
                entity.createdAt());
    }
}
