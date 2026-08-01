/*
 * Purpose: Stores acquisition, crawl, and reindex job state.
 * Why it exists: Knowledge ingestion must be observable, retryable, and auditable across connectors.
 * Architecture fit: Operational job entity for AI-2 acquisition workflows.
 */
package com.airural.platform.core.knowledge.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Knowledge acquisition job entity. */
@Entity
@Table(name = "knowledge_acquisition_jobs", schema = "knowledge")
public class KnowledgeAcquisitionJobEntity {
    @Id private UUID id;
    private UUID sourceId;
    private String jobType;
    private String status;
    private Instant startedAt;
    private Instant completedAt;
    private Integer documentsDiscovered;
    private Integer documentsAccepted;
    private BigDecimal qualityScore;
    private String errorMessage;

    protected KnowledgeAcquisitionJobEntity() {}

    /** Creates a knowledge acquisition job. */
    public KnowledgeAcquisitionJobEntity(UUID id, UUID sourceId, String jobType, String status, Instant startedAt, Instant completedAt, Integer documentsDiscovered, Integer documentsAccepted, BigDecimal qualityScore, String errorMessage) {
        this.id = id; this.sourceId = sourceId; this.jobType = jobType; this.status = status; this.startedAt = startedAt; this.completedAt = completedAt; this.documentsDiscovered = documentsDiscovered; this.documentsAccepted = documentsAccepted; this.qualityScore = qualityScore; this.errorMessage = errorMessage;
    }

    public UUID getId() { return id; }
    public UUID getSourceId() { return sourceId; }
    public String getJobType() { return jobType; }
    public String getStatus() { return status; }
    public BigDecimal getQualityScore() { return qualityScore; }
}
