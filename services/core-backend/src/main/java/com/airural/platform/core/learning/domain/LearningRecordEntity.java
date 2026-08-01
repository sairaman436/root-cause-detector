/*
 * Purpose: Stores a governed record of an AI interaction and its learning eligibility.
 * Why it exists: AI-7 requires every interaction to preserve model version, prompt version, context, input, output, confidence, evidence, reviewer, privacy, and approval state.
 * Architecture fit: Primary aggregate for continuous learning data capture.
 */
package com.airural.platform.core.learning.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Learning record entity. */
@Entity
@Table(name = "learning_records", schema = "learning")
public class LearningRecordEntity {
    @Id private UUID id;
    private Instant occurredAt;
    private String sourceType;
    private String modelVersion;
    private String promptVersion;
    @Column(columnDefinition = "TEXT") private String retrievedContext;
    @Column(name = "input_text", columnDefinition = "TEXT") private String input;
    @Column(name = "ai_output_text", columnDefinition = "TEXT") private String aiOutput;
    @Column(name = "human_edited_output_text", columnDefinition = "TEXT") private String humanEditedOutput;
    @Column(name = "accepted_output_text", columnDefinition = "TEXT") private String acceptedOutput;
    private BigDecimal confidence;
    @Column(columnDefinition = "TEXT") private String evidenceUsedJson;
    private String agentUsed;
    private String reviewer;
    private Boolean trainingEligible;
    private String privacyClassification;
    private String approvalStatus;

    protected LearningRecordEntity() {}

    /** Creates a learning record. */
    public LearningRecordEntity(UUID id, Instant occurredAt, String sourceType, String modelVersion, String promptVersion, String retrievedContext, String input, String aiOutput, String humanEditedOutput, String acceptedOutput, BigDecimal confidence, String evidenceUsedJson, String agentUsed, String reviewer, Boolean trainingEligible, String privacyClassification, String approvalStatus) {
        this.id = id; this.occurredAt = occurredAt; this.sourceType = sourceType; this.modelVersion = modelVersion; this.promptVersion = promptVersion; this.retrievedContext = retrievedContext; this.input = input; this.aiOutput = aiOutput; this.humanEditedOutput = humanEditedOutput; this.acceptedOutput = acceptedOutput; this.confidence = confidence; this.evidenceUsedJson = evidenceUsedJson; this.agentUsed = agentUsed; this.reviewer = reviewer; this.trainingEligible = trainingEligible; this.privacyClassification = privacyClassification; this.approvalStatus = approvalStatus;
    }

    public UUID getId() { return id; }
    public String getSourceType() { return sourceType; }
    public String getModelVersion() { return modelVersion; }
    public Boolean getTrainingEligible() { return trainingEligible; }
    public String getPrivacyClassification() { return privacyClassification; }
    public String getApprovalStatus() { return approvalStatus; }
}
