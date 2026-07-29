/*
 * Purpose: Persists prompt template identity and governance state.
 * Why it exists: LLM interactions must be versioned, testable, and approved before operational use.
 * Architecture fit: Prompt management aggregate root.
 */
package com.airural.platform.core.ai.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for prompt templates. */
@Entity
@Table(name = "prompt_templates", schema = "ai")
public class PromptTemplateEntity {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private PromptCategoryEntity category;
    @Column(nullable = false, unique = true, length = 160)
    private String name;
    @Column(nullable = false, length = 40)
    private String status;
    private UUID createdBy;
    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;

    protected PromptTemplateEntity() {}

    public PromptTemplateEntity(PromptCategoryEntity category, String name, String status, UUID createdBy) {
        this.id = UUID.randomUUID();
        this.category = category;
        this.name = name;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public UUID id() { return id; }
    public PromptCategoryEntity category() { return category; }
    public String name() { return name; }
    public String status() { return status; }
    public UUID createdBy() { return createdBy; }
}
