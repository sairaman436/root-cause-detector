/*
 * Purpose: Stores benchmark suites maintained by the laboratory.
 * Why it exists: Reasoning, policy, agriculture, health, education, climate, infrastructure, planning, and forecasting research require stable benchmark definitions.
 * Architecture fit: Research-1 benchmark framework entity.
 */
package com.airural.platform.core.research.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Research benchmark entity. */
@Entity
@Table(name = "research_benchmarks", schema = "research_lab")
public class ResearchBenchmarkEntity {
    @Id private UUID id;
    private String benchmarkKey;
    private String domain;
    private String taskType;
    private String metricDefinition;
    private String baseline;
    private String status;
    private Instant createdAt;

    protected ResearchBenchmarkEntity() {}

    /** Creates a research benchmark. */
    public ResearchBenchmarkEntity(UUID id, String benchmarkKey, String domain, String taskType, String metricDefinition, String baseline, String status, Instant createdAt) {
        this.id = id; this.benchmarkKey = benchmarkKey; this.domain = domain; this.taskType = taskType; this.metricDefinition = metricDefinition; this.baseline = baseline; this.status = status; this.createdAt = createdAt;
    }
}
