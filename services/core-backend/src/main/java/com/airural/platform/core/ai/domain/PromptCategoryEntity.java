/*
 * Purpose: Groups prompt templates by operational purpose.
 * Why it exists: Prompt governance requires categories for ownership, approval, and analytics.
 * Architecture fit: Prompt management catalog entity.
 */
package com.airural.platform.core.ai.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for prompt categories. */
@Entity
@Table(name = "prompt_categories", schema = "ai")
public class PromptCategoryEntity {
    @Id
    private UUID id;
    @Column(nullable = false, unique = true, length = 120)
    private String name;
    @Column(length = 500)
    private String description;
    @Column(nullable = false)
    private Instant createdAt;

    protected PromptCategoryEntity() {}

    public PromptCategoryEntity(String name, String description) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.createdAt = Instant.now();
    }

    public UUID id() { return id; }
    public String name() { return name; }
    public String description() { return description; }
}
