/*
 * Purpose: Stores one authenticated human review of one immutable evaluation-set example.
 * Why it exists: Human semantic scores are independent governance evidence and must retain their provenance.
 * Architecture fit: Evaluation bounded context; it is separate from learning candidate approval records.
 */
package com.airural.platform.core.evaluation.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Authenticated human evaluation metadata and provenance. */
@Entity
@Table(schema = "evaluation", name = "human_evaluations", uniqueConstraints = @UniqueConstraint(
        name = "uq_human_evaluation_reviewer",
        columnNames = {"evaluation_set_version", "example_id", "reviewer_id"}))
public class HumanEvaluationEntity {
    @Id
    private UUID id;

    @Column(name = "evaluation_set_version", nullable = false, length = 80)
    private String evaluationSetVersion;
    @Column(name = "example_id", nullable = false, length = 160)
    private String exampleId;
    @Column(nullable = false, length = 80)
    private String task;
    @Column(name = "model_version", nullable = false, length = 160)
    private String modelVersion;
    @Column(name = "prompt_version", nullable = false, length = 200)
    private String promptVersion;
    @Column(name = "rubric_version", nullable = false, length = 80)
    private String rubricVersion;
    @Column(name = "inference_configuration", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String inferenceConfiguration;
    @Column(name = "output_sha256", nullable = false, columnDefinition = "char(64)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private String outputSha256;
    @Column(name = "reviewer_id", nullable = false)
    private UUID reviewerId;
    @Column(name = "reviewer_comments", columnDefinition = "TEXT")
    private String reviewerComments;
    @Column(name = "evidence_references_used", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String evidenceReferencesUsed;
    @Column(name = "reviewed_at", nullable = false)
    private Instant reviewedAt;

    @OneToOne(mappedBy = "humanEvaluation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER, optional = false)
    private HumanEvaluationScoreEntity score;

    protected HumanEvaluationEntity() {}

    public HumanEvaluationEntity(UUID id, String evaluationSetVersion, String exampleId, String task,
            String modelVersion, String promptVersion, String rubricVersion, String inferenceConfiguration,
            String outputSha256, UUID reviewerId, String reviewerComments, String evidenceReferencesUsed,
            Instant reviewedAt) {
        this.id = id;
        this.evaluationSetVersion = evaluationSetVersion;
        this.exampleId = exampleId;
        this.task = task;
        this.modelVersion = modelVersion;
        this.promptVersion = promptVersion;
        this.rubricVersion = rubricVersion;
        this.inferenceConfiguration = inferenceConfiguration;
        this.outputSha256 = outputSha256;
        this.reviewerId = reviewerId;
        this.reviewerComments = reviewerComments;
        this.evidenceReferencesUsed = evidenceReferencesUsed;
        this.reviewedAt = reviewedAt;
    }

    public UUID getId() { return id; }
    public String getEvaluationSetVersion() { return evaluationSetVersion; }
    public String getExampleId() { return exampleId; }
    public String getTask() { return task; }
    public String getModelVersion() { return modelVersion; }
    public String getPromptVersion() { return promptVersion; }
    public String getRubricVersion() { return rubricVersion; }
    public String getInferenceConfiguration() { return inferenceConfiguration; }
    public String getOutputSha256() { return outputSha256; }
    public UUID getReviewerId() { return reviewerId; }
    public String getReviewerComments() { return reviewerComments; }
    public String getEvidenceReferencesUsed() { return evidenceReferencesUsed; }
    public Instant getReviewedAt() { return reviewedAt; }
    public HumanEvaluationScoreEntity getScore() { return score; }

    public void attachScore(HumanEvaluationScoreEntity score) {
        this.score = score;
        score.attachTo(this);
    }
}
