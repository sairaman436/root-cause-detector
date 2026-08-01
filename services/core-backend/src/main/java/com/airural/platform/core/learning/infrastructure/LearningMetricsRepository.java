/* Purpose: Persists learning metrics. Why it exists: Continuous learning observability requires durable metric snapshots. Architecture fit: JPA adapter for learning metrics. */
package com.airural.platform.core.learning.infrastructure;

import com.airural.platform.core.learning.domain.LearningMetricsEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for learning metrics. */
public interface LearningMetricsRepository extends JpaRepository<LearningMetricsEntity, UUID> {}
