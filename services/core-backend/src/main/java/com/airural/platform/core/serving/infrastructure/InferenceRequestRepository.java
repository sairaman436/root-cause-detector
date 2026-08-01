/* Purpose: Persists inference requests. Why it exists: Gateway request pipeline decisions must be auditable. Architecture fit: JPA adapter for inference requests. */
package com.airural.platform.core.serving.infrastructure;

import com.airural.platform.core.serving.domain.InferenceRequestEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for inference requests. */
public interface InferenceRequestRepository extends JpaRepository<InferenceRequestEntity, UUID> {}
