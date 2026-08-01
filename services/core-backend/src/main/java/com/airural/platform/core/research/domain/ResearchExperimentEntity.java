/*
 * Purpose: Stores approved and running research experiments.
 * Why it exists: Experiments need hypothesis linkage, methodology, replication status, reproducibility evidence, and approval records.
 * Architecture fit: Research-1 experiment registry entity.
 */
package com.airural.platform.core.research.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Research experiment entity. */
@Entity
@Table(name = "research_experiments", schema = "research_lab")
public class ResearchExperimentEntity {
    @Id private UUID id;
    private UUID projectId;
    private String experimentKey;
    private String title;
    private String hypothesis;
    private String methodology;
    private String benchmarkSuite;
    private String approvalStatus;
    private String replicationStatus;
    private String reproducibilityReport;
    private Instant createdAt;

    protected ResearchExperimentEntity() {}

    /** Creates a research experiment. */
    public ResearchExperimentEntity(UUID id, UUID projectId, String experimentKey, String title, String hypothesis, String methodology, String benchmarkSuite, String approvalStatus, String replicationStatus, String reproducibilityReport, Instant createdAt) {
        this.id = id; this.projectId = projectId; this.experimentKey = experimentKey; this.title = title; this.hypothesis = hypothesis; this.methodology = methodology; this.benchmarkSuite = benchmarkSuite; this.approvalStatus = approvalStatus; this.replicationStatus = replicationStatus; this.reproducibilityReport = reproducibilityReport; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getProjectId() { return projectId; }
    public String getExperimentKey() { return experimentKey; }
    public String getTitle() { return title; }
    public String getApprovalStatus() { return approvalStatus; }
}
