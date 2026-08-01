/*
 * Purpose: Stores AI safety test results.
 * Why it exists: Prompt injection, leakage, jailbreak, policy, sensitive data, hallucination, citation, unsafe advice, bias, and toxicity checks must be recorded.
 * Architecture fit: AI-5 safety framework entity.
 */
package com.airural.platform.core.evaluation.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** AI safety test entity. */
@Entity
@Table(name = "safety_tests", schema = "evaluation")
public class SafetyTestEntity {
    @Id private UUID id;
    private UUID evaluationRunId;
    private String testType;
    private String status;
    private BigDecimal riskScore;
    @Column(columnDefinition = "TEXT")
    private String findingsJson;
    private Instant createdAt;

    protected SafetyTestEntity() {}

    /** Creates a safety test result. */
    public SafetyTestEntity(UUID id, UUID evaluationRunId, String testType, String status, BigDecimal riskScore, String findingsJson, Instant createdAt) {
        this.id = id; this.evaluationRunId = evaluationRunId; this.testType = testType; this.status = status; this.riskScore = riskScore; this.findingsJson = findingsJson; this.createdAt = createdAt;
    }
}
