/*
 * Purpose: Stores serving observability metrics.
 * Why it exists: AI-8 must track requests/sec, latency, tokens/sec, GPU, VRAM, CPU, memory, queue depth, errors, and timeouts.
 * Architecture fit: Metrics entity for inference SRE and performance operations.
 */
package com.airural.platform.core.serving.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Inference metrics entity. */
@Entity
@Table(name = "inference_metrics", schema = "serving")
public class InferenceMetricsEntity {
    @Id private UUID id;
    private String metricWindow;
    private BigDecimal requestsPerSecond;
    @Column(name = "p95_latency_ms")
    private BigDecimal p95LatencyMs;
    private BigDecimal tokensPerSecond;
    private BigDecimal gpuUtilizationPercent;
    private BigDecimal vramGb;
    private BigDecimal cpuUtilizationPercent;
    private BigDecimal memoryGb;
    private Integer queueDepth;
    private BigDecimal errorRate;
    private BigDecimal timeoutRate;
    private Instant measuredAt;

    protected InferenceMetricsEntity() {}

    /** Creates inference metrics. */
    public InferenceMetricsEntity(UUID id, String metricWindow, BigDecimal requestsPerSecond, BigDecimal p95LatencyMs, BigDecimal tokensPerSecond, BigDecimal gpuUtilizationPercent, BigDecimal vramGb, BigDecimal cpuUtilizationPercent, BigDecimal memoryGb, Integer queueDepth, BigDecimal errorRate, BigDecimal timeoutRate, Instant measuredAt) {
        this.id = id; this.metricWindow = metricWindow; this.requestsPerSecond = requestsPerSecond; this.p95LatencyMs = p95LatencyMs; this.tokensPerSecond = tokensPerSecond; this.gpuUtilizationPercent = gpuUtilizationPercent; this.vramGb = vramGb; this.cpuUtilizationPercent = cpuUtilizationPercent; this.memoryGb = memoryGb; this.queueDepth = queueDepth; this.errorRate = errorRate; this.timeoutRate = timeoutRate; this.measuredAt = measuredAt;
    }
}
