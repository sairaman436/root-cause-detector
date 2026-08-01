/*
 * Purpose: Stores benchmark suite registry records.
 * Why it exists: AI-5 requires repeatable benchmark collections across rural, policy, reasoning, safety, citation, and tool-use capabilities.
 * Architecture fit: Registry entity for the enterprise evaluation platform.
 */
package com.airural.platform.core.evaluation.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Benchmark suite registry entity. */
@Entity
@Table(name = "benchmark_suites", schema = "evaluation")
public class BenchmarkSuiteEntity {
    @Id private UUID id;
    private String suiteKey;
    private String name;
    private String category;
    private String datasetType;
    private String version;
    private String status;
    @Column(columnDefinition = "TEXT")
    private String criteriaJson;
    private Instant createdAt;

    protected BenchmarkSuiteEntity() {}

    /** Creates a benchmark suite. */
    public BenchmarkSuiteEntity(UUID id, String suiteKey, String name, String category, String datasetType, String version, String status, String criteriaJson, Instant createdAt) {
        this.id = id; this.suiteKey = suiteKey; this.name = name; this.category = category; this.datasetType = datasetType; this.version = version; this.status = status; this.criteriaJson = criteriaJson; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getStatus() { return status; }
}
