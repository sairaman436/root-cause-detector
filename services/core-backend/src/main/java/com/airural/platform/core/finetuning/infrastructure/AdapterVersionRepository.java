/*
 * Purpose: Persists adapter artifact versions.
 * Why it exists: LoRA and QLoRA outputs need versioned storage URI, checksum, and status metadata.
 * Architecture fit: Infrastructure adapter for AI-4 adapter artifacts.
 */
package com.airural.platform.core.finetuning.infrastructure;

import com.airural.platform.core.finetuning.domain.AdapterVersionEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for adapter versions. */
public interface AdapterVersionRepository extends JpaRepository<AdapterVersionEntity, UUID> {
}
