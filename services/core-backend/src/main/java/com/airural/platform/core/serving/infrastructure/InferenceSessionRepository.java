/* Purpose: Persists inference sessions. Why it exists: Serving APIs need conversation and context history. Architecture fit: JPA adapter for serving sessions. */
package com.airural.platform.core.serving.infrastructure;

import com.airural.platform.core.serving.domain.InferenceSessionEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for inference sessions. */
public interface InferenceSessionRepository extends JpaRepository<InferenceSessionEntity, UUID> {}
