/*
 * Purpose: Stores one authenticated human review attached to one immutable multimodal trace.
 * Why it exists: Multimodal quality scores must be durable, attributable, and independent from the trace artifact.
 * Architecture fit: Evaluation bounded context; this entity never mutates model output or training data.
 */
package com.airural.platform.core.evaluation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Durable multimodal human-quality review. */
@Entity
@Table(schema = "evaluation", name = "multimodal_human_reviews", uniqueConstraints = @UniqueConstraint(
        name = "uq_multimodal_review_reviewer", columnNames = {"trace_id", "reviewer_id"}))
public class MultimodalHumanReviewEntity {
    @Id
    private UUID id;
    @Column(name = "trace_id", nullable = false, length = 160)
    private String traceId;
    @Column(name = "artifact_version", nullable = false, length = 120)
    private String artifactVersion;
    @Column(name = "evaluation_round", nullable = false, length = 120)
    private String evaluationRound;
    @Column(name = "reviewer_id", nullable = false)
    private UUID reviewerId;
    @Column(name = "rubric_version", nullable = false, length = 80)
    private String rubricVersion;
    @Column(name = "observation_quality", nullable = false)
    private Integer observationQuality;
    @Column(name = "evidence_relevance", nullable = false)
    private Integer evidenceRelevance;
    @Column(name = "root_cause_quality", nullable = false)
    private Integer rootCauseQuality;
    @Column(name = "recommendation_quality")
    private Integer recommendationQuality;
    @Column(name = "grounding", nullable = false)
    private Integer grounding;
    @Column(name = "overall_usefulness", nullable = false)
    private Integer overallUsefulness;
    @Column(name = "failure_classification", nullable = false, length = 80)
    private String failureClassification;
    @Column(name = "unsupported_claim_flags", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String unsupportedClaimFlags;
    @Column(name = "reviewer_comments", columnDefinition = "TEXT")
    private String reviewerComments;
    @Column(name = "submission_status", nullable = false, length = 40)
    private String submissionStatus;
    @Column(name = "reviewed_at", nullable = false)
    private Instant reviewedAt;

    protected MultimodalHumanReviewEntity() {}

    public MultimodalHumanReviewEntity(UUID id, String traceId, String artifactVersion, String evaluationRound,
            UUID reviewerId, String rubricVersion, Integer observationQuality, Integer evidenceRelevance,
            Integer rootCauseQuality, Integer recommendationQuality, Integer grounding, Integer overallUsefulness,
            String failureClassification, String unsupportedClaimFlags, String reviewerComments,
            String submissionStatus, Instant reviewedAt) {
        this.id = id;
        this.traceId = traceId;
        this.artifactVersion = artifactVersion;
        this.evaluationRound = evaluationRound;
        this.reviewerId = reviewerId;
        this.rubricVersion = rubricVersion;
        this.observationQuality = observationQuality;
        this.evidenceRelevance = evidenceRelevance;
        this.rootCauseQuality = rootCauseQuality;
        this.recommendationQuality = recommendationQuality;
        this.grounding = grounding;
        this.overallUsefulness = overallUsefulness;
        this.failureClassification = failureClassification;
        this.unsupportedClaimFlags = unsupportedClaimFlags;
        this.reviewerComments = reviewerComments;
        this.submissionStatus = submissionStatus;
        this.reviewedAt = reviewedAt;
    }

    public UUID getId() { return id; }
    public String getTraceId() { return traceId; }
    public String getArtifactVersion() { return artifactVersion; }
    public String getEvaluationRound() { return evaluationRound; }
    public UUID getReviewerId() { return reviewerId; }
    public String getRubricVersion() { return rubricVersion; }
    public Integer getObservationQuality() { return observationQuality; }
    public Integer getEvidenceRelevance() { return evidenceRelevance; }
    public Integer getRootCauseQuality() { return rootCauseQuality; }
    public Integer getRecommendationQuality() { return recommendationQuality; }
    public Integer getGrounding() { return grounding; }
    public Integer getOverallUsefulness() { return overallUsefulness; }
    public String getFailureClassification() { return failureClassification; }
    public String getUnsupportedClaimFlags() { return unsupportedClaimFlags; }
    public String getReviewerComments() { return reviewerComments; }
    public String getSubmissionStatus() { return submissionStatus; }
    public Instant getReviewedAt() { return reviewedAt; }
}

