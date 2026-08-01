/*
 * Purpose: Selects GPU capacity for scheduled jobs.
 * Why it exists: AI-3 must be single GPU, multi-GPU, queue, and future cluster ready without binding to a vendor runtime.
 * Architecture fit: Application component for training resource abstraction.
 */
package com.airural.platform.core.training.application;

import com.airural.platform.core.training.domain.GPUResourceEntity;
import com.airural.platform.core.training.infrastructure.GPUResourceRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** GPU resource selection component. */
@Component
public class GPUResourceManager {
    private final GPUResourceRepository gpuResources;

    public GPUResourceManager(GPUResourceRepository gpuResources) {
        this.gpuResources = gpuResources;
    }

    /** Finds available capacity that can satisfy the job. */
    public Optional<GPUResourceEntity> allocate(int requestedGpuCount, int requestedVramGb) {
        return gpuResources.findByStatus("AVAILABLE").stream()
                .filter(resource -> resource.getGpuCount() >= requestedGpuCount)
                .filter(resource -> resource.getTotalVramGb() - resource.getAllocatedVramGb() >= requestedVramGb)
                .findFirst();
    }
}
