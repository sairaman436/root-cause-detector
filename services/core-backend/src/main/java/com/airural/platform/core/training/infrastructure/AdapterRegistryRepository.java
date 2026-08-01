/*
 * Purpose: Persists LoRA and QLoRA adapter metadata.
 * Why it exists: Adapter lineage and integrity must be tracked without merging or deploying adapters in this milestone.
 * Architecture fit: Infrastructure adapter for AI-3 adapter registry.
 */
package com.airural.platform.core.training.infrastructure;

import com.airural.platform.core.training.domain.AdapterRegistryEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for adapter registry records. */
public interface AdapterRegistryRepository extends JpaRepository<AdapterRegistryEntity, UUID> {
}
