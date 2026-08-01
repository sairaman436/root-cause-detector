/* Purpose: Persists inference responses. Why it exists: Output validation, citation validation, fallback, and token telemetry need durable records. Architecture fit: JPA adapter for inference responses. */
package com.airural.platform.core.serving.infrastructure;

import com.airural.platform.core.serving.domain.InferenceResponseEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for inference responses. */
public interface InferenceResponseRepository extends JpaRepository<InferenceResponseEntity, UUID> {}
