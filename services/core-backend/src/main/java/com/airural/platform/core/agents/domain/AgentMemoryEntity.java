/*
 * Purpose: Persists multi-scope agent memory records.
 * Why it exists: Conversation, session, task, village, survey, shared, and knowledge reference memory must be queryable.
 * Architecture fit: Shared memory store for the agent context manager.
 */
package com.airural.platform.core.agents.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for agent memory. */
@Entity
@Table(name = "agent_memory", schema = "agents")
public class AgentMemoryEntity {
    @Id private UUID id;
    @Column(nullable = false, length = 80) private String memoryType;
    @Column(nullable = false, length = 80) private String scopeType;
    private UUID scopeId;
    private UUID conversationId;
    private UUID taskId;
    @Column(nullable = false, columnDefinition = "TEXT") private String contentJson;
    @Column(nullable = false, columnDefinition = "TEXT") private String referencesJson;
    @Column(nullable = false) private Instant createdAt;
    private Instant expiresAt;

    protected AgentMemoryEntity() {}

    public AgentMemoryEntity(String memoryType, String scopeType, UUID scopeId, UUID conversationId, UUID taskId, String contentJson, String referencesJson) {
        this.id = UUID.randomUUID();
        this.memoryType = memoryType;
        this.scopeType = scopeType;
        this.scopeId = scopeId;
        this.conversationId = conversationId;
        this.taskId = taskId;
        this.contentJson = contentJson;
        this.referencesJson = referencesJson;
        this.createdAt = Instant.now();
    }

    public UUID id() { return id; }
    public String memoryType() { return memoryType; }
    public String scopeType() { return scopeType; }
    public UUID conversationId() { return conversationId; }
    public String contentJson() { return contentJson; }
    public Instant createdAt() { return createdAt; }
}
