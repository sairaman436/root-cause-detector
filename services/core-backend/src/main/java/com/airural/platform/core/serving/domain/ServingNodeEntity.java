/*
 * Purpose: Stores serving node health and capacity.
 * Why it exists: AI-8 routing needs node status, GPU/CPU/memory capacity, queue depth, circuit breaker, and load-balancing metadata.
 * Architecture fit: Runtime inventory entity for inference infrastructure.
 */
package com.airural.platform.core.serving.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Serving node entity. */
@Entity
@Table(name = "serving_nodes", schema = "serving")
public class ServingNodeEntity {
    @Id private UUID id;
    private String nodeName;
    private String providerType;
    private String endpointUrl;
    private String hardwareClass;
    private Integer queueDepth;
    private Integer maxConcurrency;
    private Integer gpuCount;
    private Integer vramGb;
    private Integer cpuCores;
    private Integer memoryGb;
    private String circuitStatus;
    private String healthStatus;
    private Instant lastHeartbeatAt;

    protected ServingNodeEntity() {}

    /** Creates a serving node. */
    public ServingNodeEntity(UUID id, String nodeName, String providerType, String endpointUrl, String hardwareClass, Integer queueDepth, Integer maxConcurrency, Integer gpuCount, Integer vramGb, Integer cpuCores, Integer memoryGb, String circuitStatus, String healthStatus, Instant lastHeartbeatAt) {
        this.id = id; this.nodeName = nodeName; this.providerType = providerType; this.endpointUrl = endpointUrl; this.hardwareClass = hardwareClass; this.queueDepth = queueDepth; this.maxConcurrency = maxConcurrency; this.gpuCount = gpuCount; this.vramGb = vramGb; this.cpuCores = cpuCores; this.memoryGb = memoryGb; this.circuitStatus = circuitStatus; this.healthStatus = healthStatus; this.lastHeartbeatAt = lastHeartbeatAt;
    }

    public UUID getId() { return id; }
}
