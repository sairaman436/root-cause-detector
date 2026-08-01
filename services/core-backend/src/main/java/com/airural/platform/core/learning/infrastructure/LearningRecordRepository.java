/* Purpose: Persists learning records. Why it exists: AI-7 APIs need durable interaction history. Architecture fit: JPA adapter for continuous learning aggregates. */
package com.airural.platform.core.learning.infrastructure;

import com.airural.platform.core.learning.domain.LearningRecordEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for learning records. */
public interface LearningRecordRepository extends JpaRepository<LearningRecordEntity, UUID> {}
