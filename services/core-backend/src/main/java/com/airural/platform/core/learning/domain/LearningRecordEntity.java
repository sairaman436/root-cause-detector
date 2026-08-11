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
    private String taskType;
    private String scenarioGroup;
    private Boolean synthetic;
    private String datasetVersion;
    private UUID evaluationResultId;
    private BigDecimal evaluationScore;
    @Column(columnDefinition = "TEXT") private String evaluationMetadataJson;

    protected LearningRecordEntity() {}

    /** Creates a learning record. */
    public LearningRecordEntity(UUID id, Instant occurredAt, String sourceType, String modelVersion, String promptVersion, String retrievedContext, String input, String aiOutput, String humanEditedOutput, String acceptedOutput, BigDecimal confidence, String evidenceUsedJson, String agentUsed, String reviewer, Boolean trainingEligible, String privacyClassification, String approvalStatus) {
        this(id, occurredAt, sourceType, modelVersion, promptVersion, retrievedContext, input, aiOutput, humanEditedOutput, acceptedOutput, confidence, evidenceUsedJson, agentUsed, reviewer, trainingEligible, privacyClassification, approvalStatus, sourceType, id == null ? null : id.toString(), false, null);
    }

    /** Creates a learning record with explicit dataset lineage fields. */
    public LearningRecordEntity(UUID id, Instant occurredAt, String sourceType, String modelVersion, String promptVersion, String retrievedContext, String input, String aiOutput, String humanEditedOutput, String acceptedOutput, BigDecimal confidence, String evidenceUsedJson, String agentUsed, String reviewer, Boolean trainingEligible, String privacyClassification, String approvalStatus, String taskType, String scenarioGroup, Boolean synthetic, String datasetVersion) {
        this(id, occurredAt, sourceType, modelVersion, promptVersion, retrievedContext, input, aiOutput, humanEditedOutput, acceptedOutput, confidence, evidenceUsedJson, agentUsed, reviewer, trainingEligible, privacyClassification, approvalStatus, taskType, scenarioGroup, synthetic, datasetVersion, null, null, null);
    }

    /** Creates a learning record with evaluation result provenance. */
    public LearningRecordEntity(UUID id, Instant occurredAt, String sourceType, String modelVersion, String promptVersion, String retrievedContext, String input, String aiOutput, String humanEditedOutput, String acceptedOutput, BigDecimal confidence, String evidenceUsedJson, String agentUsed, String reviewer, Boolean trainingEligible, String privacyClassification, String approvalStatus, String taskType, String scenarioGroup, Boolean synthetic, String datasetVersion, UUID evaluationResultId, BigDecimal evaluationScore, String evaluationMetadataJson) {
        this.id = id; this.occurredAt = occurredAt; this.sourceType = sourceType; this.modelVersion = modelVersion; this.promptVersion = promptVersion; this.retrievedContext = retrievedContext; this.input = input; this.aiOutput = aiOutput; this.humanEditedOutput = humanEditedOutput; this.acceptedOutput = acceptedOutput; this.confidence = confidence; this.evidenceUsedJson = evidenceUsedJson; this.agentUsed = agentUsed; this.reviewer = reviewer; this.trainingEligible = trainingEligible; this.privacyClassification = privacyClassification; this.approvalStatus = approvalStatus; this.taskType = taskType; this.scenarioGroup = scenarioGroup; this.synthetic = synthetic; this.datasetVersion = datasetVersion; this.evaluationResultId = evaluationResultId; this.evaluationScore = evaluationScore; this.evaluationMetadataJson = evaluationMetadataJson;
    }

    public UUID getId() { return id; }
    public String getSourceType() { return sourceType; }
    public String getModelVersion() { return modelVersion; }
    public String getPromptVersion() { return promptVersion; }
    public String getRetrievedContext() { return retrievedContext; }
    public String getInput() { return input; }
    public String getAiOutput() { return aiOutput; }
    public String getHumanEditedOutput() { return humanEditedOutput; }
    public String getAcceptedOutput() { return acceptedOutput; }
    public String getEvidenceUsedJson() { return evidenceUsedJson; }
    public String getTaskType() { return taskType; }
    public String getScenarioGroup() { return scenarioGroup; }
    public Boolean getSynthetic() { return synthetic; }
    public String getDatasetVersion() { return datasetVersion; }
    public UUID getEvaluationResultId() { return evaluationResultId; }
    public BigDecimal getEvaluationScore() { return evaluationScore; }
    public String getEvaluationMetadataJson() { return evaluationMetadataJson; }
    public Boolean getTrainingEligible() { return trainingEligible; }
    public String getPrivacyClassification() { return privacyClassification; }
    public String getApprovalStatus() { return approvalStatus; }

    /** Returns whether this record is explicitly or inferentially synthetic. */
    public boolean isSyntheticRecord() {
        return Boolean.TRUE.equals(synthetic) || containsSynthetic(sourceType) || containsSynthetic(privacyClassification);
    }

    /** Applies a validated human correction and makes the record dataset-eligible. */
    public void approveWithOutput(String output, String datasetVersion) {
        this.humanEditedOutput = output;
        this.acceptedOutput = output;
        this.trainingEligible = true;
        this.approvalStatus = "APPROVED_FOR_DATASET";
        this.datasetVersion = datasetVersion;
    }

    /** Marks the original output as explicitly approved for dataset preparation. */
    public void approveOriginal(String datasetVersion) {
        this.trainingEligible = true;
        this.approvalStatus = "APPROVED_FOR_DATASET";
        this.datasetVersion = datasetVersion;
    }

    /** Removes a record from training eligibility after rejection. */
    public void rejectForTraining() {
        this.trainingEligible = false;
        this.approvalStatus = "REJECTED";
    }

    private boolean containsSynthetic(String value) {
        return value != null && value.toUpperCase().contains("SYNTHETIC");
    }
}
