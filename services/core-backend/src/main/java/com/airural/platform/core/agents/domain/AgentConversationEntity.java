/*
 * Purpose: Persists conversation-level context for agent interactions.
 * Why it exists: Multi-agent chat requires durable conversation memory and history.
 * Architecture fit: Conversation aggregate for context and memory management.
 */
package com.airural.platform.core.agents.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for agent conversations. */
@Entity
@Table(name = "agent_conversations", schema = "agents")
public class AgentConversationEntity {
    @Id private UUID id;
    private UUID userId;
    @Column(nullable = false, length = 180) private String title;
    @Column(nullable = false, length = 40) private String status;
    @Column(nullable = false, columnDefinition = "TEXT") private String contextJson;
    @Column(nullable = false) private Instant createdAt;
    @Column(nullable = false) private Instant updatedAt;

    protected AgentConversationEntity() {}

    public AgentConversationEntity(UUID userId, String title, String contextJson) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.title = title;
        this.status = "ACTIVE";
        this.contextJson = contextJson;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public UUID id() { return id; }
    public UUID userId() { return userId; }
    public String title() { return title; }
    public String status() { return status; }
    public String contextJson() { return contextJson; }
    public Instant createdAt() { return createdAt; }
}
