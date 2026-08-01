/*
 * Purpose: Stores conversation, user, village, survey, and knowledge context for inference sessions.
 * Why it exists: AI-8 requires bounded context, session expiration, and memory limits for all serving traffic.
 * Architecture fit: Session aggregate for the enterprise inference gateway.
 */
package com.airural.platform.core.serving.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Inference session entity. */
@Entity
@Table(name = "inference_sessions", schema = "serving")
public class InferenceSessionEntity {
    @Id private UUID id;
    private UUID userId;
    private String sessionType;
    @Column(columnDefinition = "TEXT") private String conversationContext;
    @Column(columnDefinition = "TEXT") private String userContext;
    @Column(columnDefinition = "TEXT") private String villageContext;
    @Column(columnDefinition = "TEXT") private String surveyContext;
    @Column(columnDefinition = "TEXT") private String knowledgeContext;
    private Integer memoryLimitTokens;
    private Instant expiresAt;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    protected InferenceSessionEntity() {}

    /** Creates an inference session. */
    public InferenceSessionEntity(UUID id, UUID userId, String sessionType, String conversationContext, String userContext, String villageContext, String surveyContext, String knowledgeContext, Integer memoryLimitTokens, Instant expiresAt, String status, Instant createdAt, Instant updatedAt) {
        this.id = id; this.userId = userId; this.sessionType = sessionType; this.conversationContext = conversationContext; this.userContext = userContext; this.villageContext = villageContext; this.surveyContext = surveyContext; this.knowledgeContext = knowledgeContext; this.memoryLimitTokens = memoryLimitTokens; this.expiresAt = expiresAt; this.status = status; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getStatus() { return status; }
}
