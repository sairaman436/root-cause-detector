/*
 * Purpose: Stores incoming inference request metadata and policy validation outcome.
 * Why it exists: All production inference must pass authentication, policy, prompt validation, routing, and audit controls.
 * Architecture fit: Request record for the AI-8 gateway pipeline.
 */
package com.airural.platform.core.serving.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Inference request entity. */
@Entity
@Table(name = "inference_requests", schema = "serving")
public class InferenceRequestEntity {
    @Id private UUID id;
    private UUID sessionId;
    private UUID userId;
    private String taskType;
    private String assistantType;
    private String language;
    private String userRole;
    @Column(columnDefinition = "TEXT") private String promptText;
    @Column(columnDefinition = "TEXT") private String contextJson;
    private Boolean streamingRequested;
    private Boolean batchRequested;
    private Boolean asyncRequested;
    private String policyStatus;
    private String promptSecurityStatus;
    private String status;
    private Instant createdAt;

    protected InferenceRequestEntity() {}

    /** Creates an inference request. */
    public InferenceRequestEntity(UUID id, UUID sessionId, UUID userId, String taskType, String assistantType, String language, String userRole, String promptText, String contextJson, Boolean streamingRequested, Boolean batchRequested, Boolean asyncRequested, String policyStatus, String promptSecurityStatus, String status, Instant createdAt) {
        this.id = id; this.sessionId = sessionId; this.userId = userId; this.taskType = taskType; this.assistantType = assistantType; this.language = language; this.userRole = userRole; this.promptText = promptText; this.contextJson = contextJson; this.streamingRequested = streamingRequested; this.batchRequested = batchRequested; this.asyncRequested = asyncRequested; this.policyStatus = policyStatus; this.promptSecurityStatus = promptSecurityStatus; this.status = status; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
}
