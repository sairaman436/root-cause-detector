/*
 * Purpose: Stores the independent 0-4 scores defined by HUMAN-QUALITY-RUBRIC@1.0.0.
 * Why it exists: Criterion scores must remain individually auditable and must not be collapsed into an automatic score.
 * Architecture fit: One-to-one child of a human evaluation in the evaluation bounded context.
 */
package com.airural.platform.core.evaluation.domain;

import jakarta.persistence.*;
import java.util.UUID;

/** Human rubric criterion scores. */
@Entity
@Table(schema = "evaluation", name = "human_evaluation_scores")
public class HumanEvaluationScoreEntity {
    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "human_evaluation_id", nullable = false, unique = true)
    private HumanEvaluationEntity humanEvaluation;

    @Column(name = "root_cause_quality") private Integer rootCauseQuality;
    @Column(name = "recommendation_quality") private Integer recommendationQuality;
    @Column(name = "rag_evidence_quality") private Integer ragEvidenceQuality;
    @Column(name = "uncertainty_handling") private Integer uncertaintyHandling;
    @Column(name = "practical_usefulness") private Integer practicalUsefulness;

    protected HumanEvaluationScoreEntity() {}

    public HumanEvaluationScoreEntity(UUID id, Integer rootCauseQuality, Integer recommendationQuality,
            Integer ragEvidenceQuality, Integer uncertaintyHandling, Integer practicalUsefulness) {
        this.id = id;
        this.rootCauseQuality = rootCauseQuality;
        this.recommendationQuality = recommendationQuality;
        this.ragEvidenceQuality = ragEvidenceQuality;
        this.uncertaintyHandling = uncertaintyHandling;
        this.practicalUsefulness = practicalUsefulness;
    }

    void attachTo(HumanEvaluationEntity humanEvaluation) { this.humanEvaluation = humanEvaluation; }
    public UUID getId() { return id; }
    public Integer getRootCauseQuality() { return rootCauseQuality; }
    public Integer getRecommendationQuality() { return recommendationQuality; }
    public Integer getRagEvidenceQuality() { return ragEvidenceQuality; }
    public Integer getUncertaintyHandling() { return uncertaintyHandling; }
    public Integer getPracticalUsefulness() { return practicalUsefulness; }
}

