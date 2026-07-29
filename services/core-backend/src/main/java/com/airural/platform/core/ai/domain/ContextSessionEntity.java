/*
 * Purpose: Persists context-session metadata for AI calls.
 * Why it exists: RAG and chat workflows need conversation, village, survey, administrative, and knowledge context boundaries.
 * Architecture fit: Context management entity for the AI foundation.
 */
package com.airural.platform.core.ai.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for AI context sessions. */
@Entity
@Table(name = "context_sessions", schema = "ai")
public class ContextSessionEntity {
    @Id
    private UUID id;
    private UUID userId;
    private UUID villageId;
    private UUID surveyId;
    @Column(nullable = false, length = 80)
    private String sessionType;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String contextJson;
    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;

    protected ContextSessionEntity() {}

    public ContextSessionEntity(UUID userId, UUID villageId, UUID surveyId, String sessionType, String contextJson) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.villageId = villageId;
        this.surveyId = surveyId;
        this.sessionType = sessionType;
        this.contextJson = contextJson;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public UUID id() { return id; }
    public UUID userId() { return userId; }
}
