/*
 * Purpose: Stores automated red-team attack results.
 * Why it exists: AI-5 must test prompt injection, role confusion, context poisoning, tool misuse, loops, long prompts, token flooding, and broken citations.
 * Architecture fit: Red-team framework entity for independent evaluation.
 */
package com.airural.platform.core.evaluation.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Red-team run entity. */
@Entity
@Table(name = "red_team_runs", schema = "evaluation")
public class RedTeamRunEntity {
    @Id private UUID id;
    private UUID evaluationRunId;
    private String attackType;
    private String outcome;
    private BigDecimal severityScore;
    @Column(columnDefinition = "TEXT")
    private String evidenceJson;
    private Instant createdAt;

    protected RedTeamRunEntity() {}

    /** Creates a red-team result. */
    public RedTeamRunEntity(UUID id, UUID evaluationRunId, String attackType, String outcome, BigDecimal severityScore, String evidenceJson, Instant createdAt) {
        this.id = id; this.evaluationRunId = evaluationRunId; this.attackType = attackType; this.outcome = outcome; this.severityScore = severityScore; this.evidenceJson = evidenceJson; this.createdAt = createdAt;
    }
}
