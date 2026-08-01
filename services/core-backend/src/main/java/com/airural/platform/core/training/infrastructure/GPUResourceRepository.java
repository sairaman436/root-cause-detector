/*
 * Purpose: Persists GPU resource inventory and allocation state.
 * Why it exists: The scheduler needs a queryable abstraction for single-GPU, multi-GPU, and future cluster capacity.
 * Architecture fit: Infrastructure adapter for AI-3 GPU resource management.
 */
package com.airural.platform.core.training.infrastructure;

import com.airural.platform.core.training.domain.GPUResourceEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for GPU resources. */
public interface GPUResourceRepository extends JpaRepository<GPUResourceEntity, UUID> {
    List<GPUResourceEntity> findByStatus(String status);
}
