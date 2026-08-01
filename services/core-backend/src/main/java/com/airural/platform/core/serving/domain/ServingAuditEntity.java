/*
 * Purpose: Stores immutable serving audit events.
 * Why it exists: Inference traffic requires request signing, tenant isolation, prompt security, routing, output validation, citation validation, and response audit records.
 * Architecture fit: Audit evidence entity for production serving.
 */
package com.airural.platform.core.serving.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Serving audit entity. */
@Entity
@Table(name = "serving_audits", schema = "serving")
public class ServingAuditEntity {
    @Id private UUID id;
    private UUID inferenceRequestId;
    private UUID userId;
    private String eventType;
    private String tenantId;
    private String requestSignature;
    @Column(columnDefinition = "TEXT") private String eventJson;
    private String immutableHash;
    private Instant createdAt;

    protected ServingAuditEntity() {}

    /** Creates serving audit record. */
    public ServingAuditEntity(UUID id, UUID inferenceRequestId, UUID userId, String eventType, String tenantId, String requestSignature, String eventJson, String immutableHash, Instant createdAt) {
        this.id = id; this.inferenceRequestId = inferenceRequestId; this.userId = userId; this.eventType = eventType; this.tenantId = tenantId; this.requestSignature = requestSignature; this.eventJson = eventJson; this.immutableHash = immutableHash; this.createdAt = createdAt;
    }
}
