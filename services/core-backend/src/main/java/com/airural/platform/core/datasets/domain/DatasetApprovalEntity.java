package com.airural.platform.core.datasets.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Dataset approval workflow record. */
@Entity
@Table(name = "dataset_approvals", schema = "datasets")
public class DatasetApprovalEntity {
    @Id private UUID id;
    private UUID datasetId;
    private UUID approverId;
    private String approvalStatus;
    @Column(columnDefinition = "TEXT")
    private String rationale;
    private Instant createdAt;
    protected DatasetApprovalEntity() {}
    public DatasetApprovalEntity(UUID id, UUID datasetId, UUID approverId, String approvalStatus, String rationale, Instant createdAt) {
        this.id = id; this.datasetId = datasetId; this.approverId = approverId; this.approvalStatus = approvalStatus; this.rationale = rationale; this.createdAt = createdAt;
    }
}
