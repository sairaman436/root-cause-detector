/*
 * Purpose: Stores benchmark execution records linked to an evaluation run.
 * Why it exists: Benchmark results must be independently reproducible by suite and model.
 * Architecture fit: AI-5 benchmark execution entity.
 */
package com.airural.platform.core.evaluation.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Benchmark run entity. */
@Entity
@Table(name = "benchmark_runs", schema = "evaluation")
public class BenchmarkRunEntity {
    @Id private UUID id;
    private UUID evaluationRunId;
    private UUID benchmarkSuiteId;
    private BigDecimal score;
    private String status;
    @Column(columnDefinition = "TEXT")
    private String resultJson;
    private Instant createdAt;

    protected BenchmarkRunEntity() {}

    /** Creates a benchmark run. */
    public BenchmarkRunEntity(UUID id, UUID evaluationRunId, UUID benchmarkSuiteId, BigDecimal score, String status, String resultJson, Instant createdAt) {
        this.id = id; this.evaluationRunId = evaluationRunId; this.benchmarkSuiteId = benchmarkSuiteId; this.score = score; this.status = status; this.resultJson = resultJson; this.createdAt = createdAt;
    }
}
