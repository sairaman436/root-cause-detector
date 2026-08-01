/* Purpose: Persists inference metrics. Why it exists: Serving performance and SRE dashboards need durable snapshots. Architecture fit: JPA adapter for serving observability. */
package com.airural.platform.core.serving.infrastructure;

import com.airural.platform.core.serving.domain.InferenceMetricsEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for inference metrics. */
public interface InferenceMetricsRepository extends JpaRepository<InferenceMetricsEntity, UUID> {}
