/*
 * Purpose: Stores coverage quality results for knowledge datasets.
 * Why it exists: Knowledge corpora must expose topic, geography, and policy gaps before downstream RAG usage.
 * Architecture fit: Coverage quality entity for AI-2 governance.
 */
package com.airural.platform.core.knowledge.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Knowledge coverage quality entity. */
@Entity
@Table(name = "knowledge_coverage", schema = "knowledge")
public class KnowledgeCoverageEntity {
    @Id private UUID id;
    private UUID datasetId;
    private String coverageArea;
    private BigDecimal coverageScore;
    @Column(columnDefinition = "TEXT")
    private String missingTopics;
    private Instant evaluatedAt;

    protected KnowledgeCoverageEntity() {}

    /** Creates a coverage report. */
    public KnowledgeCoverageEntity(UUID id, UUID datasetId, String coverageArea, BigDecimal coverageScore, String missingTopics, Instant evaluatedAt) {
        this.id = id; this.datasetId = datasetId; this.coverageArea = coverageArea; this.coverageScore = coverageScore; this.missingTopics = missingTopics; this.evaluatedAt = evaluatedAt;
    }

    public UUID getDatasetId() { return datasetId; }
    public String getCoverageArea() { return coverageArea; }
    public BigDecimal getCoverageScore() { return coverageScore; }
}
