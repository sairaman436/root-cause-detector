/*
 * Purpose: Stores GPU resource inventory and allocation state.
 * Why it exists: Single GPU, multi-GPU, and future cluster scheduling need a durable abstraction for capacity.
 * Architecture fit: AI-3 GPU resource manager entity.
 */
package com.airural.platform.core.training.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** GPU resource entity. */
@Entity
@Table(name = "gpu_resources", schema = "training")
public class GPUResourceEntity {
    @Id private UUID id;
    private String resourceName;
    private String resourceType;
    private Integer gpuCount;
    private Integer totalVramGb;
    private Integer allocatedVramGb;
    private String status;
    private Instant lastHeartbeatAt;

    protected GPUResourceEntity() {}

    /** Creates a GPU resource record. */
    public GPUResourceEntity(UUID id, String resourceName, String resourceType, Integer gpuCount, Integer totalVramGb, Integer allocatedVramGb, String status, Instant lastHeartbeatAt) {
        this.id = id; this.resourceName = resourceName; this.resourceType = resourceType; this.gpuCount = gpuCount; this.totalVramGb = totalVramGb; this.allocatedVramGb = allocatedVramGb; this.status = status; this.lastHeartbeatAt = lastHeartbeatAt;
    }

    public UUID getId() { return id; }
    public String getResourceName() { return resourceName; }
    public Integer getGpuCount() { return gpuCount; }
    public Integer getTotalVramGb() { return totalVramGb; }
    public Integer getAllocatedVramGb() { return allocatedVramGb; }
    public String getStatus() { return status; }
}
