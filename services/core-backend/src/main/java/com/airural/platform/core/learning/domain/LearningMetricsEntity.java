/*
 * Purpose: Stores aggregate continuous learning observability metrics.
 * Why it exists: AI-7 tracks feedback volume, acceptance rate, correction rate, model error categories, dataset growth, knowledge updates, and candidate counts.
 * Architecture fit: Metrics entity for learning platform observability.
 */
package com.airural.platform.core.learning.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Learning metrics entity. */
@Entity
@Table(name = "learning_metrics", schema = "learning")
public class LearningMetricsEntity {
    @Id private UUID id;
    private String metricWindow;
    private Integer feedbackVolume;
    private BigDecimal acceptanceRate;
    private BigDecimal correctionRate;
    @Column(columnDefinition = "TEXT") private String modelErrorCategoriesJson;
    private Integer learningDatasetGrowth;
    private Integer knowledgeUpdateCount;
    private Integer trainingCandidateCount;
    private Instant measuredAt;

    protected LearningMetricsEntity() {}

    /** Creates a learning metrics record. */
    public LearningMetricsEntity(UUID id, String metricWindow, Integer feedbackVolume, BigDecimal acceptanceRate, BigDecimal correctionRate, String modelErrorCategoriesJson, Integer learningDatasetGrowth, Integer knowledgeUpdateCount, Integer trainingCandidateCount, Instant measuredAt) {
        this.id = id; this.metricWindow = metricWindow; this.feedbackVolume = feedbackVolume; this.acceptanceRate = acceptanceRate; this.correctionRate = correctionRate; this.modelErrorCategoriesJson = modelErrorCategoriesJson; this.learningDatasetGrowth = learningDatasetGrowth; this.knowledgeUpdateCount = knowledgeUpdateCount; this.trainingCandidateCount = trainingCandidateCount; this.measuredAt = measuredAt;
    }
}
