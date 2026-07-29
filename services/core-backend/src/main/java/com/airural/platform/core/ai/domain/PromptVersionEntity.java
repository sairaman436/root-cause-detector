/*
 * Purpose: Persists immutable prompt template versions.
 * Why it exists: Prompt changes need rollback, review, test history, and analytics.
 * Architecture fit: Versioned child entity for prompt governance.
 */
package com.airural.platform.core.ai.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for prompt versions. */
@Entity
@Table(name = "prompt_versions", schema = "ai")
public class PromptVersionEntity {
    @Id
    private UUID id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private PromptTemplateEntity template;
    @Column(nullable = false)
    private Integer versionNumber;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String templateText;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String variablesJson;
    @Column(nullable = false, length = 40)
    private String approvalStatus;
    @Column(nullable = false)
    private Instant createdAt;

    protected PromptVersionEntity() {}

    public PromptVersionEntity(PromptTemplateEntity template, Integer versionNumber, String templateText, String variablesJson, String approvalStatus) {
        this.id = UUID.randomUUID();
        this.template = template;
        this.versionNumber = versionNumber;
        this.templateText = templateText;
        this.variablesJson = variablesJson;
        this.approvalStatus = approvalStatus;
        this.createdAt = Instant.now();
    }

    public UUID id() { return id; }
    public PromptTemplateEntity template() { return template; }
    public Integer versionNumber() { return versionNumber; }
    public String templateText() { return templateText; }
    public String variablesJson() { return variablesJson; }
    public String approvalStatus() { return approvalStatus; }
}
