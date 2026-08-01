/*
 * Purpose: Stores fine-tuning report, benchmark report, evaluation report, and loss curve artifacts.
 * Why it exists: AI-4 deliverables must be versioned with storage URI and checksum metadata.
 * Architecture fit: Report artifact entity for supervised fine-tuning lifecycle evidence.
 */
package com.airural.platform.core.finetuning.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Fine-tuning report entity. */
@Entity
@Table(name = "training_reports", schema = "finetuning")
public class TrainingReportEntity {
    @Id private UUID id;
    private UUID runId;
    private String reportType;
    private String storageUri;
    private String checksum;
    @Column(columnDefinition = "TEXT")
    private String summaryJson;
    private Instant createdAt;

    protected TrainingReportEntity() {}

    /** Creates a report artifact record. */
    public TrainingReportEntity(UUID id, UUID runId, String reportType, String storageUri, String checksum, String summaryJson, Instant createdAt) {
        this.id = id; this.runId = runId; this.reportType = reportType; this.storageUri = storageUri; this.checksum = checksum; this.summaryJson = summaryJson; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getRunId() { return runId; }
    public String getReportType() { return reportType; }
}
