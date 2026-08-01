/*
 * Purpose: Stores performance measurements for optimized artifacts.
 * Why it exists: AI-6 requires first-token latency, throughput, memory, utilization, startup, and concurrency evidence before release.
 * Architecture fit: Benchmark evidence entity attached to optimization artifacts.
 */
package com.airural.platform.core.optimization.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Performance benchmark entity. */
@Entity
@Table(name = "performance_benchmarks", schema = "optimization")
public class PerformanceBenchmarkEntity {
    @Id private UUID id;
    private UUID optimizationRunId;
    private UUID artifactId;
    private BigDecimal firstTokenLatencyMs;
    private BigDecimal tokensPerSecond;
    private BigDecimal peakMemoryGb;
    private BigDecimal averageMemoryGb;
    private BigDecimal gpuUtilizationPercent;
    private BigDecimal cpuUtilizationPercent;
    private BigDecimal coldStartMs;
    private BigDecimal warmStartMs;
    private Integer concurrentRequests;
    private String status;
    private Instant createdAt;

    protected PerformanceBenchmarkEntity() {}

    /** Creates a performance benchmark. */
    public PerformanceBenchmarkEntity(UUID id, UUID optimizationRunId, UUID artifactId, BigDecimal firstTokenLatencyMs, BigDecimal tokensPerSecond, BigDecimal peakMemoryGb, BigDecimal averageMemoryGb, BigDecimal gpuUtilizationPercent, BigDecimal cpuUtilizationPercent, BigDecimal coldStartMs, BigDecimal warmStartMs, Integer concurrentRequests, String status, Instant createdAt) {
        this.id = id; this.optimizationRunId = optimizationRunId; this.artifactId = artifactId; this.firstTokenLatencyMs = firstTokenLatencyMs; this.tokensPerSecond = tokensPerSecond; this.peakMemoryGb = peakMemoryGb; this.averageMemoryGb = averageMemoryGb; this.gpuUtilizationPercent = gpuUtilizationPercent; this.cpuUtilizationPercent = cpuUtilizationPercent; this.coldStartMs = coldStartMs; this.warmStartMs = warmStartMs; this.concurrentRequests = concurrentRequests; this.status = status; this.createdAt = createdAt;
    }
}
