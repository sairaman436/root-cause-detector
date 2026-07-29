/*
 * Purpose: Persists the model registry root record.
 * Why it exists: Operators need governed model cataloging before routing inference traffic.
 * Architecture fit: AI foundation aggregate for model governance and gateway routing.
 */
package com.airural.platform.core.ai.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for a registered AI model. */
@Entity
@Table(name = "ai_models", schema = "ai")
public class AIModelEntity {
    @Id
    private UUID id;
    @Column(nullable = false, unique = true, length = 120)
    private String modelId;
    @Column(nullable = false, length = 180)
    private String name;
    @Column(nullable = false, length = 80)
    private String family;
    @Column(nullable = false, length = 80)
    private String provider;
    @Column(nullable = false, length = 40)
    private String status;
    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;

    protected AIModelEntity() {}

    public AIModelEntity(String modelId, String name, String family, String provider, String status) {
        this.id = UUID.randomUUID();
        this.modelId = modelId;
        this.name = name;
        this.family = family;
        this.provider = provider;
        this.status = status;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public UUID id() { return id; }
    public String modelId() { return modelId; }
    public String name() { return name; }
    public String family() { return family; }
    public String provider() { return provider; }
    public String status() { return status; }
    public Instant createdAt() { return createdAt; }
}
